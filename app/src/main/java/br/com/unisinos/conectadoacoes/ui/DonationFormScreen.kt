package br.com.unisinos.conectadoacoes.ui

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import br.com.unisinos.conectadoacoes.data.Donation
import br.com.unisinos.conectadoacoes.data.Ngo
import br.com.unisinos.conectadoacoes.data.UrgencyLevel
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonationFormScreen(
    ngos: List<Ngo>,
    urgentNeeds: List<br.com.unisinos.conectadoacoes.data.UrgentNeedEntity> = emptyList(),
    onSaveDonation: suspend (Donation) -> Unit,
    onAddNewNgo: suspend (Ngo) -> Unit,
    onSuccessNotification: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Entidade Selecionada (Doador escolhe o destino)
    var selectedNgo by remember(ngos) {
        mutableStateOf(ngos.firstOrNull() ?: Ngo.DEFAULT_NGOS.first())
    }
    var ngoDropdownExpanded by remember { mutableStateOf(false) }
    var showAddNgoDialog by remember { mutableStateOf(false) }

    // Identificação do Doador
    var donorName by remember { mutableStateOf("") }
    var donorNeighborhood by remember { mutableStateOf("") }
    var donorPhone by remember { mutableStateOf("") }

    // Dados do Item
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(Donation.CATEGORIES.first()) }
    var description by remember { mutableStateOf("") }
    var imageUriString by remember { mutableStateOf<String?>(null) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Photo Picker nativo do Android
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val persistentPath = copyUriToInternalStorage(context, uri)
            imageUriString = persistentPath ?: uri.toString()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Seletor Dinâmico de Entidade Beneficiária (Plataforma Multientidades)
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Apartment,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Entidade Beneficiária",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    FilledTonalButton(
                        onClick = { showAddNgoDialog = true },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Nova ONG", style = MaterialTheme.typography.labelSmall)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Menu seletor da ONG destino
                ExposedDropdownMenuBox(
                    expanded = ngoDropdownExpanded,
                    onExpandedChange = { ngoDropdownExpanded = !ngoDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedNgo.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Para qual ONG você deseja doar?") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = ngoDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = ngoDropdownExpanded,
                        onDismissRequest = { ngoDropdownExpanded = false }
                    ) {
                        ngos.forEach { ngo ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(text = ngo.name, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = "Foco: ${ngo.categoryFocus} • ${ngo.address}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {
                                    selectedNgo = ngo
                                    ngoDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Detalhes da ONG Selecionada
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Category,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Foco: ${selectedNgo.categoryFocus}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = selectedNgo.address,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Horário de recebimento: ${selectedNgo.operatingHours}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 2. Vitrine de Demanda Invertida ("O que a instituição parceira mais precisa")
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Necessidades Atuais da Rede",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Toque para sugerir",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            val displayedNeeds = remember(urgentNeeds, selectedNgo.id) {
                val ngoNeeds = urgentNeeds.filter { it.ngoId == selectedNgo.id }
                if (ngoNeeds.isNotEmpty()) {
                    ngoNeeds.map { need ->
                        val level = when (need.urgencyLevel.lowercase()) {
                            "alta", "urgente" -> UrgencyLevel.HIGH
                            "estoque cheio", "baixa" -> UrgencyLevel.LOW
                            else -> UrgencyLevel.MEDIUM
                        }
                        br.com.unisinos.conectadoacoes.data.UrgentNeed(need.category, need.description, level)
                    }
                } else {
                    Donation.URGENT_NEEDS
                }
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(displayedNeeds) { need ->
                    val (badgeBg, badgeText) = when (need.level) {
                        UrgencyLevel.HIGH -> Pair(Color(0xFFFEE2E2), Color(0xFFDC2626))
                        UrgencyLevel.MEDIUM -> Pair(Color(0xFFFEF3C7), Color(0xFFD97706))
                        UrgencyLevel.LOW -> Pair(Color(0xFFF1F5F9), Color(0xFF64748B))
                    }

                    OutlinedCard(
                        onClick = {
                            selectedCategory = need.category
                            if (title.isBlank()) {
                                title = need.description
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.width(220.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = need.category,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(
                                    color = badgeBg,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = need.level.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = badgeText,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = need.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 3. Seção: Identificação do Doador
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Dados do Doador (Para Logística)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = donorName,
                    onValueChange = { donorName = it },
                    label = { Text("Seu Nome *") },
                    placeholder = { Text("Ex: Maria da Silva") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = donorNeighborhood,
                        onValueChange = { donorNeighborhood = it },
                        label = { Text("Bairro / Cidade *") },
                        placeholder = { Text("Ex: Centro - São Leopoldo") },
                        singleLine = true,
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = donorPhone,
                        onValueChange = { donorPhone = it },
                        label = { Text("WhatsApp (Opcional)") },
                        placeholder = { Text("(51) 99999-9999") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        // 4. Seção: Dados do Item para Doação
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Item para Doação",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        errorMessage = null
                    },
                    label = { Text("Título do Item *") },
                    placeholder = { Text("Ex: Cesta Básica, Casaco de Lã Tam G, Mesa de Estudos") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = categoryDropdownExpanded,
                    onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoria *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        Donation.CATEGORIES.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    selectedCategory = category
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = {
                        description = it
                        errorMessage = null
                    },
                    label = { Text("Descrição e Condições de Uso *") },
                    placeholder = { Text("Descreva conservação, tamanho, peso aproximado ou prazo de validade (alimentos).") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Text(
                    text = "Foto do Item (Essencial para Triagem Visual)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )

                if (imageUriString != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                    ) {
                        AsyncImage(
                            model = imageUriString,
                            contentDescription = "Foto da doação",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        IconButton(
                            onClick = { imageUriString = null },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(50))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remover foto",
                                tint = Color.White
                            )
                        }
                    }
                } else {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = "Adicionar Foto",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Toque para anexar foto da galeria ou câmera",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        if (errorMessage != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Button(
            onClick = {
                if (title.isBlank()) {
                    errorMessage = "Por favor, informe o título do item."
                    return@Button
                }
                if (description.isBlank()) {
                    errorMessage = "Por favor, descreva as condições do item."
                    return@Button
                }
                val finalDonorName = if (donorName.isBlank()) "Doador Solidário" else donorName.trim()
                val finalDonorNeighborhood = if (donorNeighborhood.isBlank()) "Centro" else donorNeighborhood.trim()

                isSubmitting = true
                coroutineScope.launch {
                    val donation = Donation(
                        title = title.trim(),
                        category = selectedCategory,
                        description = description.trim(),
                        imageUri = imageUriString,
                        status = Donation.STATUS_PENDING,
                        ngoId = selectedNgo.id,
                        ngoName = selectedNgo.name,
                        donorName = finalDonorName,
                        donorNeighborhood = finalDonorNeighborhood,
                        donorPhone = donorPhone.trim()
                    )
                    onSaveDonation(donation)
                    onSuccessNotification("Doação enviada para triagem da ${selectedNgo.name}!")

                    // Reset form
                    title = ""
                    description = ""
                    imageUriString = null
                    selectedCategory = Donation.CATEGORIES.first()
                    errorMessage = null
                    isSubmitting = false
                }
            },
            enabled = !isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(imageVector = Icons.Default.VolunteerActivism, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isSubmitting) "Enviando..." else "Enviar para ${selectedNgo.name}",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }

    // Modal de Cadastro de Nova ONG Parceira
    if (showAddNgoDialog) {
        AddNgoDialog(
            onDismiss = { showAddNgoDialog = false },
            onConfirm = { newNgo ->
                coroutineScope.launch {
                    onAddNewNgo(newNgo)
                    selectedNgo = newNgo
                    showAddNgoDialog = false
                    onSuccessNotification("Nova entidade \"${newNgo.name}\" cadastrada com sucesso!")
                }
            }
        )
    }
}

/**
 * Diálogo para cadastrar dinamicamente uma nova entidade/ONG parceira.
 */
@Composable
fun AddNgoDialog(
    onDismiss: () -> Unit,
    onConfirm: (Ngo) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var categoryFocus by remember { mutableStateOf("Assistência Social & Alimentos") }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var hours by remember { mutableStateOf("Seg a Sex: 08h30 às 17h30") }
    var description by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Cadastrar Nova ONG Parceira", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; error = null },
                    label = { Text("Nome da Entidade *") },
                    placeholder = { Text("Ex: Associação Esperança Viva") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = categoryFocus,
                    onValueChange = { categoryFocus = it },
                    label = { Text("Foco de Atuação *") },
                    placeholder = { Text("Ex: Alimentos, Agasalhos, Idosos") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it; error = null },
                    label = { Text("Endereço da Sede *") },
                    placeholder = { Text("Ex: Rua Brasil, 100 - Bairro Centro") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("WhatsApp / Telefone") },
                    placeholder = { Text("(51) 98888-0000") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = hours,
                    onValueChange = { hours = it },
                    label = { Text("Horário de Recebimento") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrição / Missão") },
                    placeholder = { Text("Breve descrição do trabalho comunitário") },
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
                    if (name.isBlank() || address.isBlank()) {
                        error = "Preencha o nome e o endereço da entidade."
                        return@Button
                    }
                    onConfirm(
                        Ngo(
                            name = name.trim(),
                            categoryFocus = categoryFocus.trim(),
                            address = address.trim(),
                            phone = phone.trim(),
                            operatingHours = hours.trim(),
                            description = description.trim()
                        )
                    )
                }
            ) {
                Text("Salvar Entidade")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

private fun copyUriToInternalStorage(context: Context, sourceUri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(sourceUri) ?: return null
        val fileName = "donation_${System.currentTimeMillis()}.jpg"
        val outputFile = File(context.filesDir, fileName)
        val outputStream = FileOutputStream(outputFile)

        inputStream.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        outputFile.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
