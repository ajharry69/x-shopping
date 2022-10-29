package co.ke.xently.shopping

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import co.ke.xently.shopping.features.customers.customersGraph
import co.ke.xently.shopping.features.shoppinglist.shoppingListGraph
import co.ke.xently.shopping.features.shoppinglist.ui.list.grouped.GroupedShoppingListScreen
import co.ke.xently.shopping.features.shoppinglist.ui.list.grouped.item.GroupedShoppingListItemCard
import co.ke.xently.shopping.features.shoppinglist.ui.list.item.ShoppingListItemListItem
import co.ke.xently.shopping.features.users.authenticationGraph
import co.ke.xently.shopping.features.utils.Routes
import co.ke.xently.shopping.features.utils.Shared
import co.ke.xently.shopping.features.utils.buildRoute
import co.ke.xently.shopping.libraries.data.source.ShoppingListItem
import co.ke.xently.shopping.ui.DashboardScreen
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import kotlinx.coroutines.launch

internal object NavHost {
    @Composable
    operator fun invoke(
        shared: Shared,
        navController: NavHostController,
        items: List<DashboardScreen.Item>,
        config: DashboardScreen.Config,
    ) {
        AnimatedNavHost(
            navController = navController,
            startDestination = Routes.Dashboard.toString(),
        ) {
            composable(route = Routes.Dashboard.toString()) {
                DashboardScreen(
                    items = items,
                    config = config,
                    user = shared.user,
                    modifier = Modifier.fillMaxSize(),
                    snackbarHostState = shared.snackbarHostState,
                ) {
                    // TODO: replace content with actual content
                    val scope = rememberCoroutineScope()

                    GroupedShoppingListScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        config = GroupedShoppingListScreen.Config(
                            shared = shared.copy(
                                onNavigationIconClicked = {
                                    scope.launch {
                                        if (it.isOpen) {
                                            it.close()
                                        } else if (it.isClosed) {
                                            it.open()
                                        }
                                    }
                                },
                            ),
                            config = GroupedShoppingListItemCard.Config(
                                onSeeAllClicked = {
                                    navController.navigate(
                                        Routes.ShoppingList.LIST.buildRoute(
                                            "group" to it.group,
                                            "groupBy" to it.groupBy,
                                        )
                                    )
                                }
                            ),
                            onFabClick = {
                                navController.navigate(
                                    Routes.ShoppingList.DETAIL.buildRoute(
                                        "id" to ShoppingListItem.DEFAULT_INSTANCE.id,
                                    ),
                                )
                            },
                        ),
                        menuItems = setOf(
                            ShoppingListItemListItem.MenuItem(R.string.recommend) {
                                // TODO: Implement recommendation...
                            },
                        ),
                        groupMenuItems = setOf(
                            GroupedShoppingListItemCard.MenuItem(R.string.recommend) {
                                // TODO: Implement recommendation...
                            },
                        ),
                    )
                }
            }
            customersGraph(navController = navController, shared = shared)
            shoppingListGraph(navController = navController, shared = shared)
            authenticationGraph(navController = navController, shared = shared)
        }
    }
}