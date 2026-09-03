package br.com.unisinos.conectadoacoes.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) para operações de banco de dados na tabela de ONGs.
 */
@Dao
interface NgoDao {

    @Query("SELECT * FROM ngos ORDER BY name ASC")
    fun getAllNgos(): Flow<List<Ngo>>

    @Query("SELECT * FROM ngos WHERE id = :id LIMIT 1")
    suspend fun getNgoById(id: Long): Ngo?

    @Query("SELECT COUNT(*) FROM ngos")
    suspend fun getNgoCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNgo(ngo: Ngo): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(ngos: List<Ngo>)

    @Update
    suspend fun updateNgo(ngo: Ngo)

    @Delete
    suspend fun deleteNgo(ngo: Ngo)
}
