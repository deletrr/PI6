package com.pontolivre.shared.ui.screens.user

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pontolivre.shared.ui.components.*
import com.pontolivre.shared.viewmodel.SessionViewModel
import com.pontolivre.shared.viewmodel.VehicleViewModel

@Composable
fun VehicleSelectionScreen(
    sessionCode: String,
    sessionViewModel: SessionViewModel,
    vehicleViewModel: VehicleViewModel,
    onSuccess: () -> Unit,
    onNavigateUp: () -> Unit
) {
    val vehicleState by vehicleViewModel.state.collectAsState()
    val sessionState by sessionViewModel.state.collectAsState()

    LaunchedEffect(Unit) { vehicleViewModel.loadVehicles() }

    Scaffold(
        topBar = { PontoLivreTopBar(title = "Selecionar Veículo", onNavigateUp = onNavigateUp) }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("Escolha o veículo para a sessão $sessionCode",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp))

            if (vehicleState.isLoading) {
                FullScreenLoading()
            } else if (vehicleState.vehicles.isEmpty()) {
                EmptyState("Cadastre um veículo primeiro.", Icons.Default.DirectionsCar)
                Button(onClick = onNavigateUp, modifier = Modifier.fillMaxWidth()) {
                    Text("Voltar")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(vehicleState.vehicles) { vehicle ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable {
                                sessionViewModel.claimSession(sessionCode, vehicle.id, onSuccess)
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(16.dp))
                                Column {
                                    Text(vehicle.model, fontWeight = FontWeight.Bold)
                                    Text("Placa: ${vehicle.plate}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }

            if (sessionState.isStarting) {
                Spacer(Modifier.height(16.dp))
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            sessionState.error?.let {
                Spacer(Modifier.height(16.dp))
                ErrorMessage(it)
            }
        }
    }
}
