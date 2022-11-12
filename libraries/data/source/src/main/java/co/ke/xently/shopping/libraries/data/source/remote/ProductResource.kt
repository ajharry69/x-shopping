package co.ke.xently.shopping.libraries.data.source.remote

import co.ke.xently.shopping.libraries.data.source.AbstractAttribute
import co.ke.xently.shopping.libraries.data.source.AbstractBrand
import co.ke.xently.shopping.libraries.data.source.local.ProductEntity
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
    val asEntity
        get() = ProductEntity(
            id = id,
            name = name,
            unit = unit,
            unitPrice = unitPrice,
            shopId = shop,
            unitQuantity = unitQuantity,
            purchasedQuantity = purchasedQuantity,
            datePurchased = datePurchased,
        )

    data class Attribute(
        override val name: String,
        override val value: String,
        val values: List<String>? = emptyList(),
    ) : AbstractAttribute() {
        override fun toString() = "${name}:${value}"
    }

    data class Brand(override val name: String) : AbstractBrand() {
        override fun toString() = name
    }
}