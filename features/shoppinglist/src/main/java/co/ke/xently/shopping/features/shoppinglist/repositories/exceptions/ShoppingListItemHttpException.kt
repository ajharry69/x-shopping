package co.ke.xently.shopping.features.shoppinglist.repositories.exceptions

import co.ke.xently.shopping.libraries.data.source.remote.HttpException

internal class ShoppingListItemHttpException(
    val name: List<String> = emptyList(),
    val unit: List<String> = emptyList(),
    val unitQuantity: List<String> = emptyList(),
    val purchaseQuantity: List<String> = emptyList(),
    val brands: List<String> = emptyList(),
) : HttpException() {
    override fun hasFieldErrors(): Boolean {
        return arrayOf(name, unit, unitQuantity, purchaseQuantity, brands).any { it.isNotEmpty() }
    }
}