package co.ke.xently.shopping.features.products.models

import co.ke.xently.shopping.libraries.data.source.AbstractAttribute
import co.ke.xently.shopping.libraries.data.source.AbstractBrand
import co.ke.xently.shopping.libraries.data.source.Shop
import co.ke.xently.shopping.libraries.data.source.local.models.ProductEntity
import co.ke.xently.shopping.libraries.data.source.remote.models.ProductResource
import java.util.*

data class Product(
    val id: Long,
    val name: String,
    val unit: String,
    val shop: Shop,
    val unitQuantity: Float,
    val purchasedQuantity: Float,
    val unitPrice: Float,
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
            shopId = shop.id,
            unitQuantity = unitQuantity,
            purchasedQuantity = purchasedQuantity,
            datePurchased = datePurchased,
        )

    val asResource
        get() = ProductResource(
            id = id,
            name = name,
            unit = unit,
            unitPrice = unitPrice,
            shop = shop.id,
            unitQuantity = unitQuantity,
            purchasedQuantity = purchasedQuantity,
            datePurchased = datePurchased,
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
            datePurchased = Date(),
            purchasedQuantity = 0f,
        )
    }
}