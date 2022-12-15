package co.ke.xently.shopping.features.shoppinglist

import co.ke.xently.shopping.libraries.data.source.GroupedShoppingList
import co.ke.xently.shopping.libraries.data.source.ShoppingListItem
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

interface ShoppingListNavigator: DestinationsNavigator {
    fun navigateToRecommendationRequestScreen(group: GroupedShoppingList)
    fun navigateToRecommendationRequestScreen(item: ShoppingListItem)
}