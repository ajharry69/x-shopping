package co.ke.xently.shopping.features.products.repositories.exceptions

import co.ke.xently.shopping.libraries.data.source.remote.HttpException

internal class ProductHttpException(
    val shop: List<String> = emptyList(),
    val name: List<String> = emptyList(),
    val unit: List<String> = emptyList(),
    val unitQuantity: List<String> = emptyList(),
    val unitPrice: List<String> = emptyList(),
    val brands: List<String> = emptyList(),
    val attributes: List<String> = emptyList(),
) : HttpException() {
    override fun hasFieldErrors(): Boolean {
        return arrayOf(
            shop,
            name,
            unit,
            unitQuantity,
            unitPrice,
            brands,
            attributes,
        ).any { it.isNotEmpty() }
    }
}