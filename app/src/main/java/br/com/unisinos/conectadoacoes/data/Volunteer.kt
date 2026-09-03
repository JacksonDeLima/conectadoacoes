package br.com.unisinos.conectadoacoes.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representa um voluntário, triador ou responsável operacional vinculado a uma ONG.
 * Garante a rastreabilidade humana e a gestão dinâmica de plantonistas.
 *
 * @property id Identificador único gerado automaticamente pelo SQLite/Room.
 * @property ngoId Identificador da ONG à qual o voluntário pertence.
 * @property name Nome completo do voluntário ou profissional de triagem.
 * @property role Função desempenhada (ex: "Coordenador de Triagem", "Triador de Alimentos", "Plantão").
 * @property phone Telefone ou ramal de contato operacional.
 */
@Entity(tableName = "volunteers")
data class Volunteer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ngoId: Long = 1L,
    val name: String,
    val role: String,
    val phone: String = ""
) {
    companion object {
        // Voluntários de exemplo vinculados às entidades padrão
        val DEFAULT_VOLUNTEERS = listOf(
            Volunteer(id = 1L, ngoId = 1L, name = "Matheus Silveira", role = "Coordenador de Triagem", phone = "(51) 98111-2222"),
            Volunteer(id = 2L, ngoId = 1L, name = "Ana Clara Ramos", role = "Triadora de Rouparia", phone = "(51) 98222-3333"),
            Volunteer(id = 3L, ngoId = 2L, name = "Carlos Eduardo", role = "Inspetor de Alimentos & Nutrição", phone = "(51) 98333-4444"),
            Volunteer(id = 4L, ngoId = 3L, name = "Dra. Helena Costa", role = "Assistente Social de Plantão", phone = "(51) 98444-5555")
        )
    }
}
