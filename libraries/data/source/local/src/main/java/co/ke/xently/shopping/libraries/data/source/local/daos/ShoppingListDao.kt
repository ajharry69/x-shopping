package co.ke.xently.shopping.libraries.data.source.local.daos

import androidx.paging.PagingSource
import androidx.room.*
import co.ke.xently.shopping.libraries.data.source.GroupedShoppingListCount
import co.ke.xently.shopping.libraries.data.source.local.ShoppingListItemEntity
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface ShoppingListDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(vararg items: ShoppingListItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(items: List<ShoppingListItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveBrands(brands: List<ShoppingListItemEntity.Brand>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAttributes(brands: List<ShoppingListItemEntity.Attribute>)

    @Transaction
    @Query("SELECT * FROM shoppinglist WHERE shoppinglist_id = :id")
    fun get(id: Long): Flow<ShoppingListItemEntity.WithRelated?>

    @Transaction
    @Query("SELECT * FROM shoppinglist ORDER BY name")
    fun get(): PagingSource<Int, ShoppingListItemEntity.WithRelated>

    @Transaction
    @Query("SELECT * FROM shoppinglist WHERE dateAdded = :group ORDER BY name")
    fun get(group: Date): PagingSource<Int, ShoppingListItemEntity.WithRelated>

    @Transaction
    @Query("SELECT * FROM shoppinglist WHERE dateAdded = :group ORDER BY name")
    fun getList(group: Date): Flow<List<ShoppingListItemEntity.WithRelated>>

    @Transaction
    @Query("SELECT * FROM shoppinglist WHERE shoppinglist_id = :id")
    fun getList(id: Long): Flow<List<ShoppingListItemEntity.WithRelated>>

    @Query("SELECT * FROM shoppinglist_brands WHERE LOWER(name) LIKE LOWER(:query)")
    fun getBrands(query: String): Flow<List<ShoppingListItemEntity.Brand>>

    @Query("SELECT DISTINCT(name), * FROM shoppinglist_brands WHERE LOWER(name) LIKE LOWER(:query)")
    fun getBrandsUniqueByName(query: String): Flow<List<ShoppingListItemEntity.Brand>>

    @Query("SELECT dateAdded AS `group`, COUNT(dateAdded) AS numberOfItems FROM shoppinglist GROUP BY dateAdded")
    fun getCountGroupedByDateAdded(): Flow<List<GroupedShoppingListCount>>

    @Query("DELETE FROM shoppinglist WHERE shoppinglist_id = :id")
    suspend fun delete(id: Long): Int

    @Query("DELETE FROM shoppinglist")
    suspend fun deleteAll(): Int
}