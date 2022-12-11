package co.ke.xently.shopping

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import co.ke.xently.shopping.features.products.productGraph
import co.ke.xently.shopping.features.recommendation.recommendationGraph
import co.ke.xently.shopping.features.shoppinglist.shoppingListGraph
import co.ke.xently.shopping.features.shoppinglist.ui.list.grouped.GroupedShoppingListScreen
import co.ke.xently.shopping.features.shoppinglist.ui.list.grouped.item.GroupedShoppingListItemCard
import co.ke.xently.shopping.features.shoppinglist.ui.list.item.ShoppingListItemListItem
import co.ke.xently.shopping.features.shops.shopGraph
import co.ke.xently.shopping.features.users.authenticationGraph
import co.ke.xently.shopping.features.utils.Routes
import co.ke.xently.shopping.features.utils.Shared
import co.ke.xently.shopping.features.utils.buildRoute
import co.ke.xently.shopping.libraries.data.source.Shop
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
                            ShoppingListItemListItem.MenuItem(R.string.button_label_get_recommendations) {
                                navController.navigate(Routes.Recommendation.REQUEST.buildRoute()) {
                                    launchSingleTop = true
                                }
                            },
                            ShoppingListItemListItem.MenuItem(
                                label = R.string.update,
                                onClick = {
                                    navController.navigate(Routes.ShoppingList.DETAIL.buildRoute("id" to it.id)) {
                                        launchSingleTop = true
                                    }
                                },
                            ),
                        ),
                        groupMenuItems = setOf(
                            GroupedShoppingListItemCard.MenuItem(R.string.button_label_get_recommendations) {
                                navController.navigate(Routes.Recommendation.REQUEST.buildRoute()) {
                                    launchSingleTop = true
                                }
                            },
                        ),
                    )
                }
            }
            shopGraph(shared = shared)
            productGraph(shared = shared) {
                navController.navigate(Routes.Shop.DETAIL.buildRoute(
                    "id" to Shop.DEFAULT_INSTANCE.id,
                    "name" to it)) {
                    launchSingleTop = true
                }
            }
            shoppingListGraph(navController = navController, shared = shared)
            recommendationGraph(navController = navController, shared = shared)
            authenticationGraph(navController = navController, shared = shared)
        }
    }
}