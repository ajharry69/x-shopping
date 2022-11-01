package co.ke.xently.shopping.features.shoppinglist.ui.list

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import co.ke.xently.shopping.features.shoppinglist.R
import co.ke.xently.shopping.features.shoppinglist.repositories.ShoppingListGroup
import co.ke.xently.shopping.features.shoppinglist.ui.ShoppingListItemListViewModel
import co.ke.xently.shopping.features.shoppinglist.ui.ShoppingListItemListViewModel.Request
import co.ke.xently.shopping.features.shoppinglist.ui.list.item.ShoppingListItemListItem
import co.ke.xently.shopping.features.shoppinglist.ui.search.ShoppingListItemSearchScreen
import co.ke.xently.shopping.features.shoppinglist.ui.search.ShoppingListItemSearchScreen.Content
import co.ke.xently.shopping.features.stringRes
import co.ke.xently.shopping.features.ui.ConfirmableDelete
import co.ke.xently.shopping.features.ui.ShowRemovalMessage
import co.ke.xently.shopping.features.ui.ToolbarWithProgressbar
import co.ke.xently.shopping.features.utils.State
import co.ke.xently.shopping.libraries.data.source.ShoppingListItem

internal object ShoppingListItemListScreen {
    @Composable
    operator fun invoke(
        modifier: Modifier,
        group: ShoppingListGroup?,
        menuItems: Set<ShoppingListItemListItem.MenuItem>,
        config: ShoppingListItemSearchScreen.Config,
        viewModel: ShoppingListItemListViewModel = hiltViewModel(),
    ) {
        val items = viewModel.listState.collectAsLazyPagingItems()
        val removeState by viewModel.removeState.collectAsState(State.Success(null))

        LaunchedEffect(group) {
            viewModel.fetchShoppingList(Request(group = group))
        }
        val isRefreshing by remember(items) {
            derivedStateOf {
                items.loadState.refresh == LoadState.Loading
            }
        }
        ShoppingListItemListScreen(
            config = config,
            modifier = modifier,
            items = items,
            group = group,
            isRefreshing = isRefreshing,
            removeState = removeState,
            menuItems = menuItems + ShoppingListItemListItem.MenuItem(
                onClick = ConfirmableDelete {
                    viewModel.delete(it.id)
                },
                label = R.string.feature_shoppinglist_list_item_drop_down_menu_delete,
            ),
        )
    }

    @Composable
    @VisibleForTesting
    operator fun invoke(
        modifier: Modifier,
        items: LazyPagingItems<ShoppingListItem>,
        removeState: State<Any>,
        isRefreshing: Boolean,
        group: ShoppingListGroup?,
        menuItems: Set<ShoppingListItemListItem.MenuItem>,
        config: ShoppingListItemSearchScreen.Config,
    ) {

        ShowRemovalMessage(
            removeState = removeState,
            hostState = config.shared.snackbarHostState,
            successMessage = R.string.feature_shoppinglist_list_success_removing_item,
        )

        val listState = rememberLazyListState()

        Scaffold(
            snackbarHost = {
                SnackbarHost(hostState = config.shared.snackbarHostState)
            },
            topBar = {
                ToolbarWithProgressbar(
                    title = stringResource(R.string.feature_shoppinglist_list_toolbar_title),
                    subTitle = group?.group?.toString(),
                    showProgress = removeState is State.Loading,
                    onNavigationIconClicked = config.shared.onNavigationIconClicked,
                ) {
                    IconButton(onClick = config.onSearchClick) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = stringResource(R.string.feature_shoppinglist_search_hint),
                        )
                    }
                }
            },
            floatingActionButton = {
                FloatingActionButton(onClick = config.onFabClick) {
                    Icon(
                        Icons.Default.Add,
                        stringRes(R.string.feature_shoppinglist_detail_toolbar_title,
                            R.string.feature_shoppinglist_add),
                    )
                }
            },
        ) { values: PaddingValues ->

            Content(
                items = items,
                config = config,
                menuItems = menuItems,
                listState = listState,
                isRefreshing = isRefreshing,
                modifier = modifier.padding(values),
            )
        }
    }
}