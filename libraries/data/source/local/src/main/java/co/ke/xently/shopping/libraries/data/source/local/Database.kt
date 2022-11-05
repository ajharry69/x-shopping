package co.ke.xently.shopping.libraries.data.source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import co.ke.xently.shopping.libraries.data.source.local.daos.RemoteKeyDao
import co.ke.xently.shopping.libraries.data.source.local.daos.ShopDao
import co.ke.xently.shopping.libraries.data.source.local.daos.ShoppingListDao
import co.ke.xently.shopping.libraries.data.source.local.daos.UserDao

@Database(
    entities = [
        RemoteKey::class,
        UserEntity::class,
        ShopEntity::class,
        ShoppingListItemEntity::class,
        ShoppingListItemEntity.Brand::class,
        ShoppingListItemEntity.Attribute::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(
    RoomTypeConverters.DateConverter::class,
)
abstract class Database : RoomDatabase() {
    abstract val userDao: UserDao
    abstract val shopDao: ShopDao
    abstract val shoppingListDao: ShoppingListDao
    abstract val remoteKeyDao: RemoteKeyDao
}