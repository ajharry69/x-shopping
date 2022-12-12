package co.ke.xently.shopping

import co.ke.xently.shopping.features.recommendation.ui.RecommendationNavGraph
import co.ke.xently.shopping.features.shoppinglist.ui.ShoppingListNavGraph
import co.ke.xently.shopping.features.users.ui.UsersNavGraph
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.NavGraphSpec
import com.ramcosta.composedestinations.spec.Route

object RootNavGraph : NavGraphSpec {
    override val destinationsByRoute: Map<String, DestinationSpec<*>> = emptyMap()
    override val route: String = "root"
    override val startRoute: Route = ShoppingListNavGraph

    override val nestedNavGraphs: List<NavGraphSpec> = listOf(
        ShoppingListNavGraph,
        UsersNavGraph,
        RecommendationNavGraph,
    )
}