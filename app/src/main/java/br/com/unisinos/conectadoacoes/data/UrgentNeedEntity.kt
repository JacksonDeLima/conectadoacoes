package br.com.unisinos.conectadoacoes.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representa uma necessidade ou carência urgente cadastrada e gerenciada dinamicamente
 * pela equipe da entidade parceira (Demanda Invertida).
 *
 * @property id Identificador único gerado pelo banco.
 * @property ngoId Identificador da ONG que declarou a carência.
 * @property category Categoria do material (Alimentos, Roupas, Higiene, etc.).
 * @property description Descrição detalhada da carência (ex: "Leite em pó integral e alimentos não perecíveis").
 * @property urgencyLevel Nível de criticidade ("Urgente", "Necessário", "Estoque Cheio").
 * @property isActive Indica se a demanda está ativa na vitrine do doador.
 */
@Entity(tableName = "urgent_needs")
data class UrgentNeedEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ngoId: Long,
    val category: String,
    val description: String,
    val urgencyLevel: String = "Urgente",
    val isActive: Boolean = true
) {
    companion object {
        val DEFAULT_NEEDS = listOf(
            UrgentNeedEntity(ngoId = 1L, category = "Roupas e Calçados", description = "Agasalhos e cobertores infantis", urgencyLevel = "Urgente"),
            UrgentNeedEntity(ngoId = 1L, category = "Móveis e Eletros", description = "Móveis de grande porte temporariamente suspensos", urgencyLevel = "Estoque Cheio"),
            UrgentNeedEntity(ngoId = 2L, category = "Alimentos", description = "Leite em pó integral, arroz e óleo de cozinha", urgencyLevel = "Urgente"),
            UrgentNeedEntity(ngoId = 2L, category = "Alimentos", description = "Macarrão e feijão", urgencyLevel = "Necessário"),
            UrgentNeedEntity(ngoId = 3L, category = "Higiene e Limpeza", description = "Fraldas geriátricas tamanhos G e GG", urgencyLevel = "Urgente"),
            UrgentNeedEntity(ngoId = 3L, category = "Higiene e Limpeza", description = "Sabonete líquido e hidratante corporal", urgencyLevel = "Necessário")
        )
    }
}
