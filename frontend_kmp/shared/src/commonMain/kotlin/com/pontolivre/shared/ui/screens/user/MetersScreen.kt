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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.pontolivre.shared.ui.components.*
import com.pontolivre.shared.viewmodel.HomeViewModel

@Composable
fun MetersScreen(
    viewModel: HomeViewModel,
    onNavigateToMeterDetail: (String) -> Unit,
    onNavigateUp: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
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
        topBar = { PontoLivreTopBar(title = "Vagas", onNavigateUp = onNavigateUp) }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            // FILTRO DE ENDEREÇO
            OutlinedTextField(
                value = addressQuery,
                onValueChange = { addressQuery = it },
                placeholder = { Text("Filtrar por endereço ou CEP...") },
                leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary) },
                trailingIcon = {
                    IconButton(onClick = { viewModel.filterByAddress(addressQuery) }) {
                        Icon(Icons.Default.FilterList, null)
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // BUSCA E TOGGLE MAPA
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar vaga...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                IconButton(onClick = { isMapView = !isMapView }) {
                    Icon(if (isMapView) Icons.Default.List else Icons.Default.Map, null)
                }
            }

            SectionHeader(title = "Vagas próximas (${displayMeters.count { it.status == "FREE" }}/${displayMeters.size})",
                action = {
                    IconButton(onClick = { viewModel.loadMapMeters() }) {
                        Icon(Icons.Default.Refresh, null)
                    }
                })

            if (state.isLoading) {
                FullScreenLoading()
            } else {
                if (isMapView) {
                    Box(Modifier.fillMaxSize().padding(16.dp).clip(RoundedCornerShape(16.dp))) {
                        ParkingMapView(meters = displayMeters, onMeterSelected = onNavigateToMeterDetail)
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(displayMeters) { meter ->
                            MeterCard(meter = meter, onClick = { onNavigateToMeterDetail(meter.id) })
                        }
                    }
                }
            }
        }
    }
}
