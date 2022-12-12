package co.ke.xently.shopping

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import co.ke.xently.shopping.features.products.productGraph
import co.ke.xently.shopping.features.shops.shopGraph
import co.ke.xently.shopping.features.utils.Routes
import co.ke.xently.shopping.features.utils.Shared
import co.ke.xently.shopping.features.utils.buildRoute
import co.ke.xently.shopping.libraries.data.source.Shop
import co.ke.xently.shopping.ui.DashboardScreen
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable

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
        }
    }
}