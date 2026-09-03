package br.com.unisinos.conectadoacoes.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) para gerenciamento dinâmico das necessidades urgentes das ONGs.
 */
@Dao
interface UrgentNeedDao {

    @Query("SELECT * FROM urgent_needs WHERE ngoId = :ngoId AND isActive = 1 ORDER BY id DESC")
    fun getActiveNeedsByNgo(ngoId: Long): Flow<List<UrgentNeedEntity>>

    @Query("SELECT * FROM urgent_needs WHERE isActive = 1 ORDER BY id DESC")
    fun getAllActiveNeeds(): Flow<List<UrgentNeedEntity>>

    @Query("SELECT COUNT(*) FROM urgent_needs")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(need: UrgentNeedEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(needs: List<UrgentNeedEntity>)

    @Update
    suspend fun update(need: UrgentNeedEntity)

    @Delete
    suspend fun delete(need: UrgentNeedEntity)
}
