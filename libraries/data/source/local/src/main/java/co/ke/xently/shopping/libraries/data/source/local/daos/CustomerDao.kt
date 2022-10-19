package co.ke.xently.shopping.libraries.data.source.local.daos

import androidx.room.*
import co.ke.xently.shopping.libraries.data.source.local.CustomerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Insert
    suspend fun add(vararg entities: CustomerEntity)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(entity: CustomerEntity)

    @Query("SELECT * FROM customers ORDER BY name, customers_id, taxPin")
    fun get(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE LOWER(name) LIKE LOWER(:query) OR LOWER(taxPin) LIKE LOWER(:query) OR LOWER(phoneNumber) LIKE LOWER(:query) ORDER BY name, customers_id, taxPin")
    fun get(query: String): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE customers_id = :id")
    fun get(id: Int): Flow<CustomerEntity?>

    @Query("DELETE FROM customers WHERE customers_id = :id")
    suspend fun remove(id: Int)
}