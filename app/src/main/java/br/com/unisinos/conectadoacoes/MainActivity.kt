package br.com.unisinos.conectadoacoes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.unisinos.conectadoacoes.data.AppDatabase
import br.com.unisinos.conectadoacoes.data.Donation
import br.com.unisinos.conectadoacoes.data.Ngo
import br.com.unisinos.conectadoacoes.data.Volunteer
import br.com.unisinos.conectadoacoes.ui.DonationFormScreen
import br.com.unisinos.conectadoacoes.ui.DonationListScreen
import br.com.unisinos.conectadoacoes.ui.theme.ConectaDoacoesTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)

        setContent {
            ConectaDoacoesTheme {
                MainAppScreen(
                    database = database
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(database: AppDatabase) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val donationDao = remember { database.donationDao() }
    val ngoDao = remember { database.ngoDao() }
    val volunteerDao = remember { database.volunteerDao() }

    // Coletas reativas em tempo real a partir do Room Flow
    val donations by donationDao.getAllDonations().collectAsState(initial = emptyList())
    val ngos by ngoDao.getAllNgos().collectAsState(initial = Ngo.DEFAULT_NGOS)
    val volunteers by volunteerDao.getAllVolunteers().collectAsState(initial = Volunteer.DEFAULT_VOLUNTEERS)

    // Aba ativa: 0 = Formulário do Doador, 1 = Painel de Triagem da ONG
    var selectedTab by remember { mutableIntStateOf(0) }

    val pendingCount = remember(donations) {
        donations.count { it.status.equals(Donation.STATUS_PENDING, ignoreCase = true) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "ConectaDoações",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (selectedTab == 0) "Visão do Doador • Rede Multientidades" else "Visão da ONG • Triagem & Equipe",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.VolunteerActivism,
                            contentDescription = "Quero Doar"
                        )
                    },
                    label = { Text("Quero Doar") }
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (pendingCount > 0) {
                                    Badge {
                                        Text(pendingCount.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Assignment,
                                contentDescription = "Triagem ONG"
                            )
                        }
                    },
                    label = { Text("Triagem ONG") }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> {
                    DonationFormScreen(
                        ngos = ngos,
                        onSaveDonation = { donation ->
                            donationDao.insertDonation(donation)
                        },
                        onAddNewNgo = { newNgo ->
                            ngoDao.insertNgo(newNgo)
                        },
                        onSuccessNotification = { message ->
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    message = message,
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    )
                }
                1 -> {
                    DonationListScreen(
                        donations = donations,
                        ngos = ngos,
                        volunteers = volunteers,
                        onApproveWithLogistics = { donation, logisticsType, details, reviewerName ->
                            coroutineScope.launch {
                                val updated = donation.copy(
                                    status = Donation.STATUS_APPROVED,
                                    logisticsType = logisticsType,
                                    logisticsDetails = details,
                                    reviewedBy = reviewerName,
                                    reviewedAt = System.currentTimeMillis()
                                )
                                donationDao.updateDonation(updated)
                                snackbarHostState.showSnackbar(
                                    message = "Doação aprovada com acordo de $logisticsType!",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        },
                        onRejectWithReason = { donation, reason, reviewerName ->
                            coroutineScope.launch {
                                val updated = donation.copy(
                                    status = Donation.STATUS_REJECTED,
                                    rejectionReason = reason,
                                    reviewedBy = reviewerName,
                                    reviewedAt = System.currentTimeMillis()
                                )
                                donationDao.updateDonation(updated)
                                snackbarHostState.showSnackbar(
                                    message = "Recusa registrada com feedback construtivo ao doador.",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        },
                        onResetStatus = { donation ->
                            coroutineScope.launch {
                                val updated = donation.copy(
                                    status = Donation.STATUS_PENDING,
                                    rejectionReason = null,
                                    logisticsType = null,
                                    logisticsDetails = null,
                                    reviewedBy = null,
                                    reviewedAt = null
                                )
                                donationDao.updateDonation(updated)
                                snackbarHostState.showSnackbar(
                                    message = "Item retornado para o status Pendente.",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        },
                        onDeleteDonation = { donation ->
                            coroutineScope.launch {
                                donationDao.deleteDonation(donation)
                                snackbarHostState.showSnackbar(
                                    message = "Doação removida do sistema.",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        },
                        onAddNewVolunteer = { newVolunteer ->
                            volunteerDao.insertVolunteer(newVolunteer)
                            snackbarHostState.showSnackbar(
                                message = "Novo triador ${newVolunteer.name} cadastrado!",
                                duration = SnackbarDuration.Short
                            )
                        },
                        onAddNewNgo = { newNgo ->
                            ngoDao.insertNgo(newNgo)
                            snackbarHostState.showSnackbar(
                                message = "Nova ONG ${newNgo.name} cadastrada!",
                                duration = SnackbarDuration.Short
                            )
                        }
                    )
                }
            }
        }
    }
}
