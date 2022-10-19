package co.ke.xently.shopping.features.customers.ui.search

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
import androidx.hilt.navigation.compose.hiltViewModel
import co.ke.xently.shopping.features.customers.R
import co.ke.xently.shopping.features.customers.ui.CustomerListViewModel
import co.ke.xently.shopping.features.customers.ui.list.item.CustomerListItem
import co.ke.xently.shopping.features.search.ui.SearchScreen
import co.ke.xently.shopping.features.ui.*
import co.ke.xently.shopping.features.utils.ListState
import co.ke.xently.shopping.features.utils.Shared
import co.ke.xently.shopping.features.utils.State
import co.ke.xently.shopping.libraries.data.source.Customer
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

internal object CustomerSearchScreen : SearchScreen<Customer, Customer>(R.string.fc_search_hint) {
    data class Config(
        val shared: Shared = Shared(),
        val onFabClick: () -> Unit = {},
        val onRefresh: () -> Unit = {},
        val onSearchClick: () -> Unit = {},
        val onRetryClicked: (Throwable) -> Unit = {},
    )

    override fun Customer.queryTextOnSuggestionClick() = taxPin

    private val itemContent: @Composable (LazyItemScope.(Customer, Boolean, Set<CustomerListItem.MenuItem>) -> Unit) =
        { customer, isRefreshing, menuItems ->
            CustomerListItem(
                modifier = Modifier.fillMaxWidth(),
                customer = customer,
                menuItems = menuItems,
                showPlaceholder = isRefreshing,
            )
        }

    @Composable
    fun Content(
        modifier: Modifier,
        isRefreshing: Boolean,
        listState: ListState<Customer>,
        config: Config,
        menuItems: Set<CustomerListItem.MenuItem>,
    ) {
        when (listState) {
            is ListState.Error -> {
                FullscreenError(
                    error = listState.error,
                    modifier = modifier,
                    click = ErrorButtonClick(retryAble = config.onRetryClicked),
                )
            }
            ListState.Loading -> {
                FullscreenLoading<Customer>(
                    placeholderContent = { itemContent(it, isRefreshing, menuItems) },
                    modifier = modifier,
//                    placeholder = { Customer.DEFAULT_INSTANCE },
                    numberOfPlaceholders = PLACEHOLDER_COUNT_LARGE_ITEM_SIZE,
                )
            }
            is ListState.Success -> {
                val items = listState.data
                Content(
                    items = items,
                    config = config,
                    modifier = modifier,
                    menuItems = menuItems,
                    isRefreshing = isRefreshing,
                )
            }
        }
    }

    @Composable
    private fun Content(
        items: List<Customer>,
        modifier: Modifier,
        config: Config,
        isRefreshing: Boolean,
        menuItems: Set<CustomerListItem.MenuItem>,
    ) {
        if (items.isEmpty()) {
            FullscreenEmptyList<Customer>(
                modifier = modifier,
                error = R.string.fc_list_empty_list,
            )
        } else {
            SwipeRefresh(
                onRefresh = config.onRefresh,
                modifier = modifier,
                state = rememberSwipeRefreshState(isRefreshing),
            ) {
                val state = rememberLazyListState()
                LazyColumn(state = state) {
                    items(
                        items = items,
                        key = { it.taxPin },
                        itemContent = { itemContent(it, isRefreshing, menuItems) },
                    )
                }
            }
        }
    }

    @Composable
    operator fun invoke(
        modifier: Modifier,
        config: Config,
        menuItems: Set<CustomerListItem.MenuItem>,
        viewModel: CustomerListViewModel = hiltViewModel(),
        searchViewModel: CustomerSearchViewModel = hiltViewModel(),
    ) {
        val scope = rememberCoroutineScope()
        val listState: ListState<Customer> by viewModel.listState.collectAsState(
            context = scope.coroutineContext,
            initial = ListState.Success(emptyList()),
        )

        val removeState by viewModel.removeState.collectAsState(
            context = scope.coroutineContext,
            initial = State.Success(null),
        )

        ShowRemovalMessage(
            removeState = removeState,
            hostState = config.shared.snackbarHostState,
            successMessage = R.string.fc_list_success_removing_customer,
        )

        val searchResults by searchViewModel.searchResults.collectAsState(initial = emptyList())
        val suggestions by searchViewModel.searchAutoCompleteResults.collectAsState(initial = emptyList())

        val state = rememberSearchState()
        state.searchResults = searchResults
        state.suggestions = suggestions
        CustomerSearchScreen(
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
                    val items = response.data as List<Customer>
                    Content(
                        items = items,
                        config = config,
                        menuItems = menuItems,
                        isRefreshing = false,
                        modifier = modifier.padding(values),
                    )
                }
                is SearchResponse.Suggestions -> {
                    LazyColumn(modifier = modifier.padding(values)) {
                        items(
                            key = { it.suggestionTextValue() },
                            items = response.data as List<Customer>,
                        ) {
                            Suggestion(state = state, suggestion = it)
                        }
                    }
                }
            }
        }
    }
}