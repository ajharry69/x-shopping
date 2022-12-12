package co.ke.xently.shopping.features.shoppinglist.ui.list

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
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
import co.ke.xently.shopping.features.shoppinglist.ShoppingListNavGraph
import co.ke.xently.shopping.features.shoppinglist.repositories.ShoppingListGroup
import co.ke.xently.shopping.features.shoppinglist.ui.ShoppingListItemListViewModel
import co.ke.xently.shopping.features.shoppinglist.ui.ShoppingListItemListViewModel.Request
import co.ke.xently.shopping.features.shoppinglist.ui.destinations.ShoppingListItemDetailScreenDestination
import co.ke.xently.shopping.features.shoppinglist.ui.destinations.ShoppingListItemSearchScreenDestination
import co.ke.xently.shopping.features.shoppinglist.ui.list.item.ShoppingListItemListItem
import co.ke.xently.shopping.features.shoppinglist.ui.search.ShoppingListItemSearchScreen
import co.ke.xently.shopping.features.shoppinglist.ui.search.ShoppingListItemSearchScreen.Content
import co.ke.xently.shopping.features.stringRes
import co.ke.xently.shopping.features.ui.ConfirmableDelete
import co.ke.xently.shopping.features.ui.MoveBackNavigationIconButton
import co.ke.xently.shopping.features.ui.ShowRemovalMessage
import co.ke.xently.shopping.features.ui.TopAppBarWithProgressIndicator
import co.ke.xently.shopping.features.utils.Shared
import co.ke.xently.shopping.features.utils.State
import co.ke.xently.shopping.libraries.data.source.ShoppingListItem
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

object ShoppingListItemListScreen {
    data class Args(val group: ShoppingListGroup?)

    @ShoppingListNavGraph
    @Destination(navArgsDelegate = Args::class)
    @Composable
    fun ShoppingListItemListScreen(args: Args, shared: Shared, navigator: DestinationsNavigator) {
        invoke(
            modifier = Modifier.fillMaxSize(),
            group = args.group,
            config = ShoppingListItemSearchScreen.Config(
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
        )
    }

    @Composable
    internal operator fun invoke(
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
    internal operator fun invoke(
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

        val showProgressIndicator by remember(removeState) {
            derivedStateOf {
                removeState is State.Loading
            }
        }

        Scaffold(
            snackbarHost = {
                SnackbarHost(hostState = config.shared.snackbarHostState)
            },
            topBar = {
                TopAppBarWithProgressIndicator(showProgressIndicator = showProgressIndicator) {
                    TopAppBar(
                        title = {
                            val title =
                                stringResource(R.string.feature_shoppinglist_list_toolbar_title)
                            val subTitle = group?.group
                            if (subTitle == null) {
                                Text(title)
                            } else {
                                ListItem(
                                    headlineText = { Text(title) },
                                    supportingText = { Text(subTitle) },
                                )
                            }
                        },
                        navigationIcon = {
                            MoveBackNavigationIconButton(config.shared)
                        },
                        actions = {
                            IconButton(onClick = config.onSearchClick) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = stringResource(R.string.feature_shoppinglist_search_hint),
                                )
                            }
                        },
                    )
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