package br.com.unisinos.conectadoacoes.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) para operações de banco de dados na tabela de voluntários e triadores.
 */
@Dao
interface VolunteerDao {

    @Query("SELECT * FROM volunteers ORDER BY name ASC")
    fun getAllVolunteers(): Flow<List<Volunteer>>

    @Query("SELECT * FROM volunteers WHERE ngoId = :ngoId ORDER BY name ASC")
    fun getVolunteersByNgo(ngoId: Long): Flow<List<Volunteer>>

    @Query("SELECT COUNT(*) FROM volunteers")
    suspend fun getVolunteerCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVolunteer(volunteer: Volunteer): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(volunteers: List<Volunteer>)

    @Update
    suspend fun updateVolunteer(volunteer: Volunteer)

    @Delete
    suspend fun deleteVolunteer(volunteer: Volunteer)
}
