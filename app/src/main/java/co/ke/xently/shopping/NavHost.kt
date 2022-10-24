package co.ke.xently.shopping

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import co.ke.xently.shopping.features.customers.customersGraph
import co.ke.xently.shopping.features.users.authenticationGraph
import co.ke.xently.shopping.features.utils.Routes
import co.ke.xently.shopping.features.utils.Shared
import co.ke.xently.shopping.ui.DashboardScreen
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import kotlinx.coroutines.launch

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
                    items = items,
                    config = config,
                    user = shared.user,
                    modifier = Modifier.fillMaxSize(),
                    snackbarHostState = shared.snackbarHostState,
                ) {
                    // TODO: replace content with actual content
                    val scope = rememberCoroutineScope()
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(text = if (it.isClosed) ">>> Swipe >>>" else "<<< Swipe <<<")
                        Spacer(Modifier.height(20.dp))
                        Button(onClick = { scope.launch { it.open() } }) {
                            Text("Click to open")
                        }
                    }
                }
            }
            customersGraph(navController = navController, shared = shared)
            authenticationGraph(navController = navController, shared = shared)
        }
    }
}