package br.com.unisinos.conectadoacoes.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) para operações de banco de dados na tabela de doações.
 * Utiliza Kotlin Coroutines (suspend functions) e Flow para reatividade em tempo real.
 */
@Dao
interface DonationDao {

    /**
     * Retorna todas as doações cadastradas ordenadas da mais recente para a mais antiga.
     */
    @Query("SELECT * FROM donations ORDER BY createdAt DESC")
    fun getAllDonations(): Flow<List<Donation>>

    /**
     * Retorna doações direcionadas para uma entidade específica.
     */
    @Query("SELECT * FROM donations WHERE ngoId = :ngoId ORDER BY createdAt DESC")
    fun getDonationsByNgo(ngoId: Long): Flow<List<Donation>>

    /**
     * Retorna doações filtradas pelo status especificado ("Pendente", "Aprovado", "Recusado").
     */
    @Query("SELECT * FROM donations WHERE status = :status ORDER BY createdAt DESC")
    fun getDonationsByStatus(status: String): Flow<List<Donation>>

    /**
     * Busca uma doação específica pelo seu ID.
     */
    @Query("SELECT * FROM donations WHERE id = :id LIMIT 1")
    suspend fun getDonationById(id: Long): Donation?

    /**
     * Insere uma nova doação no banco SQLite.
     * Retorna o ID gerado pelo banco.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDonation(donation: Donation): Long

    /**
     * Atualiza os dados de uma doação existente (ex: mudança de status pelo voluntário da ONG).
     */
    @Update
    suspend fun updateDonation(donation: Donation)

    /**
     * Remove uma doação do banco de dados.
     */
    @Delete
    suspend fun deleteDonation(donation: Donation)
}
