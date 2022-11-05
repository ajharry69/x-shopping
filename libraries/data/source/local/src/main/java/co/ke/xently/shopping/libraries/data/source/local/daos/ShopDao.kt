package co.ke.xently.shopping.libraries.data.source.local.daos

import androidx.paging.PagingSource
import androidx.room.*
import co.ke.xently.shopping.libraries.data.source.local.ShopEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShopDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(vararg shops: ShopEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(shops: List<ShopEntity>)

    @Transaction
    @Query("SELECT * FROM shops WHERE shops_id = :id")
    fun get(id: Long): Flow<ShopEntity?>

    @Transaction
    @Query("SELECT * FROM shops ORDER BY name, taxPin")
    fun get(): PagingSource<Int, ShopEntity>

    @Query("SELECT * FROM shops WHERE (LOWER(name) LIKE LOWER(:query)) OR (LOWER(taxPin) LIKE LOWER(:query))")
    fun get(query: String): Flow<List<ShopEntity>>

    @Query("DELETE FROM shops WHERE shops_id = :id")
    suspend fun delete(id: Long): Int

    @Query("DELETE FROM shops")
    suspend fun deleteAll(): Int
}