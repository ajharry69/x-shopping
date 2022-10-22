package co.ke.xently.shopping.libraries.data.source

open class GroupedShoppingListCount(
    open val group: String,
    open val numberOfItems: Int,
) {
    override fun equals(other: Any?) = other is GroupedShoppingList && group == other.group

    override fun hashCode(): Int {
        return group.hashCode()
    }
}