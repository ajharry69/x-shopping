package co.ke.xently.shopping.features.products

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navigation
import co.ke.xently.shopping.features.products.ui.detail.ProductDetailScreen
import co.ke.xently.shopping.features.utils.Routes
import co.ke.xently.shopping.features.utils.Shared
import co.ke.xently.shopping.features.utils.buildRoute
import co.ke.xently.shopping.libraries.data.source.Product
import com.google.accompanist.navigation.animation.composable

fun NavGraphBuilder.productGraph(shared: Shared, onAddNewShopClicked: (String) -> Unit) {
    navigation(
        route = Routes.Product.toString(),
        startDestination = Routes.Product.DETAIL.buildRoute("id" to Product.DEFAULT_INSTANCE.id),
    ) {
        composable(
            route = Routes.Product.DETAIL,
            arguments = listOf(
                navArgument("id") {
                    type = NavType.StringType
                    defaultValue = Product.DEFAULT_INSTANCE.id.toString()
                },
            ),
        ) {
            ProductDetailScreen(
                id = it.arguments?.getString("id")?.toLongOrNull() ?: Product.DEFAULT_INSTANCE.id,
                modifier = Modifier.fillMaxSize(),
                config = ProductDetailScreen.Config(
                    shared = shared,
                    onAddNewShopClicked = onAddNewShopClicked,
                ),
            )
        }
    }
}
