package co.ke.xently.shopping.features.shoppinglist.ui.search

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import co.ke.xently.shopping.features.PagedDataScreen
import co.ke.xently.shopping.features.search.ui.SearchScreen
import co.ke.xently.shopping.features.shoppinglist.R
import co.ke.xently.shopping.features.shoppinglist.ui.ShoppingListItemListViewModel
import co.ke.xently.shopping.features.shoppinglist.ui.list.item.ShoppingListItemListItem
import co.ke.xently.shopping.features.ui.*
import co.ke.xently.shopping.features.utils.Shared
import co.ke.xently.shopping.features.utils.State
import co.ke.xently.shopping.libraries.data.source.ShoppingListItem

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
        listState: LazyPagingItems<ShoppingListItem>,
        menuItems: Set<ShoppingListItemListItem.MenuItem>,
    ) {
        PagedDataScreen(
            modifier = modifier,
            listState = rememberLazyListState(),
            items = listState,
            key = { it.id },
            snackbarHostState = config.shared.snackbarHostState,
            placeholder = { ShoppingListItem.DEFAULT_INSTANCE },
            emptyListMessage = stringResource(R.string.feature_shoppinglist_list_empty_list),
        ) {
            itemContent(it, isRefreshing, menuItems)
        }
    }

    @Composable
    operator fun invoke(
        modifier: Modifier,
        config: Config,
        menuItems: Set<ShoppingListItemListItem.MenuItem>,
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
            hostState = config.shared.snackbarHostState,
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
            snackbarHostState = config.shared.snackbarHostState,
            onBack = config.shared.onNavigationIconClicked,
            onQueryChange = searchViewModel::autoCompleteSearch,
            onSearchImeActionClick = searchViewModel::search,
        ) { values: PaddingValues ->
            @Suppress("UNCHECKED_CAST")
            when (val response = state.searchResponse) {
                is SearchResponse.InitialResults -> {
                    Content(
                        config = config,
                        isRefreshing = false,
                        listState = listState,
                        menuItems = menuItems,
                        modifier = modifier.padding(values),
                    )
                }
                is SearchResponse.NoResults -> {
                    NoResults(modifier.padding(values))
                }
                is SearchResponse.Results -> {
//                    val items = response.data as List<ShoppingListItem>
//                    Content(
//                        items = items,
//                        config = config,
//                        menuItems = menuItems,
//                        isRefreshing = false,
//                        modifier = modifier.padding(values),
//                    )
                }
                is SearchResponse.Suggestions -> {
                    LazyColumn(modifier = modifier.padding(values)) {
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