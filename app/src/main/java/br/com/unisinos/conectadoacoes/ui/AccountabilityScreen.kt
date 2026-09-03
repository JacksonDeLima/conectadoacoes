package br.com.unisinos.conectadoacoes.ui

import android.content.Intent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import br.com.unisinos.conectadoacoes.data.Donation
import br.com.unisinos.conectadoacoes.data.Ngo
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
                                text = "Prestação de Contas & Balancete",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Auditoria social e conformidade com o MROSC (Lei 13.019)",
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
                                text = { Text("🌐 Todas as Entidades da Rede", fontWeight = FontWeight.Bold) },
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

                    Spacer(modifier = Modifier.height(12.dp))

                    // Botão Exportar Relatório Oficial (WhatsApp / E-mail / Impressão)
                    Button(
                        onClick = {
                            val reportText = buildAccountabilityReportText(
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
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Exportar / Compartilhar Balancete Social")
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
                    title = "Em Estoque",
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
                        text = "Inventário por Categoria de Bens",
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
                    text = "Livro de Registro de Doações (Auditoria)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${filteredDonations.size} registros",
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
                        text = "Nenhum registro de doação localizado para os filtros selecionados.",
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

    // Modal do Recibo Digital de Doação
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
                text = "Doador: ${donation.donorName} (${donation.donorNeighborhood}) • Entidade: ${donation.ngoName}",
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
                    text = "Data: ${formatDate(donation.createdAt)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedButton(
                    onClick = onOpenReceipt,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Recibo Digital", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

/**
 * Diálogo que renderiza o Comprovante/Recibo Digital de Doação
 */
@Composable
fun DigitalReceiptDialog(
    donation: Donation,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

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
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "CÓDIGO DE AUDITORIA: #${donation.trackingCode}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Divider()

                ReceiptRow(label = "Instituição Beneficiária:", value = donation.ngoName)
                ReceiptRow(label = "Cidadão Doador:", value = donation.donorName)
                ReceiptRow(label = "Bairro de Origem:", value = donation.donorNeighborhood)
                ReceiptRow(label = "Item Declarado:", value = donation.title)
                ReceiptRow(label = "Categoria de Bens:", value = donation.category)
                ReceiptRow(label = "Data de Registro:", value = formatDate(donation.createdAt))

                if (donation.status == Donation.STATUS_APPROVED || donation.status == Donation.STATUS_RECEIVED || donation.status == Donation.STATUS_DELIVERED) {
                    Divider()
                    ReceiptRow(label = "Acordo Logístico:", value = donation.logisticsType ?: "Ponto de Coleta")
                    ReceiptRow(label = "Triador Responsável:", value = donation.reviewedBy ?: "Equipe de Triagem")
                }

                if (donation.status == Donation.STATUS_RECEIVED || donation.status == Donation.STATUS_DELIVERED) {
                    ReceiptRow(label = "Entrada no Estoque:", value = "Confirmada (${formatDate(donation.receivedAt ?: donation.createdAt)})")
                }

                if (donation.status == Donation.STATUS_DELIVERED) {
                    Divider()
                    ReceiptRow(label = "Beneficiário Final:", value = donation.deliveredToBeneficiary ?: "Família Atendida")
                    ReceiptRow(label = "Data da Entrega:", value = formatDate(donation.deliveredAt ?: donation.createdAt))
                }

                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Documento registrado digitalmente pelo ConectaDoações para fins de prestação de contas (MROSC / Lei 13.019).",
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
                    val text = "🧾 RECIBO DIGITAL DE DOAÇÃO #${donation.trackingCode}\n\n" +
                            "Entidade: ${donation.ngoName}\n" +
                            "Doador: ${donation.donorName} (${donation.donorNeighborhood})\n" +
                            "Item: ${donation.title} (${donation.category})\n" +
                            "Status: ${donation.status}\n" +
                            "Data: ${formatDate(donation.createdAt)}\n\n" +
                            "ConectaDoações - Transparência e Solidariedade Comunitária."

                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, text)
                    }
                    context.startActivity(Intent.createChooser(intent, "Compartilhar Recibo Digital"))
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
 * Monta o texto oficial do balancete para compartilhamento com diretoria e conselhos.
 */
private fun buildAccountabilityReportText(
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
    val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    val sb = StringBuilder()
    sb.appendLine("📊 BALANCETE SOCIAL & PRESTAÇÃO DE CONTAS")
    sb.appendLine("Plataforma ConectaDoações • $date")
    sb.appendLine("Entidade: $ngoName")
    sb.appendLine("----------------------------------------")
    sb.appendLine("• Total de Doações Ofertadas: $total")
    sb.appendLine("• Em Análise / Pendentes: $pending")
    sb.appendLine("• Aprovadas / Agendadas: $approved")
    sb.appendLine("• Recebidas no Estoque Físico: $received")
    sb.appendLine("• Entregues a Famílias Beneficiárias: $delivered")
    sb.appendLine("• Incompatíveis / Recusadas: $rejected")
    sb.appendLine("• Taxa de Eficiência Solidária: $efficiency%")
    sb.appendLine("----------------------------------------")
    sb.appendLine("Últimos Registros com Código de Auditoria:")
    donations.take(5).forEach { d ->
        sb.appendLine("- #${d.trackingCode} | ${d.title} (${d.category}) -> Status: ${d.status}")
    }
    sb.appendLine("----------------------------------------")
    sb.appendLine("Relatório gerado em conformidade com o Marco Regulatório das Organizações da Sociedade Civil (Lei 13.019/2014).")
    return sb.toString()
}
