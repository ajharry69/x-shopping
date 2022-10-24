package co.ke.xently.shopping.features.shoppinglist.ui.list

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import co.ke.xently.shopping.features.shoppinglist.R
import co.ke.xently.shopping.features.shoppinglist.ui.ShoppingListItemListViewModel
import co.ke.xently.shopping.features.shoppinglist.ui.list.item.ShoppingListItemListItem
import co.ke.xently.shopping.features.shoppinglist.ui.search.ShoppingListItemSearchScreen
import co.ke.xently.shopping.features.shoppinglist.ui.search.ShoppingListItemSearchScreen.Content
import co.ke.xently.shopping.features.ui.ConfirmableDelete
import co.ke.xently.shopping.features.ui.ShowRemovalMessage
import co.ke.xently.shopping.features.ui.ToolbarWithProgressbar
import co.ke.xently.shopping.features.ui.stringRes
import co.ke.xently.shopping.features.utils.State
import co.ke.xently.shopping.libraries.data.source.ShoppingListItem

internal object ShoppingListItemListScreen {
    @Composable
    operator fun invoke(
        modifier: Modifier,
        menuItems: Set<ShoppingListItemListItem.MenuItem>,
        config: ShoppingListItemSearchScreen.Config,
        viewModel: ShoppingListItemListViewModel = hiltViewModel(),
    ) {
        val listState = viewModel.listState.collectAsLazyPagingItems()
        val removeState by viewModel.removeState.collectAsState(State.Success(null))

        ShoppingListItemListScreen(
            config = config,
            modifier = modifier,
            listState = listState,
            removeState = removeState,
            isRefreshing = false,
            menuItems = menuItems + ShoppingListItemListItem.MenuItem.deleteMenuItem(
                onDeleteClick = ConfirmableDelete {
                    viewModel.delete(it.id)
                },
            ),
        )
    }

    @Composable
    @VisibleForTesting
    operator fun invoke(
        modifier: Modifier,
        listState: LazyPagingItems<ShoppingListItem>,
        removeState: State<Any>,
        isRefreshing: Boolean,
        menuItems: Set<ShoppingListItemListItem.MenuItem>,
        config: ShoppingListItemSearchScreen.Config,
    ) {

        ShowRemovalMessage(
            removeState = removeState,
            hostState = config.shared.snackbarHostState,
            successMessage = R.string.feature_shoppinglist_list_success_removing_item,
        )

        /*val context = LocalContext.current

        LaunchedEffect(listState) {
            val message = if (listState is ListState.Error) {
                listState.getMessage(context)
            } else {
                return@LaunchedEffect
            }
            config.shared.snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Long,
            )
        }*/

        Scaffold(
            snackbarHost = {
                SnackbarHost(hostState = config.shared.snackbarHostState)
            },
            topBar = {
                ToolbarWithProgressbar(
                    title = stringResource(R.string.feature_shoppinglist_list_toolbar_title),
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
                config = config,
                menuItems = menuItems,
                listState = listState,
                isRefreshing = isRefreshing,
                modifier = modifier.padding(values),
            )
        }
    }
}