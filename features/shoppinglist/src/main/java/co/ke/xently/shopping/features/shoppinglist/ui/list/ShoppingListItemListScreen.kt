package co.ke.xently.shopping.features.shoppinglist.ui.list

import androidx.annotation.VisibleForTesting
import androidx.compose.animation.AnimatedVisibility
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
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import co.ke.xently.shopping.features.shoppinglist.R
import co.ke.xently.shopping.features.shoppinglist.ShoppingListNavGraph
import co.ke.xently.shopping.features.shoppinglist.ShoppingListNavigator
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

object ShoppingListItemListScreen {
    data class Args(val group: ShoppingListGroup?)

    @ShoppingListNavGraph
    @Destination(navArgsDelegate = Args::class)
    @Composable
    internal fun ShoppingListItemListScreen(
        args: Args,
        shared: Shared,
        navigator: ShoppingListNavigator,
        viewModel: ShoppingListItemListViewModel = hiltViewModel(),
    ) {
        val items = viewModel.listState.collectAsLazyPagingItems()
        val removeState by viewModel.removeState.collectAsState(State.Success(null))

        LaunchedEffect(args.group) {
            viewModel.fetchShoppingList(Request(group = args.group))
        }
        ShoppingListItemListScreen(
            modifier = Modifier.fillMaxSize(),
            items = items,
            removeState = removeState,
            group = args.group,
            menuItems = setOf(
                ShoppingListItemListItem.MenuItem(
                    label = R.string.feature_shoppinglist_list_item_drop_down_menu_update,
                    onClick = {
                        navigator.navigate(ShoppingListItemDetailScreenDestination(it.id)) {
                            launchSingleTop = true
                        }
                    },
                ),
                ShoppingListItemListItem.MenuItem(
                    onClick = ConfirmableDelete {
                        viewModel.delete(it.id)
                    },
                    label = R.string.feature_shoppinglist_list_item_drop_down_menu_delete,
                ),
            ),
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
        )
    }

    @Composable
    @VisibleForTesting
    internal operator fun invoke(
        modifier: Modifier,
        items: LazyPagingItems<ShoppingListItem>,
        removeState: State<Any>,
        group: ShoppingListGroup?,
        menuItems: Set<ShoppingListItemListItem.MenuItem>,
        config: ShoppingListItemSearchScreen.Config,
    ) {
        ShowRemovalMessage(
            removeState = removeState,
            hostState = config.shared.snackbarHostState,
            successMessage = R.string.feature_shoppinglist_list_success_removing_item,
        )

        val showProgressIndicator by remember(removeState) {
            derivedStateOf {
                removeState is State.Loading
            }
        }

        val listState = rememberLazyListState()

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
                val showFab by remember(listState) {
                    derivedStateOf {
                        !listState.isScrollInProgress
                    }
                }
                if (showFab) {
                    // Something weird is going on here! I thought we could use the boolean
                    // (showFab) directly (in AnimatedVisibility) but I was wrong - it doesn't
                    // work as expected.
                    AnimatedVisibility(visible = true) {
                        FloatingActionButton(onClick = config.onFabClick) {
                            Icon(
                                Icons.Default.Add,
                                stringRes(R.string.feature_shoppinglist_detail_toolbar_title,
                                    R.string.feature_shoppinglist_add),
                            )
                        }
                    }
                }
            },
        ) { values: PaddingValues ->
            Content(
                modifier = modifier.padding(values),
                config = config,
                items = items,
                menuItems = menuItems,
                listState = listState,
            )
        }
    }
}