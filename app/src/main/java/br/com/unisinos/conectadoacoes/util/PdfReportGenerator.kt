package br.com.unisinos.conectadoacoes.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import br.com.unisinos.conectadoacoes.data.Donation
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Gerador de Relatório Oficial de Prestação de Contas em formato PDF nativo.
 * Formatação estritamente corporativa, sóbria e acadêmica (sem emojis),
 * em conformidade com o Marco Regulatório das Organizações da Sociedade Civil (Lei 13.019/2014).
 */
object PdfReportGenerator {

    fun generateAndOpenPdf(
        context: Context,
        ngoName: String,
        donations: List<Donation>
    ): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Padrão A4 (595x842 pontos)
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paintTitle = Paint().apply {
            color = Color.BLACK
            textSize = 12f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }

        val paintSubtitle = Paint().apply {
            color = Color.DKGRAY
            textSize = 9f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }

        val paintSectionHeader = Paint().apply {
            color = Color.rgb(20, 20, 20)
            textSize = 10f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }

        val paintText = Paint().apply {
            color = Color.BLACK
            textSize = 8.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }

        val paintTextBold = Paint().apply {
            color = Color.BLACK
            textSize = 8.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }

        val paintLine = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 0.8f
        }

        val paintDarkLine = Paint().apply {
            color = Color.BLACK
            strokeWidth = 1.2f
        }

        var y = 45f
        val marginX = 40f
        val rightX = 555f

        // Cabeçalho Institucional
        canvas.drawText("UNIVERSIDADE DO VALE DO RIO DOS SINOS - UNISINOS", marginX, y, paintSubtitle)
        y += 13f
        canvas.drawText("CURSO DE ANÁLISE E DESENVOLVIMENTO DE SISTEMAS - CAMPUS SÃO LEOPOLDO", marginX, y, paintSubtitle)
        y += 18f
        canvas.drawText("RELATÓRIO DE PRESTAÇÃO DE CONTAS E BALANCETE SOCIAL", marginX, y, paintTitle)
        y += 14f
        canvas.drawText("Marco Regulatório das Organizações da Sociedade Civil - Lei Federal nº 13.019/2014", marginX, y, paintSubtitle)
        y += 8f
        canvas.drawLine(marginX, y, rightX, y, paintDarkLine)
        y += 16f

        // 1. Dados da Entidade e Emissão
        val emissaoDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText("1. IDENTIFICAÇÃO DA ENTIDADE E PERÍODO", marginX, y, paintSectionHeader)
        y += 13f
        canvas.drawText("Entidade Beneficiária: $ngoName", marginX, y, paintText)
        canvas.drawText("Data de Emissão: $emissaoDate", 370f, y, paintText)
        y += 11f
        canvas.drawText("Sistema Emissor: Plataforma ConectaDoações (Ambiente de Extensão Comunitária)", marginX, y, paintText)
        y += 15f
        canvas.drawLine(marginX, y, rightX, y, paintLine)
        y += 16f

        // 2. Demonstrativo Sintético de Custódia
        val total = donations.size
        val pending = donations.count { it.status == Donation.STATUS_PENDING }
        val approved = donations.count { it.status == Donation.STATUS_APPROVED }
        val received = donations.count { it.status == Donation.STATUS_RECEIVED }
        val delivered = donations.count { it.status == Donation.STATUS_DELIVERED }
        val rejected = donations.count { it.status == Donation.STATUS_REJECTED }
        val efficiency = if (total > 0) (((total - rejected).toDouble() / total) * 100).toInt() else 100

        canvas.drawText("2. DEMONSTRATIVO CONSOLIDADO DE FLUXO DE BENS", marginX, y, paintSectionHeader)
        y += 13f

        // Tabela Sintética
        val colW = (rightX - marginX) / 4
        canvas.drawText("Total de Ofertas: $total", marginX, y, paintText)
        canvas.drawText("Em Análise / Pendente: $pending", marginX + colW, y, paintText)
        canvas.drawText("Aprovados / Coleta: $approved", marginX + colW * 2, y, paintText)
        canvas.drawText("Recebidos no Estoque: $received", marginX + colW * 3, y, paintText)
        y += 12f
        canvas.drawText("Entregues a Famílias: $delivered", marginX, y, paintTextBold)
        canvas.drawText("Recusados / Incompatíveis: $rejected", marginX + colW, y, paintText)
        canvas.drawText("Índice de Aproveitamento: $efficiency%", marginX + colW * 2, y, paintTextBold)
        y += 15f
        canvas.drawLine(marginX, y, rightX, y, paintLine)
        y += 16f

        // 3. Inventário por Categoria de Bens
        canvas.drawText("3. DISTRIBUIÇÃO FÍSICA POR CATEGORIA DE MATERIAIS", marginX, y, paintSectionHeader)
        y += 13f
        val catSummary = Donation.CATEGORIES.joinToString("   |   ") { cat ->
            val count = donations.count { it.category == cat }
            "$cat: $count"
        }
        canvas.drawText(catSummary, marginX, y, paintText)
        y += 15f
        canvas.drawLine(marginX, y, rightX, y, paintLine)
        y += 16f

        // 4. Livro de Auditoria e Cadeia de Custódia (Últimos Registros)
        canvas.drawText("4. LIVRO DE REGISTRO E RASTREABILIDADE DE DOAÇÕES (AMOSTRA AUDITADA)", marginX, y, paintSectionHeader)
        y += 14f

        // Cabeçalho da Tabela
        canvas.drawText("CÓDIGO", marginX, y, paintTextBold)
        canvas.drawText("DATA", marginX + 60f, y, paintTextBold)
        canvas.drawText("DOADOR / BAIRRO", marginX + 120f, y, paintTextBold)
        canvas.drawText("ITEM DECLARADO", marginX + 240f, y, paintTextBold)
        canvas.drawText("STATUS", marginX + 370f, y, paintTextBold)
        canvas.drawText("DESTINAÇÃO", marginX + 445f, y, paintTextBold)
        y += 6f
        canvas.drawLine(marginX, y, rightX, y, paintDarkLine)
        y += 12f

        // Linhas de Doação
        val sample = donations.take(14)
        for (item in sample) {
            val dateStr = SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(Date(item.createdAt))
            val donorStr = truncate(item.donorName + " (" + item.donorNeighborhood + ")", 20)
            val titleStr = truncate(item.title + " - " + item.category, 22)
            val statusStr = truncate(item.status, 14)
            val destStr = truncate(item.deliveredToBeneficiary ?: if (item.status == Donation.STATUS_REJECTED) "Recusado" else "Em estoque", 16)

            canvas.drawText("#" + item.trackingCode, marginX, y, paintText)
            canvas.drawText(dateStr, marginX + 60f, y, paintText)
            canvas.drawText(donorStr, marginX + 120f, y, paintText)
            canvas.drawText(titleStr, marginX + 240f, y, paintText)
            canvas.drawText(statusStr, marginX + 370f, y, paintText)
            canvas.drawText(destStr, marginX + 445f, y, paintText)
            y += 13f
        }

        y = 740f
        canvas.drawLine(marginX, y, rightX, y, paintLine)
        y += 16f

        // 5. Termo de Encerramento e Assinaturas
        canvas.drawText("5. DECLARAÇÃO DE VERACIDADE E RESPONSABILIDADE SOCIAL", marginX, y, paintSectionHeader)
        y += 12f
        canvas.drawText(
            "Declaramos para os devidos fins de prestação de contas que os registros acima refletem com exatidão a entrada,",
            marginX, y, paintSubtitle
        )
        y += 10f
        canvas.drawText(
            "triagem e destinação das doações voluntárias recebidas no âmbito da cooperação comunitária.",
            marginX, y, paintSubtitle
        )
        y += 38f

        // Linhas de Assinatura
        val signLineW = 200f
        canvas.drawLine(marginX, y, marginX + signLineW, y, paintLine)
        canvas.drawLine(355f, y, 355f + signLineW, y, paintLine)
        y += 12f
        canvas.drawText("Responsável pela Triagem / Assistente Social", marginX + 10f, y, paintText)
        canvas.drawText("Representação Legal da Entidade / Diretoria", 365f, y, paintText)

        pdfDocument.finishPage(page)

        // Gravação do Arquivo no Armazenamento do Dispositivo
        return try {
            val documentsDir = context.getExternalFilesDir("documents") ?: context.filesDir
            if (!documentsDir.exists()) documentsDir.mkdirs()
            val fileName = "Balancete_Social_${System.currentTimeMillis()}.pdf"
            val file = File(documentsDir, fileName)
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDocument.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }

    /**
     * Abre o visualizador ou compartilhador nativo do PDF.
     */
    fun openPdfFile(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback para Intent de compartilhamento genérico se não houver leitor de PDF padrão
            try {
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
                context.startActivity(Intent.createChooser(shareIntent, "Compartilhar Relatório em PDF"))
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    private fun truncate(text: String, maxLength: Int): String {
        return if (text.length <= maxLength) text else text.substring(0, maxLength - 2) + ".."
    }
}
