package br.com.unisinos.conectadoacoes.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representa uma organização ou entidade assistencial parceira cadastrada na plataforma.
 * Permite que o ConectaDoações funcione como uma plataforma aberta multientidades.
 *
 * @property id Identificador único gerado automaticamente pelo SQLite/Room.
 * @property name Nome da instituição (ex: "Casa de Acolhimento Amparo Social").
 * @property categoryFocus Foco principal de atendimento (ex: "Alimentos", "Roupas e Agasalhos").
 * @property description Missão ou público atendido pela instituição.
 * @property address Endereço completo da sede física para entrega e triagem de doações.
 * @property phone Contato telefônico ou WhatsApp institucional.
 * @property operatingHours Horários de atendimento para recebimento de doações.
 * @property isPartnerVerified Indica se a entidade é verificada pela rede social local.
 */
@Entity(tableName = "ngos")
data class Ngo(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val categoryFocus: String,
    val description: String,
    val address: String,
    val phone: String,
    val operatingHours: String,
    val isPartnerVerified: Boolean = true
) {
    companion object {
        // Dados de carga inicial padrão para demonstração acadêmica imediata
        val DEFAULT_NGOS = listOf(
            Ngo(
                id = 1L,
                name = "Casa de Acolhimento Amparo Social",
                categoryFocus = "Assistência Social & Agasalhos",
                description = "Acolhimento temporário de famílias e indivíduos em situação de vulnerabilidade.",
                address = "Rua das Oliveiras, 450 - Centro, São Leopoldo/RS",
                phone = "(51) 98765-4321",
                operatingHours = "Seg a Sex: 08h30 às 17h30",
                isPartnerVerified = true
            ),
            Ngo(
                id = 2L,
                name = "Banco Comunitário de Alimentos Vale do Sinos",
                categoryFocus = "Alimentos Não Perecíveis",
                description = "Captação e redistribuição de alimentos para cozinhas comunitárias e creches.",
                address = "Av. das Indústrias, 1200 - Scharlau, São Leopoldo/RS",
                phone = "(51) 99123-4567",
                operatingHours = "Seg a Sáb: 08h00 às 16h00",
                isPartnerVerified = true
            ),
            Ngo(
                id = 3L,
                name = "Lar São Vicente de Paulo (Idosos)",
                categoryFocus = "Higiene, Saúde & Conforto",
                description = "Instituição de longa permanência para pessoas idosas com assistência multiprofissional.",
                address = "Rua Independência, 890 - Rio Branco, São Leopoldo/RS",
                phone = "(51) 98888-7777",
                operatingHours = "Diariamente: 09h00 às 18h00",
                isPartnerVerified = true
            )
        )
    }
}
