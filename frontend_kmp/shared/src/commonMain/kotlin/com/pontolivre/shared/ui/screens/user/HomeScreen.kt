package com.pontolivre.shared.ui.screens.user

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pontolivre.shared.model.ParkingMeterModel
import com.pontolivre.shared.repository.AppSession
import com.pontolivre.shared.ui.components.*
import com.pontolivre.shared.util.format
import com.pontolivre.shared.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToActiveSession: () -> Unit,
    onNavigateToWallet: () -> Unit,
    onNavigateToMeterDetail: (String) -> Unit,
    onNavigateToVehicles: () -> Unit,
    onNavigateToClaimSession: (String) -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val user by AppSession.currentUser.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var sessionCode by remember { mutableStateOf("") }
    var addressQuery by remember { mutableStateOf("") }
    var isMapView by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.startPolling()
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.stopPolling() }
    }

    val displayMeters = remember(state.filteredMeters, searchQuery) {
        if (searchQuery.isBlank()) state.filteredMeters
        else state.filteredMeters.filter {
            it.code.contains(searchQuery, ignoreCase = true) ||
            it.description.orEmpty().contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            PontoLivreTopBar(
                title = "PontoLivre",
                actions = {
                    IconButton(onClick = onNavigateToWallet) {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Carteira")
                    }
                }
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {

            // Greeting + balance card
            user?.let { u ->
                Card(
                    onClick = onNavigateToProfile,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Olá, ${u.name.split(" ").first()}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold)
                            Text("Saldo disponível",
                                style = MaterialTheme.typography.bodySmall)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("R$ ${"%.2f".format(u.balance)}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold)
                            TextButton(onClick = onNavigateToWallet, contentPadding = PaddingValues(0.dp)) {
                                Text("Recarregar", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }

            // Quick actions row
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val hasActive = user != null && state.activeSession != null
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.DirectionsCar,
                    label = if (hasActive) "VER SESSÃO" else "Sessão Ativa",
                    containerColor = if (hasActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (hasActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    onClick = onNavigateToActiveSession
                )
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.DirectionsCar,
                    label = "Meus Veículos",
                    onClick = onNavigateToVehicles
                )
            }

            Spacer(Modifier.height(16.dp))

            // ADICIONAR CÓDIGO DE SESSÃO (DESIGN CORRIGIDO)
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    "Adicione o código de parquímetro:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = sessionCode,
                        onValueChange = { if (it.length <= 10) sessionCode = it.uppercase() },
                        placeholder = { Text("Ex: 123456") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 2.sp
                        ),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            imeAction = androidx.compose.ui.text.input.ImeAction.Go
                        ),
                        keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                            onGo = { if (sessionCode.isNotBlank()) onNavigateToClaimSession(sessionCode) }
                        )
                    )

                    Button(
                        onClick = { if (sessionCode.isNotBlank()) onNavigateToClaimSession(sessionCode) },
                        enabled = sessionCode.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(56.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp)
                    ) {
                        Text("Iniciar", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // TÍTULO DA SEÇÃO (Sempre visível)
            Text(
                "Sessão",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // INFO DA SESSÃO ATUAL
            if (state.activeSession != null) {
                val session = state.activeSession!!
                Card(
                    onClick = onNavigateToActiveSession,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                ) {
                    Row(
                        Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.DirectionsCar, null, tint = MaterialTheme.colorScheme.onPrimary)
                            }
                        }
                        Column(Modifier.weight(1f)) {
                            Text("Estacionado", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                            Text("Vaga: ${session.meterCode}", style = MaterialTheme.typography.bodyMedium)
                            Text("Início: ${session.startTime.substringAfter("T").take(5)}h", style = MaterialTheme.typography.bodySmall)
                        }
                        Icon(Icons.Default.ChevronRight, null)
                    }
                }
            } else {
                // Estado vazio quando não há sessão
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                ) {
                    Box(
                        Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Nenhuma sessão ativa",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            
            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
expect fun ParkingMapView(
    meters: List<ParkingMeterModel>,
    onMeterSelected: (String) -> Unit
)

@Composable
private fun QuickActionCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurfaceVariant,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null,
                tint = if (containerColor == MaterialTheme.colorScheme.primary) contentColor else MaterialTheme.colorScheme.primary, 
                modifier = Modifier.size(28.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, color = contentColor)
        }
    }
}

@Composable
fun MeterCard(meter: ParkingMeterModel, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.LocalParking, contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                Column {
                    Text(meter.code, style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold)
                    Text(meter.description ?: "Sem descrição",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            StatusBadge(meter.status)
        }
    }
}
