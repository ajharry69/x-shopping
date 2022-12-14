package co.ke.xently.shopping.libraries.data.source

import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

data class GroupedShoppingList(
    override val group: String,
    override val numberOfItems: Int,
    val shoppingList: List<ShoppingListItem>,
) : GroupedShoppingListCount(group, numberOfItems) {
    companion object {
        val DEFAULT = GroupedShoppingList(
            numberOfItems = -1,
            group = SimpleDateFormat.getDateInstance().format(Date()),
            shoppingList = List(Random.nextInt(1, 10)) {
                ShoppingListItem.DEFAULT_INSTANCE
            },
        ).run {
            copy(numberOfItems = shoppingList.size)
        }
    }
}