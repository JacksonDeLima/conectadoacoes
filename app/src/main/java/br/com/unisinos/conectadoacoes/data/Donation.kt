package br.com.unisinos.conectadoacoes.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representa uma doação cadastrada no aplicativo ConectaDoações com rastreabilidade
 * completa de ponta a ponta (quem doa, para qual ONG, quem avalia, acordo logístico e justificativa).
 *
 * @property id Identificador único gerado automaticamente pelo SQLite/Room.
 * @property title Nome ou resumo do item ofertado para doação.
 * @property category Categoria do item (Alimentos, Roupas, Móveis, etc.).
 * @property description Detalhes sobre o estado de conservação, tamanho ou especificações.
 * @property imageUri Caminho local da foto persistida no armazenamento privado do dispositivo.
 * @property status Estado da triagem ("Pendente", "Aprovado" ou "Recusado").
 * @property createdAt Timestamp em milissegundos da criação para ordenação cronológica.
 * @property ngoId Identificador da entidade de destino selecionada pelo doador.
 * @property ngoName Nome da entidade de destino.
 * @property donorName Nome do cidadão doador.
 * @property donorNeighborhood Bairro/Região do doador para planejamento de rotas de coleta.
 * @property donorPhone Telefone ou WhatsApp para contato operacional.
 * @property reviewedBy Nome ou setor do voluntário responsável pela triagem.
 * @property reviewedAt Timestamp da decisão de aprovação ou recusa.
 * @property rejectionReason Justificativa construtiva e padronizada em caso de recusa.
 * @property logisticsType Modalidade logística acordada ("Entrega no Ponto de Coleta" ou "Coleta em Domicílio").
 * @property logisticsDetails Instruções específicas de agendamento, turno ou orientações de entrega.
 */
@Entity(tableName = "donations")
data class Donation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String,
    val description: String,
    val imageUri: String? = null,
    val status: String = STATUS_PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val ngoId: Long = 1L,
    val ngoName: String = "Casa de Acolhimento Amparo Social",
    val donorName: String = "Doador Solidário",
    val donorNeighborhood: String = "Centro",
    val donorPhone: String = "",
    val reviewedBy: String? = null,
    val reviewedAt: Long? = null,
    val rejectionReason: String? = null,
    val logisticsType: String? = null,
    val logisticsDetails: String? = null
) {
    companion object {
        const val STATUS_PENDING = "Pendente"
        const val STATUS_APPROVED = "Aprovado"
        const val STATUS_REJECTED = "Recusado"

        const val LOGISTICS_DROP_OFF = "Entrega no Ponto (Doador leva)"
        const val LOGISTICS_PICK_UP = "Coleta em Domicílio (ONG busca)"

        val CATEGORIES = listOf(
            "Alimentos",
            "Roupas e Calçados",
            "Móveis e Eletros",
            "Higiene e Limpeza",
            "Brinquedos e Livros",
            "Outros"
        )

        // Motivos pré-formatados de recusa construtiva (IHC - Prevenção de frustração)
        val REJECTION_REASONS = listOf(
            "Capacidade de estoque esgotada para esta categoria no momento",
            "Item necessita de reparos que excedem a capacidade técnica da entidade",
            "Alimento perecível com validade expirada ou embalagem violada",
            "Volume ou peso incompatível com o veículo de coleta disponível",
            "Não atendemos a essa categoria específica no momento"
        )

        // Necessidades urgentes sinalizadas pelas entidades (Demanda Invertida)
        val URGENT_NEEDS = listOf(
            UrgentNeed("Alimentos", "Leite em pó e alimentos não perecíveis", UrgencyLevel.HIGH),
            UrgentNeed("Roupas e Calçados", "Agasalhos e cobertores infantis", UrgencyLevel.HIGH),
            UrgentNeed("Higiene e Limpeza", "Sabonetes, fraldas geriátricas e pasta", UrgencyLevel.MEDIUM),
            UrgentNeed("Móveis e Eletros", "Móveis grandes temporariamente suspensos", UrgencyLevel.LOW)
        )
    }
}

enum class UrgencyLevel(val label: String) {
    HIGH("Urgente"),
    MEDIUM("Necessário"),
    LOW("Estoque Cheio")
}

data class UrgentNeed(
    val category: String,
    val description: String,
    val level: UrgencyLevel
)
