package co.ke.xently.shopping.libraries.data.source.remote

import co.ke.xently.shopping.libraries.data.source.AbstractAttribute
import co.ke.xently.shopping.libraries.data.source.AbstractBrand
import co.ke.xently.shopping.libraries.data.source.local.ProductEntity
import java.util.*

data class ProductResource(
    val id: Long,
    val name: String,
    val unit: String,
    val unitQuantity: Float,
    val purchaseQuantity: Float,
    val dateAdded: Date,
    val shopId: Long,
    val brands: List<Brand>,
    val attributes: List<Attribute>,
) {
    val asEntity
        get() = ProductEntity(
            id = id,
            name = name,
            unit = unit,
            shopId = shopId,
            unitQuantity = unitQuantity,
            purchaseQuantity = purchaseQuantity,
            dateAdded = dateAdded,
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