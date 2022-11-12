package co.ke.xently.shopping.libraries.data.source.remote.models

import co.ke.xently.shopping.libraries.data.source.AbstractAttribute
import co.ke.xently.shopping.libraries.data.source.AbstractBrand
import java.util.*

data class ProductResource(
    val id: Long,
    val name: String,
    val unit: String,
    val unitPrice: Float,
    val unitQuantity: Float,
    val purchasedQuantity: Float,
    val shop: Long,
    val datePurchased: Date,
    val brands: List<Brand>,
    val attributes: List<Attribute>,
) {
    data class Attribute(
        override val name: String,
        override val value: String,
        val values: List<String>? = emptyList(),
    ) : AbstractAttribute()

    data class Brand(override val name: String) : AbstractBrand()
}