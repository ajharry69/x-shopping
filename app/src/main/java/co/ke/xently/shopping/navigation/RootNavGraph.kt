package co.ke.xently.shopping.navigation

import co.ke.xently.shopping.features.products.ui.detail.ProductsNavGraph
import co.ke.xently.shopping.features.recommendation.ui.RecommendationNavGraph
import co.ke.xently.shopping.features.shoppinglist.ui.ShoppingListNavGraph
import co.ke.xently.shopping.features.shops.ui.detail.ShopsNavGraph
import co.ke.xently.shopping.features.users.ui.UsersNavGraph
import co.ke.xently.shopping.ui.MainNavGraph
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.NavGraphSpec
import com.ramcosta.composedestinations.spec.Route

object RootNavGraph : NavGraphSpec {
    override val destinationsByRoute: Map<String, DestinationSpec<*>> = emptyMap()
    override val route: String = "root"
    override val startRoute: Route = MainNavGraph

    override val nestedNavGraphs: List<NavGraphSpec> = listOf(
        MainNavGraph,
        ShopsNavGraph,
        UsersNavGraph,
        ProductsNavGraph,
        ShoppingListNavGraph,
        RecommendationNavGraph,
    )
}