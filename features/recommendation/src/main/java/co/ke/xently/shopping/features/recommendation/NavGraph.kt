package co.ke.xently.shopping.features.recommendation

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.navigation
import co.ke.xently.shopping.features.recommendation.ui.RecommendationScreen
import co.ke.xently.shopping.features.recommendation.ui.request.RecommendationRequestScreen
import co.ke.xently.shopping.features.utils.Routes
import co.ke.xently.shopping.features.utils.Shared
import co.ke.xently.shopping.features.utils.buildRoute
import com.google.accompanist.navigation.animation.composable
import com.google.android.material.dialog.MaterialAlertDialogBuilder

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
            val context = LocalContext.current
            RecommendationScreen(
                modifier = Modifier.fillMaxSize(),
                config = RecommendationScreen.Config(
                    shared = shared,
                    onDirectionClick = { recommendation ->
                        val navigationQuery = recommendation.shop.run {
                            coordinates.let {
                                "${it.lat},${it.lon}"
                            }
                        }
                        val uri = Uri.parse("google.navigation:q=$navigationQuery")
                        val mapIntent = Intent(Intent.ACTION_VIEW, uri)
                        if (mapIntent.resolveActivity(context.packageManager) == null) {
                            MaterialAlertDialogBuilder(context)
                                .setMessage(R.string.app_handling_directions_not_found)
                                .setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
                                .create().show()
                        } else {
                            mapIntent.run {
                                setPackage("com.google.android.apps.maps")
                                if (resolveActivity(context.packageManager) != null) {
                                    context.startActivity(this)
                                } else {
                                    context.startActivity(mapIntent)
                                }
                            }
                        }
                    }
                ),
            )
        }
    }
}
