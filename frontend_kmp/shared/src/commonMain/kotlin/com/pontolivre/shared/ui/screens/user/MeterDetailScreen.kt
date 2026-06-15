package com.pontolivre.shared.ui.screens.user

import androidx.compose.foundation.layout.*
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
import com.pontolivre.shared.model.ParkingMeterModel
import com.pontolivre.shared.ui.components.*
import com.pontolivre.shared.viewmodel.SessionViewModel
import com.pontolivre.shared.api.ParkingMeterApi
import com.pontolivre.shared.api.ApiResult

@Composable
fun MeterDetailScreen(
    meterId: String,
    onNavigateUp: () -> Unit
) {
    var meter by remember { mutableStateOf<ParkingMeterModel?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(meterId) {
        isLoading = true
        when (val result = ParkingMeterApi.getById(meterId)) {
            is ApiResult.Success -> {
                meter = result.data
                isLoading = false
            }
            is ApiResult.Error -> {
                error = result.message
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            PontoLivreTopBar(
                title = "Detalhes da Vaga",
                onNavigateUp = onNavigateUp
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            if (isLoading) {
                FullScreenLoading()
            } else if (error != null) {
                ErrorMessage(error!!, onRetry = { /* reload */ })
            } else {
                        meter?.let { m ->
                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Meter Info Card
                        Card(
                            Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(m.code, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                                    StatusBadge(m.status)
                                }
                                Text(m.description ?: "Sem descrição", style = MaterialTheme.typography.bodyMedium)
                                
                                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                                
                                InfoRow("Status da Vaga", if(m.status == "FREE") "Disponível" else "Ocupada", Icons.Default.Info)
                                InfoRow("Localização", "${m.latitude ?: 0.0}, ${m.longitude ?: 0.0}", Icons.Default.LocationOn)

                                // Mini Mapa na tela de detalhes
                                Spacer(Modifier.height(8.dp))
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                ) {
                                    ParkingMapView(
                                        meters = listOf(m),
                                        onMeterSelected = {} // Desativado no mini-mapa
                                    )
                                }
                            }
                        }

                        if (m.status == "FREE") {
                            Spacer(Modifier.weight(1f))
                            
                            Card(
                                Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    "Atenção: Para utilizar esta vaga, dirija o seu veículo até ela. O parquímetro gerará um código que você deve inserir na tela inicial.",
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                            
                            Spacer(Modifier.weight(1f))

                            Button(
                                onClick = onNavigateUp,
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Voltar")
                            }
                        } else {
                            Box(Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                                Text("Esta vaga já está ocupada.", style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            }
        }
    }
}
