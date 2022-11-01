package co.ke.xently.shopping.features.shoppinglist.ui.list.grouped

import androidx.annotation.VisibleForTesting
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
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
import co.ke.xently.shopping.features.PagedDataScreen
import co.ke.xently.shopping.features.shoppinglist.GroupBy
import co.ke.xently.shopping.features.shoppinglist.R
import co.ke.xently.shopping.features.shoppinglist.ui.list.grouped.GroupedShoppingListViewModel.Request
import co.ke.xently.shopping.features.shoppinglist.ui.list.grouped.item.GroupedShoppingListItemCard
import co.ke.xently.shopping.features.shoppinglist.ui.list.item.ShoppingListItemListItem
import co.ke.xently.shopping.features.ui.ConfirmableDelete
import co.ke.xently.shopping.features.ui.ShowRemovalMessage
import co.ke.xently.shopping.features.ui.ToolbarWithProgressbar
import co.ke.xently.shopping.features.stringRes
import co.ke.xently.shopping.features.utils.Shared
import co.ke.xently.shopping.features.utils.State
import co.ke.xently.shopping.libraries.data.source.GroupedShoppingList
import co.ke.xently.shopping.libraries.data.source.ShoppingListItem

object GroupedShoppingListScreen {
    data class Config(
        val shared: Shared = Shared(),
        val onFabClick: () -> Unit = {},
        val onRefresh: () -> Unit = {},
        val onSearchClick: () -> Unit = {},
        val onRetryClicked: (Throwable) -> Unit = {},
        val config: GroupedShoppingListItemCard.Config = GroupedShoppingListItemCard.Config(),
    )

    @Composable
    operator fun invoke(
        modifier: Modifier,
        config: Config,
        menuItems: Set<ShoppingListItemListItem.MenuItem>,
        groupMenuItems: Set<GroupedShoppingListItemCard.MenuItem>,
        viewModel: GroupedShoppingListViewModel = hiltViewModel(),
    ) {
        val items = viewModel.groupedShoppingList.collectAsLazyPagingItems()
        val groupedShoppingListCount by viewModel.groupedShoppingListCount.collectAsState()
        val removeState by viewModel.removeState.collectAsState(State.Success(null))

        LaunchedEffect(true) {
            viewModel.fetchGroupedShoppingList(Request())
        }

        GroupedShoppingListScreen(
            modifier = modifier,
            items = items,
            config = config,
            removeState = removeState,
            groupBy = GroupBy.DateAdded,
            groupMenuItems = groupMenuItems,
            groupCount = groupedShoppingListCount,
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
        config: Config,
        groupBy: GroupBy,
        removeState: State<Any>,
        groupCount: Map<Any, Int>,
        items: LazyPagingItems<GroupedShoppingList>,
        menuItems: Set<ShoppingListItemListItem.MenuItem>,
        groupMenuItems: Set<GroupedShoppingListItemCard.MenuItem>,
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
                    showProgress = removeState is State.Loading,
                    navigationIcon = {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = stringResource(R.string.feature_shoppinglist_content_description_open_drawer),
                        )
                    },
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
                val showFab by remember(listState) {
                    derivedStateOf {
                        !listState.isScrollInProgress
                    }
                }
                AnimatedVisibility(visible = showFab) {
                    FloatingActionButton(onClick = config.onFabClick) {
                        Icon(
                            Icons.Default.Add,
                            stringRes(R.string.feature_shoppinglist_detail_toolbar_title,
                                R.string.feature_shoppinglist_add),
                        )
                    }
                }
            },
        ) { values: PaddingValues ->
            PagedDataScreen(
                modifier = modifier.padding(values),
                listState = listState,
                items = items,
                key = { it.group },
                snackbarHostState = config.shared.snackbarHostState,
                placeholder = { GroupedShoppingList.DEFAULT },
                emptyListMessage = stringResource(R.string.feature_shoppinglist_list_empty_list),
            ) { groupList ->
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
                    config = config.config,
                    groupMenuItems = groupMenuItems,
                    showPlaceholder = showPlaceholder,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
            }
        }
    }
}