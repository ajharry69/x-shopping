package co.ke.xently.shopping

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import co.ke.xently.shopping.features.customers.customersGraph
import co.ke.xently.shopping.features.users.authenticationGraph
import co.ke.xently.shopping.features.utils.Routes
import co.ke.xently.shopping.features.utils.Shared
import co.ke.xently.shopping.ui.dashboard.DashboardScreen
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable

object NavHost {
    @Composable
    operator fun invoke(
        shared: Shared,
        showSignOutProgressbar: Boolean,
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
                    modifier = Modifier.fillMaxSize(),
                    user = shared.user,
                    items = items,
                    showProgressbar = showSignOutProgressbar,
                    snackbarHostState = shared.snackbarHostState,
                    config = config,
                )
            }
            customersGraph(navController = navController, shared = shared)
            authenticationGraph(navController = navController, shared = shared)
        }
    }
}