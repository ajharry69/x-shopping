package co.ke.xently.shopping.features.shops

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navigation
import co.ke.xently.shopping.features.shops.ui.detail.ShopDetailScreen
import co.ke.xently.shopping.features.utils.Routes
import co.ke.xently.shopping.features.utils.Shared
import co.ke.xently.shopping.features.utils.buildRoute
import co.ke.xently.shopping.libraries.data.source.Shop
import com.google.accompanist.navigation.animation.composable

fun NavGraphBuilder.shopGraph(shared: Shared) {
    navigation(
        route = Routes.Shop.toString(),
        startDestination = Routes.Shop.DETAIL.buildRoute("id" to Shop.DEFAULT_INSTANCE.id),
    ) {
        composable(
            route = Routes.Shop.DETAIL,
            arguments = listOf(
                navArgument("id") {
                    type = NavType.StringType
                    defaultValue = Shop.DEFAULT_INSTANCE.id.toString()
                },
                navArgument("name") {
                    type = NavType.StringType
                    defaultValue = null
                    nullable = true
                },
            ),
        ) {
            ShopDetailScreen(
                id = it.arguments?.getString("id")?.toLongOrNull() ?: Shop.DEFAULT_INSTANCE.id,
                modifier = Modifier.fillMaxSize(),
                config = ShopDetailScreen.Config(
                    shared = shared,
                    name = it.arguments?.getString("name"),
                ),
            )
        }
    }
}
