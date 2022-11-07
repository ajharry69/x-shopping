package co.ke.xently.shopping.libraries.data.source.local.daos

import androidx.paging.PagingSource
import androidx.room.*
import co.ke.xently.shopping.libraries.data.source.local.ProductEntity
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(vararg entities: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(entities: List<ProductEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveBrands(brands: List<ProductEntity.Brand>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAttributes(brands: List<ProductEntity.Attribute>)

    @Transaction
    @Query("SELECT * FROM products WHERE products_id = :id")
    fun get(id: Long): Flow<ProductEntity.WithRelated?>

    @Transaction
    @Query("SELECT * FROM products ORDER BY name")
    fun get(): PagingSource<Int, ProductEntity.WithRelated>

    @Transaction
    @Query("SELECT * FROM products WHERE dateAdded = :group ORDER BY name")
    fun get(group: Date): PagingSource<Int, ProductEntity.WithRelated>

    @Transaction
    @Query("SELECT * FROM products WHERE dateAdded = :group ORDER BY name")
    fun getList(group: Date): Flow<List<ProductEntity.WithRelated>>

    @Transaction
    @Query("SELECT * FROM products WHERE products_id = :id")
    fun getList(id: Long): Flow<List<ProductEntity.WithRelated>>

    @Query("SELECT * FROM products_brands WHERE LOWER(name) LIKE LOWER(:query)")
    fun getBrands(query: String): Flow<List<ProductEntity.Brand>>

    @Query("SELECT DISTINCT(name), * FROM products_brands WHERE LOWER(name) LIKE LOWER(:query)")
    fun getBrandsUniqueByName(query: String): Flow<List<ProductEntity.Brand>>

    @Query("SELECT * FROM products_attributes WHERE LOWER(name) = LOWER(:name) AND LOWER(value) LIKE LOWER(:value)")
    fun getAttributes(name: String, value: String): Flow<List<ProductEntity.Attribute>>

    @Query("SELECT DISTINCT(name) FROM products_attributes WHERE LOWER(name) LIKE LOWER(:query)")
    fun getUniqueAttributeNames(query: String): Flow<List<String>>

    @Query("SELECT DISTINCT(unit) FROM products WHERE LOWER(unit) LIKE LOWER(:query)")
    fun getUniqueMeasurementUnits(query: String): Flow<List<String>>

    @Query("DELETE FROM products WHERE products_id = :id")
    suspend fun delete(id: Long): Int

    @Query("DELETE FROM products")
    suspend fun deleteAll(): Int
}