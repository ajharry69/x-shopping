package co.ke.xently.shopping.libraries.data.source.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [
        Index("name"),
        Index("id", unique = true),
        Index("email", unique = true),
    ],
)
data class UserEntity(
    @ColumnInfo(name = "users_id")
    @PrimaryKey
    val localId: Int = 1,
    val id: Long,
    val name: String,
    val email: String,
    val shopsCount: Int,
    val isActive: Boolean,
    val isVerified: Boolean,
    val isPasswordResetRequested: Boolean,
)
