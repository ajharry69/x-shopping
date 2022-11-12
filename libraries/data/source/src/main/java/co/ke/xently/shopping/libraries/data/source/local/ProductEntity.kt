package co.ke.xently.shopping.libraries.data.source.local

import androidx.room.*
import co.ke.xently.shopping.libraries.data.source.AbstractAttribute
import co.ke.xently.shopping.libraries.data.source.AbstractBrand
import co.ke.xently.shopping.libraries.data.source.Product
import co.ke.xently.shopping.libraries.data.source.Shop
import java.util.*

@Entity(
    tableName = "products",
    indices = [
        Index("shopId")
    ],
    foreignKeys = [
        ForeignKey(
            entity = ShopEntity::class,
            parentColumns = ["shops_id"],
            childColumns = ["shopId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "products_id")
    val id: Long,
    val name: String,
    val unit: String,
    val unitPrice: Float,
    val shopId: Long,
    val unitQuantity: Float,
    val purchasedQuantity: Float,
    val datePurchased: Date,
) {
    data class WithRelated(
        @Embedded
        val entity: ProductEntity,
        @Relation(parentColumn = "shopId", entityColumn = "shops_id")
        val shop: ShopEntity,
        @Relation(parentColumn = "products_id", entityColumn = "productId")
        val brands: List<Brand>,
        @Relation(parentColumn = "products_id", entityColumn = "productId")
        val attributes: List<Attribute>,
    ) {
        val asUIInstance
            get() = entity.asUIInstance.copy(
                shop = shop.asUIInstance,
                brands = brands.map {
                    Product.Brand(name = it.name)
                },
                attributes = attributes.map {
                    Product.Attribute(
                        name = it.name,
                        value = it.value,
                        values = emptyList(),
                    )
                },
            )
    }

    @Entity(
        tableName = "products_attributes",
        indices = [
            Index("name"),
            Index("value"),
            Index("productId"),
        ],
        primaryKeys = ["name", "value"],
        foreignKeys = [
            ForeignKey(
                entity = ProductEntity::class,
                onDelete = ForeignKey.CASCADE,
                parentColumns = ["products_id"],
                childColumns = ["productId"],
            ),
        ],
    )
    data class Attribute(
        override val name: String,
        override val value: String,
        val productId: Long,
    ) : AbstractAttribute()

    @Entity(
        tableName = "products_brands",
        indices = [
            Index("name"),
            Index("productId"),
        ],
        primaryKeys = ["name", "productId"],
        foreignKeys = [
            ForeignKey(
                entity = ProductEntity::class,
                onDelete = ForeignKey.CASCADE,
                parentColumns = ["products_id"],
                childColumns = ["productId"],
            ),
        ],
    )
    data class Brand(override val name: String, val productId: Long) : AbstractBrand()

    val asUIInstance
        get() = Product(
            id = id,
            name = name,
            unit = unit,
            shop = Shop.DEFAULT_INSTANCE,
            unitQuantity = unitQuantity,
            unitPrice = unitPrice,
            brands = emptyList(),
            attributes = emptyList(),
            purchasedQuantity = purchasedQuantity,
            datePurchased = datePurchased,
        )
}