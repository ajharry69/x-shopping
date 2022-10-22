package co.ke.xently.shopping.libraries.data.source

import co.ke.xently.shopping.libraries.data.source.local.ShoppingListItemEntity
import java.util.*

data class ShoppingListItem(
    val id: Long,
    val name: String,
    val unit: String,
    val unitQuantity: Float,
    val purchaseQuantity: Float,
    val dateAdded: Date,
    val brands: List<Brand>,
    val attributes: List<Attribute>,
) {
    val asEntity
        get() = ShoppingListItemEntity(
            id = id,
            name = name,
            unit = unit,
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

    override fun toString(): String {
        return "${
            name.replaceFirstChar {
                if (it.isLowerCase()) {
                    it.titlecase(Locale.ROOT)
                } else {
                    it.toString()
                }
            }
        }, ${unitQuantity}${unit} - $purchaseQuantity"
    }

    companion object {
        val DEFAULT_INSTANCE = ShoppingListItem(
            id = -1L,
            name = "Gold crown milk",
            unit = "litres",
            brands = emptyList(),
            attributes = emptyList(),
            unitQuantity = 1f,
            purchaseQuantity = 1f,
            dateAdded = Date(),
        )
    }
}