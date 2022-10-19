package co.ke.xently.shopping.features.customers

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.*
import co.ke.xently.shopping.features.customers.ui.CustomerListViewModel
import co.ke.xently.shopping.features.customers.ui.detail.CustomerDetailScreen
import co.ke.xently.shopping.features.customers.ui.list.CustomerListScreen
import co.ke.xently.shopping.features.customers.ui.list.item.CustomerListItem
import co.ke.xently.shopping.features.customers.ui.search.CustomerSearchScreen
import co.ke.xently.shopping.features.ui.ConfirmableDelete
import co.ke.xently.shopping.features.utils.Routes
import co.ke.xently.shopping.features.utils.Shared
import co.ke.xently.shopping.features.utils.buildRoute
import co.ke.xently.shopping.libraries.data.source.Customer
import com.google.accompanist.navigation.animation.composable

fun NavGraphBuilder.customersGraph(shared: Shared, navController: NavHostController) {
    navigation(route = Routes.Customers.toString(), startDestination = Routes.Customers.LIST) {
        val config = CustomerSearchScreen.Config(
            shared = shared,
            onFabClick = {
                navController.navigate(Routes.Customers.DETAIL.buildRoute("id" to Customer.DEFAULT_INSTANCE.id)) {
                    launchSingleTop = true
                }
            },
            onSearchClick = {
                navController.navigate(Routes.Customers.SEARCH.buildRoute()) {
                    launchSingleTop = true
                }
            },
        )

        @Composable
        fun viewModelWithMenuItems(): Pair<CustomerListViewModel, Set<CustomerListItem.MenuItem>> {
            val viewModel = hiltViewModel<CustomerListViewModel>()
            val viewModelDependentMenuItems = CustomerListItem.MenuItem.deleteMenuItem(
                onDeleteClick = ConfirmableDelete { viewModel.delete(it.id) },
            )
            val menuItems = setOf(
                CustomerListItem.MenuItem(
                    label = R.string.fc_list_item_drop_down_menu_update,
                    onClick = {
                        navController.navigate(Routes.Customers.DETAIL.buildRoute("id" to it.id)) {
                            launchSingleTop = true
                        }
                    },
                ),
            ) + viewModelDependentMenuItems
            return Pair(viewModel, menuItems)
        }

        composable(route = Routes.Customers.SEARCH) {
            val (viewModel, menuItems) = viewModelWithMenuItems()
            CustomerSearchScreen(
                config = config,
                menuItems = menuItems,
                viewModel = viewModel,
                modifier = Modifier.fillMaxSize(),
            )
        }
        composable(route = Routes.Customers.LIST) {
            val (viewModel, menuItems) = viewModelWithMenuItems()
            CustomerListScreen(
                config = config,
                viewModel = viewModel,
                menuItems = menuItems,
                modifier = Modifier.fillMaxSize(),
            )
        }
        composable(
            route = Routes.Customers.DETAIL,
            arguments = listOf(
                navArgument("id") {
                    type = NavType.StringType
                },
            ),
        ) {
            CustomerDetailScreen(
                id = it.arguments?.getString("id")?.toIntOrNull()
                    ?: Customer.DEFAULT_INSTANCE.id,
                modifier = Modifier.fillMaxSize(),
                config = CustomerDetailScreen.Config(
                    shared = shared,
                ),
            )
        }
    }
}
