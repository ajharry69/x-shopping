package co.ke.xently.shopping.features.shoppinglist.repositories

import co.ke.xently.shopping.features.shoppinglist.GroupBy

data class ShoppingListGroup(
    val group: Any,
    val groupBy: GroupBy = GroupBy.DateAdded,
)