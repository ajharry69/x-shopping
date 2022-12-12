package co.ke.xently.shopping

import androidx.navigation.NavOptionsBuilder
import co.ke.xently.shopping.features.recommendation.RecommendationNavigator
import co.ke.xently.shopping.features.recommendation.ui.destinations.RecommendationRequestScreenDestination
import co.ke.xently.shopping.features.shoppinglist.ShoppingListNavigator
import co.ke.xently.shopping.features.shoppinglist.repositories.ShoppingListGroup
import co.ke.xently.shopping.features.users.UsersNavigator
import co.ke.xently.shopping.libraries.data.source.ShoppingListItem
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

class Navigator(private val navigator: DestinationsNavigator) : ShoppingListNavigator,
    UsersNavigator, RecommendationNavigator {
    override fun navigateToRecommendationRequestScreen(group: ShoppingListGroup) {
        navigator.navigate(RecommendationRequestScreenDestination()) {
            launchSingleTop = true
        }
    }

    override fun navigateToRecommendationRequestScreen(item: ShoppingListItem) {
        navigator.navigate(RecommendationRequestScreenDestination()) {
            launchSingleTop = true
        }
    }

    override fun clearBackStack(route: String): Boolean {
        return navigator.clearBackStack(route)
    }

    override fun navigate(
        route: String,
        onlyIfResumed: Boolean,
        builder: NavOptionsBuilder.() -> Unit,
    ) {
        return navigator.navigate(route, onlyIfResumed, builder)
    }

    override fun navigateUp(): Boolean {
        return navigator.navigateUp()
    }

    override fun popBackStack(): Boolean {
        return navigator.popBackStack()
    }

    override fun popBackStack(route: String, inclusive: Boolean, saveState: Boolean): Boolean {
        return navigator.popBackStack(route, inclusive, saveState)
    }
}