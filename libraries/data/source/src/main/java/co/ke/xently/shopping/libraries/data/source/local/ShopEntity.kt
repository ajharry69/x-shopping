package co.ke.xently.shopping.libraries.data.source.local

import androidx.room.*
import co.ke.xently.shopping.libraries.data.source.Coordinate
import co.ke.xently.shopping.libraries.data.source.Shop

@Entity(
    tableName = "shops",
    indices = [
        Index("taxPin", unique = true),
    ],
)
data class ShopEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "shops_id")
    val id: Long,
    val name: String,
    val taxPin: String,
    @Embedded(prefix = "shops_")
    val coordinates: Coordinate,
) {
    val asUIInstance
        get() = Shop(
            id = id,
            name = name,
            taxPin = taxPin,
            coordinates = coordinates,
        )
}