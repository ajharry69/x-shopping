package co.ke.xently.shopping.libraries.data.source

import co.ke.xently.shopping.libraries.data.source.utils.DateFormat.DEFAULT_LOCAL_DATE_FORMAT
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
            group = DEFAULT_LOCAL_DATE_FORMAT.format(Date()),
            shoppingList = List(Random.nextInt(1, 20)) {
                ShoppingListItem.DEFAULT
            },
        ).run {
            copy(numberOfItems = shoppingList.size)
        }
    }
}