package co.ke.xently.shopping.libraries.data.source.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import co.ke.xently.shopping.libraries.data.source.Customer

@Entity(
    tableName = "customers",
    indices = [
        Index("taxPin", unique = true),
        Index("name"),
        Index("phoneNumber"),
        Index("exemptionNumber"),
    ],
)
data class CustomerEntity(
    @ColumnInfo(name = "customers_id")
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val name: String,
    val taxPin: String,
    val physicalAddress: String?,
    val phoneNumber: String?,
    val exemptionNumber: String,
) {
    val asUIInstance
        get() = Customer(
            id = id,
            name = name,
            taxPin = taxPin,
            physicalAddress = physicalAddress,
            phoneNumber = phoneNumber,
            exemptionNumber = exemptionNumber,
        )
}
