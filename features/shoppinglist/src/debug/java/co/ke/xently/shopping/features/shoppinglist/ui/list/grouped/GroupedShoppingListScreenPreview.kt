package co.ke.xently.shopping.features.shoppinglist.ui.list.grouped

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import co.ke.xently.shopping.features.shoppinglist.GroupBy
import co.ke.xently.shopping.features.ui.XentlyPreview
import co.ke.xently.shopping.features.ui.theme.XentlyTheme
import co.ke.xently.shopping.features.utils.Shared
import co.ke.xently.shopping.features.utils.State
import co.ke.xently.shopping.libraries.data.source.GroupedShoppingList
import co.ke.xently.shopping.libraries.data.source.ShoppingListItem
import kotlinx.coroutines.flow.flowOf
import java.util.*

@SuppressLint("VisibleForTests")
@XentlyPreview
@Composable
private fun GroupedShoppingListScreenPreview() {
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
        val items = flowOf(PagingData.from(List(5) {
            GroupedShoppingList.DEFAULT.copy(
                shoppingList = shoppingList,
            )
        })).collectAsLazyPagingItems()
        GroupedShoppingListScreen(
            modifier = Modifier.fillMaxSize(),
            shared = Shared(),
            groupBy = GroupBy.DateAdded,
            removeState = State.Success(null),
            groupCount = mapOf(Pair("2021-09-29", shoppingList.size)),
            items = items,
            menuItems = emptySet(),
            groupMenuItems = emptySet(),
        )
    }
}
