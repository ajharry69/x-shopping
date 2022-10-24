package co.ke.xently.shopping.features.shoppinglist

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.*
import co.ke.xently.shopping.features.shoppinglist.ui.ShoppingListItemListViewModel
import co.ke.xently.shopping.features.shoppinglist.ui.detail.ShoppingListItemDetailScreen
import co.ke.xently.shopping.features.shoppinglist.ui.list.ShoppingListItemListScreen
import co.ke.xently.shopping.features.shoppinglist.ui.list.item.ShoppingListItemListItem
import co.ke.xently.shopping.features.shoppinglist.ui.search.ShoppingListItemSearchScreen
import co.ke.xently.shopping.features.ui.ConfirmableDelete
import co.ke.xently.shopping.features.utils.Routes
import co.ke.xently.shopping.features.utils.Shared
import co.ke.xently.shopping.features.utils.buildRoute
import co.ke.xently.shopping.libraries.data.source.ShoppingListItem
import com.google.accompanist.navigation.animation.composable

fun NavGraphBuilder.shoppingListGraph(shared: Shared, navController: NavHostController) {
    navigation(route = Routes.ShoppingList.toString(), startDestination = Routes.ShoppingList.LIST) {
        val config = ShoppingListItemSearchScreen.Config(
            shared = shared,
            onFabClick = {
                navController.navigate(Routes.ShoppingList.DETAIL.buildRoute("id" to ShoppingListItem.DEFAULT_INSTANCE.id)) {
                    launchSingleTop = true
                }
            },
            onSearchClick = {
                navController.navigate(Routes.ShoppingList.SEARCH.buildRoute()) {
                    launchSingleTop = true
                }
            },
        )

        @Composable
        fun viewModelWithMenuItems(): Pair<ShoppingListItemListViewModel, Set<ShoppingListItemListItem.MenuItem>> {
            val viewModel = hiltViewModel<ShoppingListItemListViewModel>()
            val viewModelDependentMenuItems = ShoppingListItemListItem.MenuItem.deleteMenuItem(
                onDeleteClick = ConfirmableDelete { viewModel.delete(it.id) },
            )
            val menuItems = setOf(
                ShoppingListItemListItem.MenuItem(
                    label = R.string.feature_shoppinglist_list_item_drop_down_menu_update,
                    onClick = {
                        navController.navigate(Routes.ShoppingList.DETAIL.buildRoute("id" to it.id)) {
                            launchSingleTop = true
                        }
                    },
                ),
            ) + viewModelDependentMenuItems
            return Pair(viewModel, menuItems)
        }

        composable(route = Routes.ShoppingList.SEARCH) {
            val (viewModel, menuItems) = viewModelWithMenuItems()
            ShoppingListItemSearchScreen(
                config = config,
                menuItems = menuItems,
                viewModel = viewModel,
                modifier = Modifier.fillMaxSize(),
            )
        }
        composable(route = Routes.ShoppingList.LIST) {
            val (viewModel, menuItems) = viewModelWithMenuItems()
            ShoppingListItemListScreen(
                config = config,
                viewModel = viewModel,
                menuItems = menuItems,
                modifier = Modifier.fillMaxSize(),
            )
        }
        composable(
            route = Routes.ShoppingList.DETAIL,
            arguments = listOf(
                navArgument("id") {
                    type = NavType.StringType
                },
            ),
        ) {
            ShoppingListItemDetailScreen(
                id = it.arguments?.getString("id")?.toLongOrNull()
                    ?: ShoppingListItem.DEFAULT_INSTANCE.id,
                modifier = Modifier.fillMaxSize(),
                config = ShoppingListItemDetailScreen.Config(
                    shared = shared,
                ),
            )
        }
    }
}
