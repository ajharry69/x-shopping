package co.ke.xently.shopping.navigation

import androidx.navigation.NavOptionsBuilder
import co.ke.xently.shopping.features.products.ProductsNavigator
import co.ke.xently.shopping.features.recommendation.RecommendationNavigator
import co.ke.xently.shopping.features.recommendation.ui.destinations.RecommendationRequestScreenDestination
import co.ke.xently.shopping.features.shoppinglist.ShoppingListNavigator
import co.ke.xently.shopping.features.shops.ui.detail.destinations.ShopDetailScreenDestination
import co.ke.xently.shopping.features.users.UsersNavigator
import co.ke.xently.shopping.libraries.data.source.GroupedShoppingList
import co.ke.xently.shopping.libraries.data.source.ShoppingListItem
import co.ke.xently.shopping.ui.destinations.MainScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

class Navigator(private val navigator: DestinationsNavigator) : ShoppingListNavigator,
    UsersNavigator,
    ProductsNavigator,
    RecommendationNavigator {
    override fun navigateToRecommendationRequestScreen(group: GroupedShoppingList) {
        navigator.navigate(RecommendationRequestScreenDestination(group.shoppingList.toTypedArray())) {
            launchSingleTop = true
        }
    }

    override fun navigateToRecommendationRequestScreen(item: ShoppingListItem) {
        navigator.navigate(RecommendationRequestScreenDestination(arrayOf(item))) {
            launchSingleTop = true
        }
    }

    override fun onAddNewShopClicked(name: String) {
        navigator.navigate(ShopDetailScreenDestination(name = name)) {
            launchSingleTop = true
        }
    }

    override fun navigateToMainScreen(): Boolean {
        return navigator.popBackStack(MainScreenDestination().route, false)
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