package br.com.unisinos.conectadoacoes.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import br.com.unisinos.conectadoacoes.data.Donation
import br.com.unisinos.conectadoacoes.data.Ngo
import br.com.unisinos.conectadoacoes.data.UrgentNeedEntity
import br.com.unisinos.conectadoacoes.data.Volunteer
import br.com.unisinos.conectadoacoes.ui.theme.*
import br.com.unisinos.conectadoacoes.util.LogisticsRouteHelper
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
    urgentNeeds: List<UrgentNeedEntity>,
    onApproveWithLogistics: (Donation, logisticsType: String, details: String, reviewerName: String) -> Unit,
    onRejectWithReason: (Donation, reason: String, reviewerName: String) -> Unit,
    onConfirmReceiptInStock: (Donation, receiverName: String) -> Unit,
    onDeliverToBeneficiary: (Donation, beneficiaryName: String, notes: String) -> Unit,
    onResetStatus: (Donation) -> Unit,
    onDeleteDonation: (Donation) -> Unit,
    onAddNewVolunteer: suspend (Volunteer) -> Unit,
    onAddNewNgo: suspend (Ngo) -> Unit,
    onAddNewUrgentNeed: suspend (UrgentNeedEntity) -> Unit,
    onDeleteUrgentNeed: suspend (UrgentNeedEntity) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Filtros de Status (Cadeia de Custódia Completa)
    var selectedFilter by remember { mutableStateOf("Todos") }
    val filterOptions = listOf(
        "Todos",
        Donation.STATUS_PENDING,
        Donation.STATUS_APPROVED,
        Donation.STATUS_RECEIVED,
        Donation.STATUS_DELIVERED,
        Donation.STATUS_REJECTED
    )

    // Filtro por Entidade Ativa
    var selectedNgoFilterId by remember { mutableStateOf<Long?>(null) }
    var ngoFilterDropdownExpanded by remember { mutableStateOf(false) }

    // Plantonista / Triador Ativo da Sessão
    var activeVolunteer by remember(volunteers) {
        mutableStateOf(volunteers.firstOrNull() ?: Volunteer(name = "Voluntário Plantonista", role = "Triagem Geral"))
    }
    var volunteerDropdownExpanded by remember { mutableStateOf(false) }

    // Modais e Diálogos
    var showAddVolunteerDialog by remember { mutableStateOf(false) }
    var showManageNeedsDialog by remember { mutableStateOf(false) }
    var showLookupCodeDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    var itemToApprove by remember { mutableStateOf<Donation?>(null) }
    var itemToReject by remember { mutableStateOf<Donation?>(null) }
    var itemToDeliver by remember { mutableStateOf<Donation?>(null) }
    var itemForReceiptModal by remember { mutableStateOf<Donation?>(null) }

    val activeNgo = remember(ngos, selectedNgoFilterId) {
        if (selectedNgoFilterId == null) ngos.firstOrNull() ?: Ngo.DEFAULT_NGOS.first()
        else ngos.firstOrNull { it.id == selectedNgoFilterId } ?: ngos.firstOrNull() ?: Ngo.DEFAULT_NGOS.first()
    }

    val activeNgoName = remember(selectedNgoFilterId, activeNgo) {
        if (selectedNgoFilterId == null) "Todas as Entidades Parceiras" else activeNgo.name
    }

    // Filtragem combinada por status, entidade e busca por código de auditoria
    val filteredList = remember(donations, selectedFilter, selectedNgoFilterId, searchQuery) {
        donations.filter { donation ->
            val matchesStatus = if (selectedFilter == "Todos") true else donation.status.equals(selectedFilter, ignoreCase = true)
            val matchesNgo = if (selectedNgoFilterId == null) true else donation.ngoId == selectedNgoFilterId
            val matchesSearch = if (searchQuery.isBlank()) true else {
                donation.trackingCode.contains(searchQuery, ignoreCase = true) ||
                        donation.title.contains(searchQuery, ignoreCase = true) ||
                        donation.donorName.contains(searchQuery, ignoreCase = true)
            }
            matchesStatus && matchesNgo && matchesSearch
        }
    }

    // Itens aprovados que precisam de coleta em domicílio
    val pendingPickups = remember(donations, selectedNgoFilterId) {
        donations.filter {
            it.status == Donation.STATUS_APPROVED &&
                    it.logisticsType == Donation.LOGISTICS_PICK_UP &&
                    (selectedNgoFilterId == null || it.ngoId == selectedNgoFilterId)
        }
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
                                text = { Text("Todas as Entidades Parceiras", fontWeight = FontWeight.Bold) },
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

                    Spacer(modifier = Modifier.width(8.dp))

                    // Botão para Gerenciar Vitrine de Demandas da ONG
                    FilledTonalIconButton(
                        onClick = { showManageNeedsDialog = true },
                        modifier = Modifier.size(52.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Campaign, contentDescription = "Gerenciar Demandas")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Linha 2: Seletor de Plantonista / Triador Ativo + Botão Adicionar Voluntário
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

                // Linha 3: Barra de Ação Logística Rápida (Rotas Google Maps & Busca por Código de Auditoria)
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Botão de Rotas no Google Maps
                    OutlinedButton(
                        onClick = {
                            if (pendingPickups.isEmpty()) {
                                Toast.makeText(context, "Não há coletas em domicílio agendadas no momento.", Toast.LENGTH_SHORT).show()
                            } else {
                                val stops = pendingPickups.map { "${it.donorNeighborhood}, São Leopoldo - RS" }
                                LogisticsRouteHelper.openMultiStopRoute(
                                    context = context,
                                    destinationAddress = activeNgo.address,
                                    stops = stops
                                )
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Icon(imageVector = Icons.Default.DirectionsCar, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Rota de Coletas (${pendingPickups.size})",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    // Botão de Busca por Código de Auditoria
                    OutlinedButton(
                        onClick = { showLookupCodeDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Buscar Código", style = MaterialTheme.typography.labelSmall)
                    }
                }

                if (searchQuery.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Filtro ativo: \"$searchQuery\"",
                                style = MaterialTheme.typography.bodySmall
                            )
                            IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(20.dp)) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Limpar busca")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Filtros de Status (Cadeia de Custódia)
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
                        onConfirmReceipt = {
                            onConfirmReceiptInStock(donation, "${activeVolunteer.name} (${activeVolunteer.role})")
                        },
                        onOpenDeliverDialog = { itemToDeliver = donation },
                        onOpenReceipt = { itemForReceiptModal = donation },
                        onOpenNavigation = {
                            LogisticsRouteHelper.openNavigationToLocation(
                                context = context,
                                locationQuery = "${donation.donorNeighborhood}, São Leopoldo - RS"
                            )
                        },
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

    // Modal de Registro de Entrega ao Beneficiário
    itemToDeliver?.let { donation ->
        DeliverToBeneficiaryDialog(
            donation = donation,
            onDismiss = { itemToDeliver = null },
            onConfirm = { beneficiaryName, notes ->
                onDeliverToBeneficiary(donation, beneficiaryName, notes)
                itemToDeliver = null
            }
        )
    }

    // Modal de Visualização de Recibo Digital com QR Code
    itemForReceiptModal?.let { donation ->
        DigitalReceiptDialog(
            donation = donation,
            onDismiss = { itemForReceiptModal = null }
        )
    }

    // Modal de Busca por Código de Auditoria
    if (showLookupCodeDialog) {
        var tempCode by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showLookupCodeDialog = false },
            title = { Text("Localizar por Código de Auditoria", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Digite o código impresso ou bipe o número do recibo (ex: CD-4812):",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = tempCode,
                        onValueChange = { tempCode = it },
                        label = { Text("Código de Auditoria") },
                        placeholder = { Text("CD-4812") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        searchQuery = tempCode.trim().replace("#", "")
                        showLookupCodeDialog = false
                    }
                ) {
                    Text("Filtrar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLookupCodeDialog = false }) {
                    Text("Cancelar")
                }
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

    // Modal de Gerenciamento da Vitrine de Demandas
    if (showManageNeedsDialog) {
        ManageNeedsDialog(
            activeNgo = activeNgo,
            needs = urgentNeeds.filter { it.ngoId == activeNgo.id },
            onDismiss = { showManageNeedsDialog = false },
            onAddNeed = { category, description, level ->
                coroutineScope.launch {
                    onAddNewUrgentNeed(
                        UrgentNeedEntity(
                            ngoId = activeNgo.id,
                            category = category,
                            description = description,
                            urgencyLevel = level
                        )
                    )
                }
            },
            onDeleteNeed = { need ->
                coroutineScope.launch {
                    onDeleteUrgentNeed(need)
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
    onConfirmReceipt: () -> Unit,
    onOpenDeliverDialog: () -> Unit,
    onOpenReceipt: () -> Unit,
    onOpenNavigation: () -> Unit,
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
            // Linha Superior: Código de Rastreio, Categoria e Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
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
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = donation.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                StatusBadge(status = donation.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Foto + Informações Principais
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

                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Entidade: ${donation.ngoName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

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

            // Destaques da Cadeia de Custódia
            if (donation.status == Donation.STATUS_APPROVED && donation.logisticsType != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = Color(0xFFF0FDF4),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Aprovado: ${donation.logisticsType}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D)
                            )
                            if (donation.logisticsType == Donation.LOGISTICS_PICK_UP) {
                                TextButton(
                                    onClick = onOpenNavigation,
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Map, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Ver no Maps", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                        if (!donation.logisticsDetails.isNullOrBlank()) {
                            Text(text = donation.logisticsDetails, style = MaterialTheme.typography.bodySmall, color = Color(0xFF166534))
                        }
                    }
                }
            } else if (donation.status == Donation.STATUS_RECEIVED) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = Color(0xFFF0F9FF),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBAE6FD)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Item em Estoque Físico no Galpão",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0369A1)
                        )
                        Text(
                            text = "Conferido por: ${donation.receivedBy ?: "Voluntário"} em ${formatTimestamp(donation.receivedAt ?: donation.createdAt)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF0C4A6E)
                        )
                    }
                }
            } else if (donation.status == Donation.STATUS_DELIVERED) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = Color(0xFFF0FDF4),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Ciclo Concluído: Entregue ao Beneficiário",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF15803D)
                        )
                        Text(
                            text = "Destinado a: ${donation.deliveredToBeneficiary ?: "Família Atendida"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF166534),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            } else if (donation.status == Donation.STATUS_REJECTED && !donation.rejectionReason.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = Color(0xFFFEF2F2),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Motivo da Recusa (Feedback Construtivo):",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF991B1B)
                        )
                        Text(text = donation.rejectionReason, style = MaterialTheme.typography.bodySmall, color = Color(0xFF7F1D1D))
                    }
                }
            }

            // WhatsApp do Doador
            if (donation.donorPhone.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable {
                            val cleanNumber = donation.donorPhone.replace("[^0-9]".toRegex(), "")
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/55$cleanNumber"))
                            context.startActivity(intent)
                        }
                        .padding(vertical = 2.dp)
                ) {
                    Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = Color(0xFF0D9488), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "WhatsApp: ${donation.donorPhone}",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF0D9488),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Ações de Custódia e Triagem
            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (donation.status) {
                    Donation.STATUS_PENDING -> {
                        Button(
                            onClick = onOpenApproveDialog,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Aprovar")
                        }

                        Button(
                            onClick = onOpenRejectDialog,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Recusar")
                        }
                    }

                    Donation.STATUS_APPROVED -> {
                        Button(
                            onClick = onConfirmReceipt,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Inventory, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Entrada Estoque")
                        }
                    }

                    Donation.STATUS_RECEIVED -> {
                        Button(
                            onClick = onOpenDeliverDialog,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.VolunteerActivism, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Entregar a Família")
                        }
                    }

                    else -> {
                        OutlinedButton(
                            onClick = onReset,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reavaliar")
                        }
                    }
                }

                // Botão Recibo e QR Code
                FilledTonalIconButton(
                    onClick = onOpenReceipt,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = "Recibo e QR Code",
                        tint = MaterialTheme.colorScheme.primary
                    )
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
 * Diálogo para a ONG gerenciar suas carências urgentes (adicionar e remover demandas da vitrine)
 */
@Composable
fun ManageNeedsDialog(
    activeNgo: Ngo,
    needs: List<UrgentNeedEntity>,
    onDismiss: () -> Unit,
    onAddNeed: (category: String, description: String, level: String) -> Unit,
    onDeleteNeed: (UrgentNeedEntity) -> Unit
) {
    var newCategory by remember { mutableStateOf(Donation.CATEGORIES.first()) }
    var newDescription by remember { mutableStateOf("") }
    var newLevel by remember { mutableStateOf("Urgente") }
    val levelOptions = listOf("Urgente", "Necessário", "Estoque Cheio")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Demandas Urgentes: ${activeNgo.name}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 450.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Cadastre itens prioritários para atualizar a vitrine exibida aos doadores:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = newDescription,
                    onValueChange = { newDescription = it },
                    label = { Text("Item / Descrição da Necessidade *") },
                    placeholder = { Text("Ex: Leite em pó integral, Cobertores") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    levelOptions.forEach { level ->
                        FilterChip(
                            selected = newLevel == level,
                            onClick = { newLevel = level },
                            label = { Text(level, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Button(
                    onClick = {
                        if (newDescription.isNotBlank()) {
                            onAddNeed(newCategory, newDescription.trim(), newLevel)
                            newDescription = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Adicionar à Vitrine")
                }

                Divider(modifier = Modifier.padding(vertical = 4.dp))

                Text(
                    text = "Demandas Ativas na Vitrine (${needs.size}):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                if (needs.isEmpty()) {
                    Text(
                        text = "Nenhuma demanda personalizada cadastrada para esta entidade.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    needs.forEach { need ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = need.description, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                    Text(text = "${need.category} • Nível: ${need.urgencyLevel}", style = MaterialTheme.typography.labelSmall)
                                }
                                IconButton(onClick = { onDeleteNeed(need) }, modifier = Modifier.size(28.dp)) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Concluir")
            }
        }
    )
}

/**
 * Diálogo para registrar a destinação final (entrega à família acolhida)
 */
@Composable
fun DeliverToBeneficiaryDialog(
    donation: Donation,
    onDismiss: () -> Unit,
    onConfirm: (beneficiaryName: String, notes: String) -> Unit
) {
    var beneficiaryName by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Registrar Entrega ao Beneficiário", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Fechamento de custódia do item \"${donation.title}\":",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = beneficiaryName,
                    onValueChange = { beneficiaryName = it; error = null },
                    label = { Text("Família ou Indivíduo Atendido *") },
                    placeholder = { Text("Ex: Família dos Santos (4 pessoas) - Bairro Feitoria") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Observações Sociais / Encaminhamento") },
                    placeholder = { Text("Ex: Atendimento via encaminhamento do CRAS Centro.") },
                    minLines = 2,
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
                    if (beneficiaryName.isBlank()) {
                        error = "Informe a identificação da família acolhida."
                        return@Button
                    }
                    onConfirm(beneficiaryName.trim(), notes.trim())
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
            ) {
                Text("Confirmar Entrega")
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
 * Diálogo de Aprovação com Acordo Logístico
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
        Donation.STATUS_RECEIVED -> Triple(Color(0xFFE0F2FE), Color(0xFF0369A1), Icons.Default.Inventory)
        Donation.STATUS_DELIVERED -> Triple(Color(0xFFDCFCE7), Color(0xFF15803D), Icons.Default.Verified)
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
            text = "As doações cadastradas pelos cidadãos aparecerão aqui para triagem visual, conferência de estoque e entrega final.",
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
