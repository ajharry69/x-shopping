package co.ke.xently.shopping.features.shoppinglist.ui.search

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import co.ke.xently.shopping.features.search.ui.SearchScreen
import co.ke.xently.shopping.features.shoppinglist.R
import co.ke.xently.shopping.features.shoppinglist.ShoppingListNavGraph
import co.ke.xently.shopping.features.shoppinglist.ui.ShoppingListItemListViewModel
import co.ke.xently.shopping.features.shoppinglist.ui.destinations.ShoppingListItemDetailScreenDestination
import co.ke.xently.shopping.features.shoppinglist.ui.destinations.ShoppingListItemSearchScreenDestination
import co.ke.xently.shopping.features.shoppinglist.ui.list.item.ShoppingListItemListItem
import co.ke.xently.shopping.features.ui.PagedDataScreen
import co.ke.xently.shopping.features.ui.ShowRemovalMessage
import co.ke.xently.shopping.features.utils.Shared
import co.ke.xently.shopping.features.utils.State
import co.ke.xently.shopping.libraries.data.source.ShoppingListItem
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

internal object ShoppingListItemSearchScreen :
    SearchScreen<ShoppingListItem, ShoppingListItem>(R.string.feature_shoppinglist_search_hint) {
    data class Config(
        val shared: Shared = Shared(),
        val onFabClick: () -> Unit = {},
        val onRefresh: () -> Unit = {},
        val onSearchClick: () -> Unit = {},
        val onRetryClicked: (Throwable) -> Unit = {},
    )

    override fun ShoppingListItem.queryTextOnSuggestionClick() = id.toString()

    private val itemContent: @Composable (LazyItemScope.(ShoppingListItem, Boolean, Set<ShoppingListItemListItem.MenuItem>) -> Unit) =
        { shoppingListItem, isRefreshing, menuItems ->
            ShoppingListItemListItem(
                modifier = Modifier.fillMaxWidth(),
                shoppingListItem = shoppingListItem,
                menuItems = menuItems,
                showPlaceholder = isRefreshing,
            )
        }

    @Composable
    fun Content(
        modifier: Modifier,
        isRefreshing: Boolean,
        config: Config,
        items: LazyPagingItems<ShoppingListItem>,
        menuItems: Set<ShoppingListItemListItem.MenuItem>,
        listState: LazyListState = rememberLazyListState(),
    ) {
        PagedDataScreen(
            modifier = modifier,
            listState = listState,
            items = items,
            key = { it.id },
            snackbarHostState = config.shared.snackbarHostState,
            placeholder = { ShoppingListItem.DEFAULT_INSTANCE },
            emptyListMessage = stringResource(R.string.feature_shoppinglist_list_empty_list),
        ) {
            itemContent(it, isRefreshing, menuItems)
        }
    }

    @ShoppingListNavGraph
    @Destination
    @Composable
    fun ShoppingListItemSearchScreen(
        shared: Shared,
        navigator: DestinationsNavigator,
        viewModel: ShoppingListItemListViewModel = hiltViewModel(),
        searchViewModel: ShoppingListItemSearchViewModel = hiltViewModel(),
    ) {
        val scope = rememberCoroutineScope()
        val listState = viewModel.listState.collectAsLazyPagingItems()

        val removeState by viewModel.removeState.collectAsState(
            context = scope.coroutineContext,
            initial = State.Success(null),
        )

        ShowRemovalMessage(
            removeState = removeState,
            hostState = shared.snackbarHostState,
            successMessage = R.string.feature_shoppinglist_list_success_removing_item,
        )

        val searchResults by searchViewModel.searchResults.collectAsState(initial = emptyList())
        val suggestions by searchViewModel.searchAutoCompleteResults.collectAsState(initial = emptyList())

        val state = rememberSearchState()
        state.searchResults = searchResults
        state.suggestions = suggestions
        ShoppingListItemSearchScreen(
            state = state,
            autoFocusSearchField = true,
            snackbarHostState = shared.snackbarHostState,
            onBack = shared.onNavigationIconClicked,
            onQueryChange = searchViewModel::autoCompleteSearch,
            onSearchImeActionClick = searchViewModel::search,
        ) { values: PaddingValues ->
            @Suppress("UNCHECKED_CAST")
            when (val response = state.searchResponse) {
                is SearchResponse.InitialResults -> {
                    Content(
                        config = Config(
                            shared = shared,
                            onFabClick = {
                                navigator.navigate(ShoppingListItemDetailScreenDestination()) {
                                    launchSingleTop = true
                                }
                            },
                            onSearchClick = {
                                navigator.navigate(ShoppingListItemSearchScreenDestination()) {
                                    launchSingleTop = true
                                }
                            },
                        ),
                        isRefreshing = false,
                        items = listState,
                        menuItems = setOf(
                            ShoppingListItemListItem.MenuItem(
                                label = R.string.feature_shoppinglist_list_item_drop_down_menu_update,
                                onClick = {
                                    navigator.navigate(ShoppingListItemDetailScreenDestination(it.id)) {
                                        launchSingleTop = true
                                    }
                                },
                            ),
                        ),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(values),
                    )
                }
                is SearchResponse.NoResults -> {
                    NoResults(Modifier
                        .fillMaxSize()
                        .padding(values))
                }
                is SearchResponse.Results -> {
//                    val items = response.data as List<ShoppingListItem>
//                    Content(
//                        items = items,
//                        config = config,
//                        menuItems = menuItems,
//                        isRefreshing = false,
//                        modifier = Modifier.fillMaxSize().padding(values),
//                    )
                }
                is SearchResponse.Suggestions -> {
                    LazyColumn(modifier = Modifier
                        .fillMaxSize()
                        .padding(values)) {
                        items(
                            key = { it.suggestionTextValue() },
                            items = response.data as List<ShoppingListItem>,
                        ) {
                            Suggestion(state = state, suggestion = it)
                        }
                    }
                }
            }
        }
    }
}