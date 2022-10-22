package co.ke.xently.shopping.libraries.data.source.local

import androidx.room.*
import co.ke.xently.shopping.libraries.data.source.AbstractAttribute
import co.ke.xently.shopping.libraries.data.source.AbstractBrand
import co.ke.xently.shopping.libraries.data.source.ShoppingListItem
import java.util.*

@Entity(tableName = "shoppinglist")
data class ShoppingListItemEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "shoppinglist_id")
    val id: Long,
    val name: String,
    val unit: String,
    val unitQuantity: Float,
    val purchaseQuantity: Float,
    val dateAdded: Date,
) {
    data class WithRelated(
        @Embedded
        val entity: ShoppingListItemEntity,
        @Relation(parentColumn = "shoppinglist_id", entityColumn = "shoppingListItemId")
        val brands: List<Brand>,
        @Relation(parentColumn = "shoppinglist_id", entityColumn = "shoppingListItemId")
        val attributes: List<Attribute>,
    ) {
        val asUIInstance
            get() = entity.asUIInstance.copy(
                brands = brands.map {
                    ShoppingListItem.Brand(name = it.name)
                },
                attributes = attributes.map {
                    ShoppingListItem.Attribute(
                        name = it.name,
                        value = it.value,
                        values = emptyList(),
                    )
                },
            )
    }

    @Entity(
        tableName = "shoppinglist_attributes",
        indices = [
            Index("name"),
            Index("value"),
            Index("shoppingListItemId"),
        ],
        primaryKeys = ["name", "value"],
        foreignKeys = [
            ForeignKey(
                entity = ShoppingListItemEntity::class,
                onDelete = ForeignKey.CASCADE,
                parentColumns = ["shoppinglist_id"],
                childColumns = ["shoppingListItemId"],
            ),
        ],
    )
    data class Attribute(
        override val name: String,
        override val value: String,
        val shoppingListItemId: Long,
    ) : AbstractAttribute()

    @Entity(
        tableName = "shoppinglist_brands",
        indices = [
            Index("name"),
            Index("shoppingListItemId"),
        ],
        primaryKeys = ["name", "shoppingListItemId"],
        foreignKeys = [
            ForeignKey(
                entity = ShoppingListItemEntity::class,
                onDelete = ForeignKey.CASCADE,
                parentColumns = ["shoppinglist_id"],
                childColumns = ["shoppingListItemId"],
            ),
        ],
    )
    data class Brand(override val name: String, val shoppingListItemId: Long) : AbstractBrand()

    val asUIInstance
        get() = ShoppingListItem(
            id = id,
            name = name,
            unit = unit,
            unitQuantity = unitQuantity,
            purchaseQuantity = purchaseQuantity,
            dateAdded = dateAdded,
            brands = emptyList(),
            attributes = emptyList(),
        )
}