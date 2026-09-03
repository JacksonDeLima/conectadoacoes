package br.com.unisinos.conectadoacoes.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import br.com.unisinos.conectadoacoes.data.Donation
import br.com.unisinos.conectadoacoes.data.Ngo
import br.com.unisinos.conectadoacoes.data.Volunteer
import br.com.unisinos.conectadoacoes.ui.theme.*
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonationListScreen(
    donations: List<Donation>,
    ngos: List<Ngo>,
    volunteers: List<Volunteer>,
    onApproveWithLogistics: (Donation, logisticsType: String, details: String, reviewerName: String) -> Unit,
    onRejectWithReason: (Donation, reason: String, reviewerName: String) -> Unit,
    onResetStatus: (Donation) -> Unit,
    onDeleteDonation: (Donation) -> Unit,
    onAddNewVolunteer: suspend (Volunteer) -> Unit,
    onAddNewNgo: suspend (Ngo) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    // Filtro por Status
    var selectedFilter by remember { mutableStateOf("Todos") }
    val filterOptions = listOf("Todos", Donation.STATUS_PENDING, Donation.STATUS_APPROVED, Donation.STATUS_REJECTED)

    // Filtro por Entidade Ativa (Todas ou ONG específica)
    var selectedNgoFilterId by remember { mutableStateOf<Long?>(null) } // null = todas
    var ngoFilterDropdownExpanded by remember { mutableStateOf(false) }

    // Plantonista / Triador Ativo da Sessão
    var activeVolunteer by remember(volunteers) {
        mutableStateOf(volunteers.firstOrNull() ?: Volunteer(name = "Voluntário Plantonista", role = "Triagem Geral"))
    }
    var volunteerDropdownExpanded by remember { mutableStateOf(false) }

    // Modais
    var showAddVolunteerDialog by remember { mutableStateOf(false) }
    var itemToApprove by remember { mutableStateOf<Donation?>(null) }
    var itemToReject by remember { mutableStateOf<Donation?>(null) }

    val filteredList = remember(donations, selectedFilter, selectedNgoFilterId) {
        donations.filter { donation ->
            val matchesStatus = if (selectedFilter == "Todos") true else donation.status.equals(selectedFilter, ignoreCase = true)
            val matchesNgo = if (selectedNgoFilterId == null) true else donation.ngoId == selectedNgoFilterId
            matchesStatus && matchesNgo
        }
    }

    val activeNgoName = remember(ngos, selectedNgoFilterId) {
        if (selectedNgoFilterId == null) "Todas as Entidades Parceiras"
        else ngos.firstOrNull { it.id == selectedNgoFilterId }?.name ?: "Entidade Parceira"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Painel Superior de Gestão da Entidade e Equipe
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Linha 1: Seletor de ONG Ativa
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ExposedDropdownMenuBox(
                        expanded = ngoFilterDropdownExpanded,
                        onExpandedChange = { ngoFilterDropdownExpanded = !ngoFilterDropdownExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = activeNgoName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Filtrar por Entidade") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = ngoFilterDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = ngoFilterDropdownExpanded,
                            onDismissRequest = { ngoFilterDropdownExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("🌐 Todas as Entidades Parceiras", fontWeight = FontWeight.Bold) },
                                onClick = {
                                    selectedNgoFilterId = null
                                    ngoFilterDropdownExpanded = false
                                }
                            )
                            Divider()
                            ngos.forEach { ngo ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(ngo.name, fontWeight = FontWeight.SemiBold)
                                            Text(ngo.categoryFocus, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    },
                                    onClick = {
                                        selectedNgoFilterId = ngo.id
                                        ngoFilterDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Linha 2: Seletor de Plantonista / Triador Ativo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ExposedDropdownMenuBox(
                        expanded = volunteerDropdownExpanded,
                        onExpandedChange = { volunteerDropdownExpanded = !volunteerDropdownExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = "${activeVolunteer.name} (${activeVolunteer.role})",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Triador de Plantão Ativo") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = volunteerDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = volunteerDropdownExpanded,
                            onDismissRequest = { volunteerDropdownExpanded = false }
                        ) {
                            volunteers.forEach { volunteer ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(volunteer.name, fontWeight = FontWeight.Bold)
                                            Text(volunteer.role, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                        }
                                    },
                                    onClick = {
                                        activeVolunteer = volunteer
                                        volunteerDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    FilledTonalIconButton(
                        onClick = { showAddVolunteerDialog = true },
                        modifier = Modifier.size(52.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PersonAdd, contentDescription = "Adicionar Voluntário")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Filtros de Status
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(filterOptions) { filter ->
                        val count = if (filter == "Todos") {
                            filteredList.size
                        } else {
                            filteredList.count { it.status.equals(filter, ignoreCase = true) }
                        }
                        val isSelected = selectedFilter == filter

                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilter = filter },
                            label = {
                                Text(
                                    text = "$filter ($count)",
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null,
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }
            }
        }

        // Lista de Doações
        if (filteredList.isEmpty()) {
            EmptyTriageState(filter = selectedFilter)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredList, key = { it.id }) { donation ->
                    DonationCardProfessional(
                        donation = donation,
                        onOpenApproveDialog = { itemToApprove = donation },
                        onOpenRejectDialog = { itemToReject = donation },
                        onReset = { onResetStatus(donation) },
                        onDelete = { onDeleteDonation(donation) }
                    )
                }
            }
        }
    }

    // Modal de Aprovação com Acordo Logístico
    itemToApprove?.let { donation ->
        val destinationNgo = ngos.firstOrNull { it.id == donation.ngoId } ?: ngos.firstOrNull() ?: Ngo.DEFAULT_NGOS.first()
        ApprovalLogisticsDialog(
            donation = donation,
            ngo = destinationNgo,
            defaultReviewerName = "${activeVolunteer.name} (${activeVolunteer.role})",
            onDismiss = { itemToApprove = null },
            onConfirm = { logisticsType, details, reviewer ->
                onApproveWithLogistics(donation, logisticsType, details, reviewer)
                itemToApprove = null
            }
        )
    }

    // Modal de Recusa com Justificativa Construtiva
    itemToReject?.let { donation ->
        RejectionReasonDialog(
            donation = donation,
            defaultReviewerName = "${activeVolunteer.name} (${activeVolunteer.role})",
            onDismiss = { itemToReject = null },
            onConfirm = { reason, reviewer ->
                onRejectWithReason(donation, reason, reviewer)
                itemToReject = null
            }
        )
    }

    // Modal para Cadastrar Novo Voluntário
    if (showAddVolunteerDialog) {
        AddVolunteerDialog(
            ngos = ngos,
            initialNgoId = selectedNgoFilterId ?: ngos.firstOrNull()?.id ?: 1L,
            onDismiss = { showAddVolunteerDialog = false },
            onConfirm = { newVolunteer ->
                coroutineScope.launch {
                    onAddNewVolunteer(newVolunteer)
                    activeVolunteer = newVolunteer
                    showAddVolunteerDialog = false
                }
            }
        )
    }
}

@Composable
fun DonationCardProfessional(
    donation: Donation,
    onOpenApproveDialog: () -> Unit,
    onOpenRejectDialog: () -> Unit,
    onReset: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Linha Superior: Categoria, Entidade Destino e Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${donation.category} • ${donation.ngoName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                StatusBadge(status = donation.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Conteúdo: Foto + Informações
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!donation.imageUri.isNullOrBlank()) {
                    AsyncImage(
                        model = donation.imageUri,
                        contentDescription = "Foto da doação",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(85.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(85.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = donation.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${donation.donorName} (${donation.donorNeighborhood})",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = donation.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = if (expanded) 10 else 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Seção de Feedback da Decisão (Quando Aprovado ou Recusado)
            if (donation.status == Donation.STATUS_APPROVED && donation.logisticsType != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = Color(0xFFF0FDF4),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (donation.logisticsType == Donation.LOGISTICS_PICK_UP) Icons.Default.LocalShipping else Icons.Default.Storefront,
                                contentDescription = null,
                                tint = Color(0xFF16A34A),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = donation.logisticsType,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D)
                            )
                        }
                        if (!donation.logisticsDetails.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = donation.logisticsDetails,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF166534)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Aprovado por: ${donation.reviewedBy ?: "Equipe da ONG"} • ${formatTimestamp(donation.reviewedAt ?: donation.createdAt)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF15803D).copy(alpha = 0.8f)
                        )
                    }
                }
            } else if (donation.status == Donation.STATUS_REJECTED && !donation.rejectionReason.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = Color(0xFFFEF2F2),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Motivo da Recusa (Feedback Construtivo):",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF991B1B)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = donation.rejectionReason,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF7F1D1D)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Triado por: ${donation.reviewedBy ?: "Equipe da ONG"} • ${formatTimestamp(donation.reviewedAt ?: donation.createdAt)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF991B1B).copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Botão de Contato WhatsApp (Se informado pelo doador)
            if (donation.donorPhone.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable {
                            val cleanNumber = donation.donorPhone.replace("[^0-9]".toRegex(), "")
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/55$cleanNumber"))
                            context.startActivity(intent)
                        }
                        .padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Contato WhatsApp",
                        tint = Color(0xFF0D9488),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Contato do Doador: ${donation.donorPhone}",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF0D9488),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (donation.status == Donation.STATUS_PENDING) {
                    Button(
                        onClick = onOpenApproveDialog,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Aprovar")
                    }

                    Button(
                        onClick = onOpenRejectDialog,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Recusar")
                    }
                } else {
                    OutlinedButton(
                        onClick = onReset,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reavaliar Item")
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Excluir",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Diálogo de Aprovação com Acordo Logístico dinâmico baseado na ONG destinatária
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApprovalLogisticsDialog(
    donation: Donation,
    ngo: Ngo,
    defaultReviewerName: String,
    onDismiss: () -> Unit,
    onConfirm: (logisticsType: String, details: String, reviewerName: String) -> Unit
) {
    var selectedType by remember { mutableStateOf(Donation.LOGISTICS_DROP_OFF) }
    var details by remember { mutableStateOf("Entregar na sede de ${ngo.name} (${ngo.address}) no horário: ${ngo.operatingHours}.") }
    var reviewerName by remember { mutableStateOf(defaultReviewerName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Aprovar Doação & Definir Logística",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Defina como o item \"${donation.title}\" será recebido por ${ngo.name}:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedType = Donation.LOGISTICS_DROP_OFF
                                details = "Entregar na sede de ${ngo.name} (${ngo.address}) no horário: ${ngo.operatingHours}."
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = selectedType == Donation.LOGISTICS_DROP_OFF,
                            onClick = {
                                selectedType = Donation.LOGISTICS_DROP_OFF
                                details = "Entregar na sede de ${ngo.name} (${ngo.address}) no horário: ${ngo.operatingHours}."
                            }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Entrega no Ponto (Doador leva)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedType = Donation.LOGISTICS_PICK_UP
                                details = "Coleta agendada pela equipe de ${ngo.name} para o bairro ${donation.donorNeighborhood}."
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = selectedType == Donation.LOGISTICS_PICK_UP,
                            onClick = {
                                selectedType = Donation.LOGISTICS_PICK_UP
                                details = "Coleta agendada pela equipe de ${ngo.name} para o bairro ${donation.donorNeighborhood}."
                            }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Coleta em Domicílio (ONG busca)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = { Text("Instruções Logísticas para o Doador") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = reviewerName,
                    onValueChange = { reviewerName = it },
                    label = { Text("Triador / Responsável") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedType, details.trim(), reviewerName.trim()) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
            ) {
                Text("Confirmar Aprovação")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * Diálogo de Recusa com Justificativa Construtiva
 */
@Composable
fun RejectionReasonDialog(
    donation: Donation,
    defaultReviewerName: String,
    onDismiss: () -> Unit,
    onConfirm: (reason: String, reviewerName: String) -> Unit
) {
    var selectedReason by remember { mutableStateOf(Donation.REJECTION_REASONS.first()) }
    var customNote by remember { mutableStateOf("") }
    var reviewerName by remember { mutableStateOf(defaultReviewerName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Recusa Construtiva da Doação",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Selecione o motivo da incompatibilidade para orientar o doador com clareza e respeito:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Donation.REJECTION_REASONS.forEach { reason ->
                        val isSelected = selectedReason == reason
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedReason = reason }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedReason = reason }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = reason,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = customNote,
                    onValueChange = { customNote = it },
                    label = { Text("Nota Adicional / Orientação ao Doador (Opcional)") },
                    placeholder = { Text("Ex: Indicar cooperativas de reciclagem parceiras.") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = reviewerName,
                    onValueChange = { reviewerName = it },
                    label = { Text("Triador / Responsável") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalReason = if (customNote.isNotBlank()) "$selectedReason — Obs: ${customNote.trim()}" else selectedReason
                    onConfirm(finalReason, reviewerName.trim())
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
            ) {
                Text("Confirmar Recusa")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * Diálogo para cadastrar um novo voluntário/triador na equipe
 */
@Composable
fun AddVolunteerDialog(
    ngos: List<Ngo>,
    initialNgoId: Long,
    onDismiss: () -> Unit,
    onConfirm: (Volunteer) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Triador de Doações") }
    var phone by remember { mutableStateOf("") }
    var selectedNgoId by remember { mutableLongStateOf(initialNgoId) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Novo Voluntário / Triador", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; error = null },
                    label = { Text("Nome Completo *") },
                    placeholder = { Text("Ex: Juliana Mendes") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text("Função / Cargo *") },
                    placeholder = { Text("Ex: Triador de Rouparia, Nutricionista") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Telefone / Ramal") },
                    placeholder = { Text("(51) 98000-1111") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (error != null) {
                    Text(text = error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        error = "Informe o nome do voluntário."
                        return@Button
                    }
                    onConfirm(
                        Volunteer(
                            name = name.trim(),
                            role = role.trim(),
                            phone = phone.trim(),
                            ngoId = selectedNgoId
                        )
                    )
                }
            ) {
                Text("Cadastrar Voluntário")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun StatusBadge(status: String) {
    val (bgColor, textColor, icon) = when (status) {
        Donation.STATUS_APPROVED -> Triple(StatusApprovedBg, StatusApprovedText, Icons.Default.CheckCircle)
        Donation.STATUS_REJECTED -> Triple(StatusRejectedBg, StatusRejectedText, Icons.Default.Cancel)
        else -> Triple(StatusPendingBg, StatusPendingText, Icons.Default.AccessTime)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = status,
                style = MaterialTheme.typography.labelSmall,
                color = textColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun EmptyTriageState(filter: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AssignmentTurnedIn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (filter == "Todos") "Nenhuma doação encontrada" else "Nenhum item com status \"$filter\"",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "As doações enviadas pelos cidadãos aparecerão aqui para triagem visual e agendamento da equipe.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
