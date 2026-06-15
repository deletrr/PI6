package com.pontolivre.shared.ui.screens.user

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
import com.pontolivre.shared.viewmodel.VehicleViewModel
import kotlinx.coroutines.delay

@Composable
fun VehiclesScreen(viewModel: VehicleViewModel, onNavigateUp: () -> Unit) {
    val state by viewModel.state.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadVehicles()
        while(true) {
            delay(30_000)
            viewModel.loadVehicles()
        }
    }

    Scaffold(
        topBar = { PontoLivreTopBar(title = "Meus Veículos", onNavigateUp = onNavigateUp) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Veículo")
            }
        }
    ) { padding ->
        if (state.isLoading) {
            FullScreenLoading()
        } else if (state.vehicles.isEmpty()) {
            EmptyState("Nenhum veículo cadastrado.", Icons.Default.DirectionsCar)
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.vehicles) { vehicle ->
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                        Row(
                            Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(vehicle.model, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("Placa: ${vehicle.plate}", style = MaterialTheme.typography.bodyMedium)
                                Text("Cor: ${vehicle.color}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { viewModel.deleteVehicle(vehicle.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var model by remember { mutableStateOf("") }
        var plate by remember { mutableStateOf("") }
        var color by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Novo Veículo") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = model, onValueChange = { model = it }, label = { Text("Modelo (Ex: Civic)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = plate, onValueChange = { plate = it.uppercase() }, label = { Text("Placa (Ex: ABC-1234)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = color, onValueChange = { color = it }, label = { Text("Cor (Ex: Prata)") }, modifier = Modifier.fillMaxWidth())
                    state.error?.let { ErrorMessage(it) }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addVehicle(model, plate, color) {
                            showAddDialog = false
                            model = ""; plate = ""; color = ""
                        }
                    },
                    enabled = !state.isSaving
                ) {
                    if (state.isSaving) CircularProgressIndicator(Modifier.size(16.dp))
                    else Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancelar") }
            }
        )
    }
}
