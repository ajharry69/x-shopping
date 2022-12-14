package co.ke.xently.shopping.features.shoppinglist.ui.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import co.ke.xently.shopping.features.search.ui.SearchScreen
import co.ke.xently.shopping.features.shoppinglist.R
import co.ke.xently.shopping.features.shoppinglist.ShoppingListNavGraph
import co.ke.xently.shopping.features.shoppinglist.ui.ShoppingListItemListViewModel
import co.ke.xently.shopping.features.shoppinglist.ui.destinations.ShoppingListItemDetailScreenDestination
import co.ke.xently.shopping.features.shoppinglist.ui.destinations.ShoppingListItemSearchScreenDestination
import co.ke.xently.shopping.features.shoppinglist.ui.list.item.ShoppingListItemListItem
import co.ke.xently.shopping.features.ui.Fullscreen
import co.ke.xently.shopping.features.ui.PagedDataScreen
import co.ke.xently.shopping.features.ui.ShowRemovalMessage
import co.ke.xently.shopping.features.utils.Shared
import co.ke.xently.shopping.features.utils.State
import co.ke.xently.shopping.libraries.data.source.ShoppingListItem
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.flow.flowOf

internal object ShoppingListItemSearchScreen :
    SearchScreen<ShoppingListItem, ShoppingListItem>(R.string.feature_shoppinglist_search_hint) {
    @Stable
    data class Config(
        val shared: Shared = Shared(),
        val onFabClick: () -> Unit = {},
        val onSearchClick: () -> Unit = {},
    )

    override fun ShoppingListItem.queryTextOnSuggestionClick() = id.toString()

    @Composable
    fun Content(
        modifier: Modifier,
        config: Config,
        items: LazyPagingItems<ShoppingListItem>,
        menuItems: Set<ShoppingListItemListItem.MenuItem>,
        listState: LazyListState = rememberLazyListState(),
    ) {
        PagedDataScreen(
            modifier = modifier,
            items = items,
            state = items.loadState.refresh,
            loadingContent = {
                Fullscreen.Loading(
                    modifier = modifier,
                    placeholder = { ShoppingListItem.DEFAULT_INSTANCE },
                ) { shoppingListItem ->
                    ShoppingListItemListItem(
                        menuItems = menuItems,
                        showPlaceholder = true,
                        item = shoppingListItem,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            errorContent = {
                Fullscreen.Error(
                    modifier = modifier,
                    error = it.error,
                    onErrorClick = { items.retry() },
                )
            },
            successWithEmptyListContent = {
                Fullscreen.EmptyList<ShoppingListItem>(
                    modifier = modifier,
                    error = stringResource(R.string.feature_shoppinglist_list_empty_list),
                )
            },
            content = {
                LazyColumn(state = listState) {
                    items(items, key = { it.id }) { item ->
                        val shoppingListItem = item ?: ShoppingListItem.DEFAULT_INSTANCE
                        val showPlaceholder by remember(shoppingListItem) {
                            derivedStateOf {
                                shoppingListItem.id == ShoppingListItem.DEFAULT_INSTANCE.id
                            }
                        }
                        ShoppingListItemListItem(
                            item = shoppingListItem,
                            menuItems = menuItems,
                            showPlaceholder = showPlaceholder,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    item {
                        PagedDataScreen.DefaultSetupAppendLoadState(
                            items = items,
                            snackbarHostState = config.shared.snackbarHostState,
                        )
                    }
                }
            },
        )
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

        val config = remember(shared) {
            Config(
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
            )
        }

        val menuItems = remember {
            setOf(
                ShoppingListItemListItem.MenuItem(
                    label = R.string.feature_shoppinglist_list_item_drop_down_menu_update,
                    onClick = {
                        navigator.navigate(ShoppingListItemDetailScreenDestination(it.id)) {
                            launchSingleTop = true
                        }
                    },
                ),
            )
        }

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
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(values),
                        config = config,
                        items = listState,
                        menuItems = menuItems,
                    )
                }
                is SearchResponse.NoResults -> {
                    NoResults(Modifier
                        .fillMaxSize()
                        .padding(values))
                }
                is SearchResponse.Results -> {
                    val items = remember(response.data) {
                        flowOf(PagingData.from(response.data as List<ShoppingListItem>))
                    }.collectAsLazyPagingItems()
                    Content(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(values),
                        config = config,
                        items = items,
                        menuItems = menuItems,
                    )
                }
                is SearchResponse.Suggestions -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(values),
                    ) {
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