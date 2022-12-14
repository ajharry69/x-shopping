package co.ke.xently.shopping.features.shoppinglist.ui.list.grouped

import androidx.annotation.VisibleForTesting
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import co.ke.xently.shopping.features.shoppinglist.GroupBy
import co.ke.xently.shopping.features.shoppinglist.R
import co.ke.xently.shopping.features.shoppinglist.ShoppingListNavGraph
import co.ke.xently.shopping.features.shoppinglist.ShoppingListNavigator
import co.ke.xently.shopping.features.shoppinglist.repositories.ShoppingListGroup
import co.ke.xently.shopping.features.shoppinglist.ui.destinations.ShoppingListItemDetailScreenDestination
import co.ke.xently.shopping.features.shoppinglist.ui.destinations.ShoppingListItemListScreenDestination
import co.ke.xently.shopping.features.shoppinglist.ui.list.grouped.GroupedShoppingListViewModel.Request
import co.ke.xently.shopping.features.shoppinglist.ui.list.grouped.item.GroupedShoppingListItemCard
import co.ke.xently.shopping.features.shoppinglist.ui.list.item.ShoppingListItemListItem
import co.ke.xently.shopping.features.stringRes
import co.ke.xently.shopping.features.ui.*
import co.ke.xently.shopping.features.utils.Shared
import co.ke.xently.shopping.features.utils.State
import co.ke.xently.shopping.libraries.data.source.GroupedShoppingList
import co.ke.xently.shopping.libraries.data.source.ShoppingListItem
import com.ramcosta.composedestinations.annotation.Destination

object GroupedShoppingListScreen {
    @ShoppingListNavGraph(start = true)
    @Destination
    @Composable
    fun GroupedShoppingListScreen(
        shared: Shared,
        navigator: ShoppingListNavigator,
        viewModel: GroupedShoppingListViewModel = hiltViewModel(),
    ) {
        val items = viewModel.groupedShoppingList.collectAsLazyPagingItems()
        val groupedShoppingListCount by viewModel.groupedShoppingListCount.collectAsState()
        val removeState by viewModel.removeState.collectAsState(State.Success(null))

        LaunchedEffect(true) {
            viewModel.fetchGroupedShoppingList(Request())
        }

        GroupedShoppingListScreen(
            modifier = Modifier.fillMaxSize(),
            items = items,
            shared = shared,
            removeState = removeState,
            groupBy = GroupBy.DateAdded,
            groupCount = groupedShoppingListCount,
            onSeeAllClicked = {
                navigator.navigate(ShoppingListItemListScreenDestination(it)) {
                    launchSingleTop = true
                }
            },
            onFabClick = {
                navigator.navigate(ShoppingListItemDetailScreenDestination()) {
                    launchSingleTop = true
                }
            },
            groupMenuItems = setOf(
                GroupedShoppingListItemCard.MenuItem(
                    label = R.string.button_label_get_recommendations,
                    onClick = navigator::navigateToRecommendationRequestScreen,
                ),
            ),
            menuItems = setOf(
                ShoppingListItemListItem.MenuItem(
                    label = R.string.button_label_get_recommendations,
                    onClick = navigator::navigateToRecommendationRequestScreen,
                ),
                ShoppingListItemListItem.MenuItem(
                    label = R.string.update,
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
        )
    }

    @Composable
    @VisibleForTesting
    internal operator fun invoke(
        modifier: Modifier,
        shared: Shared,
        groupBy: GroupBy,
        removeState: State<Any>,
        groupCount: Map<Any, Int>,
        items: LazyPagingItems<GroupedShoppingList>,
        menuItems: Set<ShoppingListItemListItem.MenuItem>,
        groupMenuItems: Set<GroupedShoppingListItemCard.MenuItem>,
        onSeeAllClicked: (ShoppingListGroup) -> Unit = {},
        onFabClick: () -> Unit = {},
        onSearchClick: () -> Unit = {},
    ) {
        ShowRemovalMessage(
            removeState = removeState,
            hostState = shared.snackbarHostState,
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
                SnackbarHost(hostState = shared.snackbarHostState)
            },
            topBar = {
                TopAppBarWithProgressIndicator(showProgressIndicator = showProgressIndicator) {
                    TopAppBar(
                        title = {
                            Text(stringResource(co.ke.xently.shopping.features.R.string.app_name))
                        },
                        navigationIcon = {
                            IconButton(onClick = shared.onNavigationIconClicked) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = stringResource(R.string.feature_shoppinglist_content_description_open_drawer),
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = onSearchClick) {
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
                        FloatingActionButton(onClick = onFabClick) {
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
            PagedDataScreen(
                modifier = modifier.padding(values),
                items = items,
                state = items.loadState.refresh,
                loadingContent = {
                    Fullscreen.Loading(
                        modifier = modifier,
                        placeholder = { GroupedShoppingList.DEFAULT },
                    ) { groupList ->
                        GroupedShoppingListItemCard(
                            showPlaceholder = true,
                            groupBy = groupBy,
                            groupList = groupList,
                            menuItems = menuItems,
                            listCount = groupCount,
                            groupMenuItems = groupMenuItems,
                            onSeeAllClicked = onSeeAllClicked,
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
                    Fullscreen.EmptyList<GroupedShoppingList>(
                        modifier = modifier,
                        error = stringResource(R.string.feature_shoppinglist_list_empty_list),
                    )
                },
                content = {
                    LazyColumn(
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    ) {
                        items(items, key = { it.group }) { item ->
                            val groupList = item ?: GroupedShoppingList.DEFAULT
                            val showPlaceholder by remember(groupList.shoppingList) {
                                derivedStateOf {
                                    groupList.shoppingList.any { it.id == ShoppingListItem.DEFAULT_INSTANCE.id }
                                }
                            }
                            GroupedShoppingListItemCard(
                                groupBy = groupBy,
                                groupList = groupList,
                                menuItems = menuItems,
                                listCount = groupCount,
                                groupMenuItems = groupMenuItems,
                                showPlaceholder = showPlaceholder,
                                onSeeAllClicked = onSeeAllClicked,
                            )
                        }
                        item {
                            PagedDataScreen.DefaultSetupAppendLoadState(
                                items = items,
                                snackbarHostState = shared.snackbarHostState,
                            )
                        }
                    }
                },
            )
        }
    }
}