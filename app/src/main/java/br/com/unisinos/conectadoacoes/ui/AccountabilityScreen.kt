package br.com.unisinos.conectadoacoes.ui

import android.content.Intent
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import br.com.unisinos.conectadoacoes.data.Donation
import br.com.unisinos.conectadoacoes.data.Ngo
import br.com.unisinos.conectadoacoes.util.PdfReportGenerator
import br.com.unisinos.conectadoacoes.util.QrCodeGenerator
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountabilityScreen(
    donations: List<Donation>,
    ngos: List<Ngo>
) {
    val context = LocalContext.current

    // Filtro por Entidade
    var selectedNgoId by remember { mutableStateOf<Long?>(null) }
    var ngoDropdownExpanded by remember { mutableStateOf(false) }

    // Estado do Recibo Selecionado para Visualização
    var selectedDonationForReceipt by remember { mutableStateOf<Donation?>(null) }

    // Filtragem das doações para o relatório
    val filteredDonations = remember(donations, selectedNgoId) {
        if (selectedNgoId == null) donations
        else donations.filter { it.ngoId == selectedNgoId }
    }

    val selectedNgoName = remember(ngos, selectedNgoId) {
        if (selectedNgoId == null) "Todas as Entidades da Rede"
        else ngos.firstOrNull { it.id == selectedNgoId }?.name ?: "Entidade Parceira"
    }

    // Métricas Consolidadas
    val totalDonations = filteredDonations.size
    val pendingCount = filteredDonations.count { it.status == Donation.STATUS_PENDING }
    val approvedCount = filteredDonations.count { it.status == Donation.STATUS_APPROVED }
    val receivedInStockCount = filteredDonations.count { it.status == Donation.STATUS_RECEIVED }
    val deliveredCount = filteredDonations.count { it.status == Donation.STATUS_DELIVERED }
    val rejectedCount = filteredDonations.count { it.status == Donation.STATUS_REJECTED }

    val efficiencyRate = remember(totalDonations, rejectedCount) {
        if (totalDonations == 0) 100
        else (((totalDonations - rejectedCount).toDouble() / totalDonations) * 100).toInt()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Banner Institucional de Transparência e MROSC
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Assessment,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Prestação de Contas e Balancete Social",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Auditoria social em conformidade com o MROSC (Lei Federal 13.019/2014)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Seletor de Entidade
                    ExposedDropdownMenuBox(
                        expanded = ngoDropdownExpanded,
                        onExpandedChange = { ngoDropdownExpanded = !ngoDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedNgoName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Filtrar Balancete por Entidade") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = ngoDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = ngoDropdownExpanded,
                            onDismissRequest = { ngoDropdownExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Todas as Entidades da Rede", fontWeight = FontWeight.Bold) },
                                onClick = {
                                    selectedNgoId = null
                                    ngoDropdownExpanded = false
                                }
                            )
                            Divider()
                            ngos.forEach { ngo ->
                                DropdownMenuItem(
                                    text = { Text(ngo.name) },
                                    onClick = {
                                        selectedNgoId = ngo.id
                                        ngoDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Ações de Exportação Oficial
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Botão 1: Gerar PDF Nativo Oficial
                        Button(
                            onClick = {
                                val pdfFile = PdfReportGenerator.generateAndOpenPdf(
                                    context = context,
                                    ngoName = selectedNgoName,
                                    donations = filteredDonations
                                )
                                if (pdfFile != null) {
                                    PdfReportGenerator.openPdfFile(context, pdfFile)
                                    Toast.makeText(context, "Relatório em PDF gerado com sucesso!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Erro ao gerar arquivo PDF.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Gerar PDF Oficial", style = MaterialTheme.typography.labelMedium)
                        }

                        // Botão 2: Compartilhar Texto Institucional
                        OutlinedButton(
                            onClick = {
                                val reportText = buildFormalAccountabilityReportText(
                                    ngoName = selectedNgoName,
                                    total = totalDonations,
                                    pending = pendingCount,
                                    approved = approvedCount,
                                    received = receivedInStockCount,
                                    delivered = deliveredCount,
                                    rejected = rejectedCount,
                                    efficiency = efficiencyRate,
                                    donations = filteredDonations
                                )
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "Balancete Social - ConectaDoações")
                                    putExtra(Intent.EXTRA_TEXT, reportText)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Compartilhar Balancete Social"))
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Compartilhar Texto", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }

        // 2. Grid de Métricas Consolidadas (Cards com Indicadores)
        item {
            Text(
                text = "Indicadores de Cadeia de Custódia",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricKpiCard(
                    title = "Total de Ofertas",
                    value = totalDonations.toString(),
                    color = MaterialTheme.colorScheme.primary,
                    icon = Icons.Default.Inbox,
                    modifier = Modifier.weight(1f)
                )
                MetricKpiCard(
                    title = "Em Estoque Físico",
                    value = (approvedCount + receivedInStockCount).toString(),
                    color = Color(0xFF0284C7),
                    icon = Icons.Default.Inventory,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricKpiCard(
                    title = "Entregue a Famílias",
                    value = deliveredCount.toString(),
                    color = Color(0xFF16A34A),
                    icon = Icons.Default.VolunteerActivism,
                    modifier = Modifier.weight(1f)
                )
                MetricKpiCard(
                    title = "Eficiência Solidária",
                    value = "$efficiencyRate%",
                    color = Color(0xFFD97706),
                    icon = Icons.Default.TrendingUp,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 3. Distribuição por Categoria de Doação
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Inventário por Categoria de Materiais",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Donation.CATEGORIES.forEach { category ->
                        val count = filteredDonations.count { it.category == category }
                        val progress = if (totalDonations > 0) count.toFloat() / totalDonations else 0f

                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = category, style = MaterialTheme.typography.bodySmall)
                                Text(
                                    text = "$count itens (${(progress * 100).toInt()}%)",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 4. Livro de Registro & Auditoria de Itens (Rastreabilidade)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Livro de Registro e Auditoria",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${filteredDonations.size} registros arquivados",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (filteredDonations.isEmpty()) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Text(
                        text = "Nenhum registro localizado para os filtros selecionados.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }
        } else {
            items(filteredDonations, key = { it.id }) { donation ->
                AuditItemCard(
                    donation = donation,
                    onOpenReceipt = { selectedDonationForReceipt = donation }
                )
            }
        }
    }

    // Modal do Recibo Digital de Doação com QR Code
    selectedDonationForReceipt?.let { donation ->
        DigitalReceiptDialog(
            donation = donation,
            onDismiss = { selectedDonationForReceipt = null }
        )
    }
}

@Composable
fun MetricKpiCard(
    title: String,
    value: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun AuditItemCard(
    donation: Donation,
    onOpenReceipt: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "#${donation.trackingCode}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                StatusBadge(status = donation.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = donation.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Doador: ${donation.donorName} (${donation.donorNeighborhood}) | Entidade: ${donation.ngoName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!donation.deliveredToBeneficiary.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF16A34A),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Destinado a: ${donation.deliveredToBeneficiary}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF16A34A),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Registro: ${formatDate(donation.createdAt)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedButton(
                    onClick = onOpenReceipt,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Icon(imageVector = Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Recibo e QR Code", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

/**
 * Diálogo do Recibo Digital de Doação com QR Code 2D integrado
 */
@Composable
fun DigitalReceiptDialog(
    donation: Donation,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val qrBitmap = remember(donation.trackingCode) {
        QrCodeGenerator.generateQrBitmap(donation.trackingCode, 320)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Recibo Digital de Doação",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // QR Code 2D Renderizado
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "QR Code de Auditoria",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Text(
                    text = "CÓDIGO DE AUDITORIA: #${donation.trackingCode}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                Divider(modifier = Modifier.padding(vertical = 4.dp))

                ReceiptRow(label = "Instituição Beneficiária:", value = donation.ngoName)
                ReceiptRow(label = "Cidadão Doador:", value = donation.donorName)
                ReceiptRow(label = "Bairro de Origem:", value = donation.donorNeighborhood)
                ReceiptRow(label = "Item Declarado:", value = donation.title)
                ReceiptRow(label = "Categoria de Bens:", value = donation.category)
                ReceiptRow(label = "Data de Registro:", value = formatDate(donation.createdAt))

                if (donation.status == Donation.STATUS_APPROVED || donation.status == Donation.STATUS_RECEIVED || donation.status == Donation.STATUS_DELIVERED) {
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                    ReceiptRow(label = "Acordo Logístico:", value = donation.logisticsType ?: "Ponto de Coleta")
                    ReceiptRow(label = "Triador Responsável:", value = donation.reviewedBy ?: "Equipe de Triagem")
                }

                if (donation.status == Donation.STATUS_RECEIVED || donation.status == Donation.STATUS_DELIVERED) {
                    ReceiptRow(label = "Entrada no Estoque:", value = "Confirmada (${formatDate(donation.receivedAt ?: donation.createdAt)})")
                }

                if (donation.status == Donation.STATUS_DELIVERED) {
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                    ReceiptRow(label = "Beneficiário Final:", value = donation.deliveredToBeneficiary ?: "Família Atendida")
                    ReceiptRow(label = "Data da Entrega:", value = formatDate(donation.deliveredAt ?: donation.createdAt))
                }

                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Documento registrado digitalmente pela plataforma ConectaDoações para fins de comprovação e prestação de contas (MROSC / Lei 13.019/2014).",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val formalText = "RECIBO DIGITAL DE DOAÇÃO #${donation.trackingCode}\n" +
                            "--------------------------------------------------\n" +
                            "Instituição Beneficiária: ${donation.ngoName}\n" +
                            "Doador: ${donation.donorName} (${donation.donorNeighborhood})\n" +
                            "Item: ${donation.title} - Categoria: ${donation.category}\n" +
                            "Status da Cadeia de Custódia: ${donation.status}\n" +
                            "Data de Entrada: ${formatDate(donation.createdAt)}\n" +
                            "--------------------------------------------------\n" +
                            "Plataforma ConectaDoações | Registro Formal de Conformidade Social."

                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, formalText)
                    }
                    context.startActivity(Intent.createChooser(intent, "Compartilhar Recibo Oficial"))
                }
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Compartilhar Recibo")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar")
            }
        }
    )
}

@Composable
fun ReceiptRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

/**
 * Monta o texto oficial do balancete de forma estritamente formal e sóbria (sem emojis).
 */
private fun buildFormalAccountabilityReportText(
    ngoName: String,
    total: Int,
    pending: Int,
    approved: Int,
    received: Int,
    delivered: Int,
    rejected: Int,
    efficiency: Int,
    donations: List<Donation>
): String {
    val date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
    val sb = StringBuilder()
    sb.appendLine("RELATÓRIO DE PRESTAÇÃO DE CONTAS E BALANCETE SOCIAL")
    sb.appendLine("Plataforma ConectaDoações - Universidade do Vale do Rio dos Sinos (UNISINOS)")
    sb.appendLine("Marco Regulatório das Organizações da Sociedade Civil - Lei Federal nº 13.019/2014")
    sb.appendLine("----------------------------------------------------------------------")
    sb.appendLine("ENTIDADE BENEFICIÁRIA: $ngoName")
    sb.appendLine("DATA DE EMISSÃO: $date")
    sb.appendLine("----------------------------------------------------------------------")
    sb.appendLine("1. DEMONSTRATIVO CONSOLIDADO DO FLUXO DE BENS:")
    sb.appendLine("  - Total de Ofertas Registradas: $total")
    sb.appendLine("  - Ofertas em Triagem Inicial: $pending")
    sb.appendLine("  - Ofertas Aprovadas com Coleta/Entrega Agendada: $approved")
    sb.appendLine("  - Itens com Entrada Confirmada no Estoque Físico: $received")
    sb.appendLine("  - Doações Destinadas e Entregues a Famílias: $delivered")
    sb.appendLine("  - Ofertas Recusadas por Incompatibilidade Técnica: $rejected")
    sb.appendLine("  - Índice de Aproveitamento de Doações: $efficiency%")
    sb.appendLine("----------------------------------------------------------------------")
    sb.appendLine("2. REGISTROS AUDITADOS RECENTES (AMOSTRA DE CUSTÓDIA):")
    donations.take(8).forEach { d ->
        val dataStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(d.createdAt))
        val destStr = d.deliveredToBeneficiary ?: if (d.status == Donation.STATUS_REJECTED) "Recusado" else "Em estoque"
        sb.appendLine("  - [#${d.trackingCode}] $dataStr | ${d.title} (${d.category}) | Doador: ${d.donorName} | Destinação: $destStr | Status: ${d.status}")
    }
    sb.appendLine("----------------------------------------------------------------------")
    sb.appendLine("Declaração: As informações acima refletem com fidelidade os registros de entrada,")
    sb.appendLine("triagem e destinação operados pelo sistema ConectaDoações.")
    return sb.toString()
}
