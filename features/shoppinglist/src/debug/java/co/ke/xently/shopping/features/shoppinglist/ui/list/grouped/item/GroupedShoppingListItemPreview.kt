package co.ke.xently.shopping.features.shoppinglist.ui.list.grouped.item

import androidx.compose.runtime.Composable
import co.ke.xently.shopping.features.ui.XentlyPreview
import co.ke.xently.shopping.features.ui.theme.XentlyTheme
import co.ke.xently.shopping.libraries.data.source.GroupedShoppingList
import co.ke.xently.shopping.libraries.data.source.ShoppingListItem
import java.util.*

@XentlyPreview
@Composable
private fun GroupedShoppingListItemPreview() {
    XentlyTheme {
        val shoppingList = listOf(
            ShoppingListItem.DEFAULT_INSTANCE.copy(id = 1L,
                name = "Bread",
                unit = "grams",
                unitQuantity = 400f,
                purchaseQuantity = 1f,
                dateAdded = Date()),
            ShoppingListItem.DEFAULT_INSTANCE.copy(id = 2L,
                name = "Milk",
                unit = "litres",
                unitQuantity = 1f,
                purchaseQuantity = 1f,
                dateAdded = Date()),
            ShoppingListItem.DEFAULT_INSTANCE.copy(id = 3L,
                name = "Sugar",
                unit = "kilograms",
                unitQuantity = 2f,
                purchaseQuantity = 1f,
                dateAdded = Date()),
            ShoppingListItem.DEFAULT_INSTANCE.copy(id = 4L,
                name = "Toothpaste",
                unit = "millilitres",
                unitQuantity = 75f,
                purchaseQuantity = 1f,
                dateAdded = Date()),
            ShoppingListItem.DEFAULT_INSTANCE.copy(id = 5L,
                name = "Book",
                unit = "piece",
                unitQuantity = 1f,
                purchaseQuantity = 1f,
                dateAdded = Date()),
        )
        GroupedShoppingListItemCard(
            GroupedShoppingList.DEFAULT.copy(
                group = "2021-09-29",
                shoppingList = shoppingList,
            ),
            listCount =
            mapOf(Pair("2021-09-29", shoppingList.size)),
        )
    }
}