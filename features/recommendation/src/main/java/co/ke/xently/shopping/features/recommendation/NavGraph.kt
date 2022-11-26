package co.ke.xently.shopping.features.recommendation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.navigation
import co.ke.xently.shopping.features.recommendation.ui.RecommendationScreen
import co.ke.xently.shopping.features.recommendation.ui.request.RecommendationRequestScreen
import co.ke.xently.shopping.features.utils.Routes
import co.ke.xently.shopping.features.utils.Shared
import co.ke.xently.shopping.features.utils.buildRoute
import com.google.accompanist.navigation.animation.composable

fun NavGraphBuilder.recommendationGraph(shared: Shared, navController: NavHostController) {
    navigation(
        route = Routes.Recommendation.toString(),
        startDestination = Routes.Recommendation.REQUEST,
    ) {
        composable(route = Routes.Recommendation.REQUEST) {
            RecommendationRequestScreen(
                modifier = Modifier.fillMaxSize(),
                config = RecommendationRequestScreen.Config(
                    shared = shared,
                    onRecommendClick = {
                        navController.navigate(Routes.Recommendation.RECOMMEND.buildRoute()) {
                            launchSingleTop = true
                        }
                    },
                ),
            )
        }
        composable(route = Routes.Recommendation.RECOMMEND) {
            RecommendationScreen(
                modifier = Modifier.fillMaxSize(),
                config = RecommendationScreen.Config(
                    shared = shared,
                ),
            )
        }
    }
}
