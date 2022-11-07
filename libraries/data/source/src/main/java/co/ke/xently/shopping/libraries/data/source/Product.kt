package co.ke.xently.shopping.libraries.data.source

import co.ke.xently.shopping.libraries.data.source.local.ProductEntity
import co.ke.xently.shopping.libraries.data.source.remote.ProductResource
import java.util.*

data class Product(
    val id: Long,
    val name: String,
    val unit: String,
    val shop: Shop,
    val unitQuantity: Float,
    val unitPrice: Float,
    val dateAdded: Date,
    val brands: List<Brand>,
    val attributes: List<Attribute>,
) {
    val asEntity
        get() = ProductEntity(
            id = id,
            name = name,
            unit = unit,
            shopId = shop.id,
            unitQuantity = unitQuantity,
            purchaseQuantity = unitPrice,
            dateAdded = dateAdded,
        )

    val asResource
        get() = ProductResource(
            id = id,
            name = name,
            unit = unit,
            shopId = shop.id,
            unitQuantity = unitQuantity,
            purchaseQuantity = unitPrice,
            dateAdded = dateAdded,
            brands = emptyList(),
            attributes = emptyList(),
        ).copy(
            brands = brands.map {
                ProductResource.Brand(name = it.name)
            },
            attributes = attributes.map {
                ProductResource.Attribute(
                    name = it.name,
                    value = it.value,
                    values = emptyList(),
                )
            },
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

    companion object {
        val DEFAULT_INSTANCE = Product(
            id = -1L,
            shop = Shop.DEFAULT_INSTANCE,
            name = "Gold crown milk",
            unit = "litres",
            brands = emptyList(),
            attributes = emptyList(),
            unitQuantity = 1f,
            unitPrice = 0f,
            dateAdded = Date(),
        )
    }
}