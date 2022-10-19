package co.ke.xently.shopping.libraries.data.source.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import co.ke.xently.shopping.libraries.data.source.local.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(entity: UserEntity)

    @Query("SELECT id FROM users WHERE isActive = 1")
    suspend fun getCurrentlyActiveUserID(): Long

    @Query("SELECT * FROM users WHERE isActive = 1 LIMIT 1")
    fun getAuthenticated(): Flow<UserEntity?>

    @Query("DELETE FROM users")
    suspend fun clear()
}