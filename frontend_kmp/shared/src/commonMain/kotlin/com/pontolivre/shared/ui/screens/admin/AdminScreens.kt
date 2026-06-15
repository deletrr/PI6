package com.pontolivre.shared.ui.screens.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.pontolivre.shared.model.*
import com.pontolivre.shared.repository.AppSession
import com.pontolivre.shared.util.format
import com.pontolivre.shared.ui.components.*
import com.pontolivre.shared.ui.theme.*
import com.pontolivre.shared.viewmodel.*

// ── AdminDashboardScreen ──────────────────────────────────────────────────────

@Composable
fun AdminDashboardScreen(
    viewModel: AdminDashboardViewModel,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val user by AppSession.currentUser.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }

    Scaffold(
        topBar = {
            PontoLivreTopBar(
                title = "Painel Admin",
                actions = {
                    IconButton(onClick = { AppSession.logout(); onLogout() }) {
                        Icon(Icons.Default.Logout, contentDescription = "Sair")
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            FullScreenLoading()
        } else {
            Column(
                Modifier.fillMaxSize().padding(padding)
                    .verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Welcome
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Row(Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        Column {
                            Text("Bem-vindo, ${user?.name?.split(" ")?.first() ?: "Admin"}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("Painel de controle",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                        }
                    }
                }

                // Stats grid
                state.dashboard?.let { d ->
                    Text("Resumo geral", style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold)

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard(Modifier.weight(1f), "Usuários ativos",
                            d.totalUsers.toString(), Icons.Default.People, SuccessGreen,
                            onClick = { onNavigate("admin_users") })
                        StatCard(Modifier.weight(1f), "Vagas livres",
                            "${d.freeMeters}/${d.totalMeters}", Icons.Default.LocalParking,
                            SuccessGreen,
                            onClick = { onNavigate("admin_meters") })
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard(Modifier.weight(1f), "Vagas ocupadas",
                            d.occupiedMeters.toString(), Icons.Default.DirectionsCar, ErrorRed,
                            onClick = { onNavigate("admin_occupied_meters") })
                        StatCard(Modifier.weight(1f), "Sessões ativas",
                            d.activeSessions.toString(), Icons.Default.Timer, WarningAmber,
                            onClick = { onNavigate("admin_active_sessions") })
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard(Modifier.weight(1f), "Receita hoje",
                            "R$ ${"%.2f".format(d.todayRevenue)}", Icons.Default.AttachMoney, SuccessGreen,
                            onClick = { onNavigate("admin_extract") })
                        StatCard(Modifier.weight(1f), "Obstruídos",
                            d.occupiedMeters.minus(d.activeSessions).coerceAtLeast(0).toString(), Icons.Default.Block, ErrorRed,
                            onClick = { onNavigate("admin_obstructed") })
                    }
                }

                // Navigation menu
                Text("Gerenciamento", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)

                val menuItems = listOf(
                    Triple(Icons.Default.People,         "Usuários",      "admin_users"),
                    Triple(Icons.Default.LocalParking,   "Parquímetros",  "admin_meters"),
                    Triple(Icons.Default.DirectionsCar,  "Sessões",       "admin_sessions"),
                    Triple(Icons.Default.Warning,        "Multas",        "admin_fines"),
                    Triple(Icons.Default.Receipt,        "Extrato",       "admin_extract"),
                    Triple(Icons.Default.SupportAgent,   "Suporte",       "admin_support"),
                    Triple(Icons.Default.Code,       "Logs MQTT",     "admin_logs"),
                )
                menuItems.forEach { (icon, label, route) ->
                    Card(Modifier.fillMaxWidth().clickable { onNavigate(route) },
                        shape = RoundedCornerShape(12.dp)) {
                        Row(Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween) {
                            Row(verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Icon(icon, contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary)
                                Text(label, style = MaterialTheme.typography.bodyLarge)
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick), 
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = color)
            Text(label, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ── AdminUsersScreen ──────────────────────────────────────────────────────────

@Composable
fun AdminUsersScreen(viewModel: AdminUsersViewModel, onNavigateUp: () -> Unit) {
    val state by viewModel.state.collectAsState()
    var search by remember { mutableStateOf("") }
    var editUser by remember { mutableStateOf<UserModel?>(null) }

    LaunchedEffect(Unit) { viewModel.load() }

    Scaffold(
        topBar = {
            PontoLivreTopBar(title = "Usuários (${state.total})", onNavigateUp = onNavigateUp) }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = search, onValueChange = { search = it; viewModel.load(it) },
                placeholder = { Text("Buscar usuário...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier.fillMaxWidth().padding(16.dp), singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            state.successMessage?.let {
                Text(it, Modifier.padding(horizontal = 16.dp), color = SuccessGreen,
                    style = MaterialTheme.typography.bodySmall)
            }
            state.error?.let { ErrorMessage(it) }

            if (state.isLoading) FullScreenLoading()
            else LazyColumn(Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.users) { user ->
                    Card(Modifier.fillMaxWidth().clickable { editUser = user },
                        shape = RoundedCornerShape(12.dp)) {
                        Row(Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Surface(shape = RoundedCornerShape(50),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(40.dp)) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(user.name.first().uppercaseChar().toString(),
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    }
                                }
                                Column {
                                    Text(user.name, fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.bodyMedium)
                                    Text(user.email, style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("R$ ${"%.2f".format(user.balance)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (user.balance > 0) SuccessGreen else ErrorRed)
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                StatusBadge(if (user.active) "FREE" else "MAINTENANCE")
                                Spacer(Modifier.height(4.dp))
                                Text(if (user.role == "ADMIN") "ADMIN" else "USER",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }

    editUser?.let { user ->
        AdminEditUserDialog(
            user = user,
            isSaving = state.isSaving,
            onSave = { req -> viewModel.updateUser(user.id, req) { editUser = null } },
            onDelete = { viewModel.deleteUser(user.id); editUser = null },
            onDismiss = { editUser = null }
        )
    }
}

@Composable
private fun AdminEditUserDialog(
    user: UserModel,
    isSaving: Boolean,
    onSave: (AdminUpdateUserRequest) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    var balance by remember { mutableStateOf(user.balance.toString()) }
    var active by remember { mutableStateOf(user.active) }
    var role by remember { mutableStateOf(user.role) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(user.name) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(user.email, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(value = balance, onValueChange = { balance = it },
                    label = { Text("Saldo (R$)") }, modifier = Modifier.fillMaxWidth(),
                    singleLine = true)
                
                Text("Perfil", style = MaterialTheme.typography.labelSmall)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = role == "USER", onClick = { role = "USER" }, label = { Text("USER") })
                    FilterChip(selected = role == "ADMIN", onClick = { role = "ADMIN" }, label = { Text("ADMIN") })
                }

                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()) {
                    Text("Conta ativa")
                    Switch(checked = active, onCheckedChange = { active = it })
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onDelete, colors = ButtonDefaults.textButtonColors(contentColor = ErrorRed)) {
                    Text("Excluir Permanente")
                }
                Button(
                    onClick = { 
                        val balanceDouble = balance.toDoubleOrNull() ?: 0.0
                        onSave(AdminUpdateUserRequest(
                            active = active,
                            balance = balanceDouble,
                            role = role
                        )) 
                    },
                    enabled = !isSaving
                ) { Text("Salvar") }
            }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancelar") } }
    )

    if (showDeleteConfirm) {
        ConfirmDialog(
            title = "Desativar usuário?",
            message = "O usuário perderá acesso ao sistema.",
            confirmLabel = "Desativar",
            onConfirm = { onDelete(); showDeleteConfirm = false },
            onDismiss = { showDeleteConfirm = false }
        )
    }
}

// ── AdminMetersScreen ─────────────────────────────────────────────────────────

@Composable
fun AdminMetersScreen(viewModel: AdminMetersViewModel, onNavigateUp: () -> Unit) {
    val state by viewModel.state.collectAsState()
    var showCreate by remember { mutableStateOf(false) }
    var editingMeter by remember { mutableStateOf<ParkingMeterModel?>(null) }

    LaunchedEffect(Unit) { viewModel.load() }

    Scaffold(
        topBar = { PontoLivreTopBar(title = "Parquímetros", onNavigateUp = onNavigateUp) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreate = true }) {
                Icon(Icons.Default.Add, contentDescription = "Novo parquímetro")
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            state.successMessage?.let {
                Text(it, Modifier.padding(16.dp), color = SuccessGreen,
                    style = MaterialTheme.typography.bodySmall)
            }
            state.error?.let { ErrorMessage(it) }

            if (state.isLoading) FullScreenLoading()
            else LazyColumn(Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.meters) { meter ->
                    Card(Modifier.fillMaxWidth().clickable { editingMeter = meter },
                        shape = RoundedCornerShape(12.dp)) {
                        Row(Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(meter.code, fontWeight = FontWeight.Bold)
                                    if (meter.orphan) {
                                        Surface(color = WarningAmber.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(50)) {
                                            Text("ÓRFÃO", Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = WarningAmber)
                                        }
                                    }
                                }
                                Text(meter.description ?: "Sem descrição",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (meter.latitude != null) {
                                    Text("${meter.latitude}, ${meter.longitude}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Column(horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                StatusBadge(meter.status)
                                IconButton(onClick = { editingMeter = meter },
                                    modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar",
                                        tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreate) {
        MeterRegistrationDialog(
            isLoading = state.isLoading,
            isSaving = state.isSaving,
            onSearchCep = { cep, onResult -> viewModel.searchAddress(cep, onResult) },
            onSearchCoordinates = { address, onResult -> viewModel.searchCoordinates(address, onResult) },
            onSave = { req -> viewModel.createMeter(req) { showCreate = false } },
            onDismiss = { showCreate = false }
        )
    }

    editingMeter?.let { meter ->
        AdminEditMeterDialog(
            meter = meter,
            isSaving = state.isSaving,
            onSearchCep = { cep, onResult -> viewModel.searchAddress(cep, onResult) },
            onSearchCoordinates = { address, onResult -> viewModel.searchCoordinates(address, onResult) },
            onSave = { req -> viewModel.updateMeter(meter.id, req) { editingMeter = null } },
            onDelete = { viewModel.deleteMeter(meter.id); editingMeter = null },
            onDismiss = { editingMeter = null }
        )
    }
}

@Composable
fun MeterRegistrationDialog(
    isLoading: Boolean,
    isSaving: Boolean,
    onSearchCep: (String, (String) -> Unit) -> Unit,
    onSearchCoordinates: (String, (Double, Double) -> Unit) -> Unit,
    onSave: (CreateParkingMeterRequest) -> Unit,
    onDismiss: () -> Unit
) {
    var code by remember { mutableStateOf("") }
    var cep by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }
    var lat by remember { mutableStateOf("") }
    var lng by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo Parquímetro") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(value = code, onValueChange = { code = it.uppercase() },
                    label = { Text("Código * (ex: PKM-001)") }, modifier = Modifier.fillMaxWidth())

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(value = cep, onValueChange = { cep = it },
                        label = { Text("CEP") }, modifier = Modifier.weight(1f))
                    IconButton(onClick = { 
                        onSearchCep(cep) { 
                            address = it
                            // Busca coordenadas usando o endereço retornado
                            onSearchCoordinates(it) { l1, l2 ->
                                lat = l1.toString()
                                lng = l2.toString()
                            }
                        } 
                    }) {
                        Icon(Icons.Default.Search, "Buscar")
                    }
                }

                OutlinedTextField(value = address, onValueChange = { address = it },
                    label = { Text("Endereço / Descrição") }, modifier = Modifier.fillMaxWidth())

                OutlinedTextField(value = number, onValueChange = { number = it },
                    label = { Text("Número") }, modifier = Modifier.fillMaxWidth())

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(value = lat, onValueChange = { lat = it },
                        label = { Text("Lat") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = lng, onValueChange = { lng = it },
                        label = { Text("Lng") }, modifier = Modifier.weight(1f))
                    
                    Box(contentAlignment = Alignment.Center) {
                        if (isLoading) {
                            CircularProgressIndicator(Modifier.size(48.dp).padding(12.dp), strokeWidth = 2.dp)
                        } else {
                            IconButton(
                                onClick = {
                                    val fullAddress = if (number.isNotBlank()) "$address, $number" else address
                                    if (fullAddress.isNotBlank()) {
                                        onSearchCoordinates(fullAddress) { l1, l2 ->
                                            lat = l1.toString()
                                            lng = l2.toString()
                                        }
                                    }
                                },
                                enabled = address.isNotBlank()
                            ) {
                                Icon(Icons.Default.Map, "Buscar Coordenadas", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val finalDesc = if (number.isNotBlank()) "$address, $number" else address
                onSave(CreateParkingMeterRequest(
                    code = code, description = finalDesc,
                    latitude = lat.toDoubleOrNull(), longitude = lng.toDoubleOrNull()
                ))
            }, enabled = code.isNotBlank() && !isSaving) { Text("Criar") }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun AdminEditMeterDialog(
    meter: ParkingMeterModel,
    isSaving: Boolean,
    onSearchCep: (String, (String) -> Unit) -> Unit,
    onSearchCoordinates: (String, (Double, Double) -> Unit) -> Unit,
    onSave: (UpdateParkingMeterRequest) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    var desc by remember { mutableStateOf(meter.description ?: "") }
    var lat by remember { mutableStateOf(meter.latitude?.toString() ?: "") }
    var lng by remember { mutableStateOf(meter.longitude?.toString() ?: "") }
    var active by remember { mutableStateOf(meter.active) }
    var cep by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar ${meter.code}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(value = cep, onValueChange = { cep = it },
                        label = { Text("CEP") }, modifier = Modifier.weight(1f), singleLine = true)
                    IconButton(onClick = { 
                        onSearchCep(cep) { 
                            desc = it
                            onSearchCoordinates(it) { l1, l2 ->
                                lat = l1.toString()
                                lng = l2.toString()
                            }
                        } 
                    }) {
                        Icon(Icons.Default.Search, "Buscar")
                    }
                }

                OutlinedTextField(value = desc, onValueChange = { desc = it },
                    label = { Text("Descrição / Endereço") }, modifier = Modifier.fillMaxWidth())

                OutlinedTextField(value = number, onValueChange = { number = it },
                    label = { Text("Número") }, modifier = Modifier.fillMaxWidth())

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(value = lat, onValueChange = { lat = it },
                        label = { Text("Latitude") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = lng, onValueChange = { lng = it },
                        label = { Text("Longitude") }, modifier = Modifier.weight(1f))
                    
                    IconButton(
                        onClick = {
                            val fullAddress = if (number.isNotBlank()) "$desc, $number" else desc
                            if (fullAddress.isNotBlank()) {
                                onSearchCoordinates(fullAddress) { l1, l2 ->
                                    lat = l1.toString()
                                    lng = l2.toString()
                                }
                            }
                        },
                        enabled = desc.isNotBlank()
                    ) {
                        Icon(Icons.Default.Map, "Buscar Coordenadas", tint = MaterialTheme.colorScheme.primary)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Ativo")
                    Switch(checked = active, onCheckedChange = { active = it })
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    val finalDesc = if (number.isNotBlank()) "$desc, $number" else desc
                    onSave(UpdateParkingMeterRequest(
                        description = finalDesc, latitude = lat.toDoubleOrNull(),
                        longitude = lng.toDoubleOrNull(), active = active
                    ))
                }, enabled = !isSaving) { Text("Salvar") }
            }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

// ── AdminOrphansScreen ────────────────────────────────────────────────────────

@Composable
fun AdminOrphansScreen(viewModel: AdminMetersViewModel, onNavigateUp: () -> Unit) {
    val state by viewModel.state.collectAsState()
    var selected by remember { mutableStateOf<ParkingMeterModel?>(null) }
    var lat by remember { mutableStateOf("") }
    var lng by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var cep by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.load() }

    Scaffold(
        topBar = {
            PontoLivreTopBar(
                title = "Parquímetros Órfãos",
                onNavigateUp = onNavigateUp,
                actions = {
                    IconButton(onClick = { viewModel.load() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Atualizar")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            state.successMessage?.let {
                Card(Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.12f))) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.CheckCircle, null, tint = SuccessGreen)
                        Text(it, color = SuccessGreen)
                    }
                }
            }

            if (state.orphans.isEmpty()) {
                EmptyState("Nenhum parquímetro órfão.", Icons.Default.LocationOn)
            } else {
                LazyColumn(Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.orphans) { meter ->
                        Card(Modifier.fillMaxWidth().clickable {
                            selected = meter
                            lat = ""
                            lng = ""
                            desc = meter.description ?: ""
                            cep = ""
                        }, shape = RoundedCornerShape(12.dp),
                            border = CardDefaults.outlinedCardBorder()) {
                            Row(Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text(meter.code, fontWeight = FontWeight.Bold)
                                    Text(meter.description ?: "Sem localização",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Icon(Icons.Default.EditLocation, contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }

    selected?.let { meter ->
        var number by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { selected = null },
            title = { Text("Configurar ${meter.code}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("Defina as coordenadas para exibir este parquímetro no mapa.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(value = cep, onValueChange = { cep = it },
                            label = { Text("CEP") }, modifier = Modifier.weight(1f),
                            singleLine = true)
                        IconButton(onClick = { 
                            viewModel.searchAddress(cep) { 
                                desc = it 
                                viewModel.searchCoordinates(it) { l1, l2 ->
                                    lat = l1.toString()
                                    lng = l2.toString()
                                }
                            } 
                        }) {
                            Icon(Icons.Default.Search, "Buscar")
                        }
                    }

                    OutlinedTextField(value = desc, onValueChange = { desc = it },
                        label = { Text("Descrição / Endereço") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true)
                    
                    OutlinedTextField(value = number, onValueChange = { number = it },
                        label = { Text("Número") }, modifier = Modifier.fillMaxWidth())

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(value = lat, onValueChange = { lat = it },
                            label = { Text("Latitude *") }, placeholder = { Text("-23.5505") },
                            modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(value = lng, onValueChange = { lng = it },
                            label = { Text("Longitude *") }, placeholder = { Text("-46.6333") },
                            modifier = Modifier.weight(1f), singleLine = true)

                        Box(contentAlignment = Alignment.Center) {
                            if (state.isLoading) {
                                CircularProgressIndicator(Modifier.size(48.dp).padding(12.dp), strokeWidth = 2.dp)
                            } else {
                                IconButton(
                                    onClick = {
                                        val fullAddress = if (number.isNotBlank()) "$desc, $number" else desc
                                        if (fullAddress.isNotBlank()) {
                                            viewModel.searchCoordinates(fullAddress) { l1, l2 ->
                                                lat = l1.toString()
                                                lng = l2.toString()
                                            }
                                        }
                                    },
                                    enabled = desc.isNotBlank()
                                ) {
                                    Icon(Icons.Default.Map, "Buscar Coordenadas", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val latD = lat.toDoubleOrNull() ?: return@Button
                        val lngD = lng.toDoubleOrNull() ?: return@Button
                        val finalDesc = if (number.isNotBlank()) "$desc, $number" else desc
                        viewModel.assignCoordinates(meter.id, latD, lngD, finalDesc.ifBlank { null }) {
                            selected = null
                        }
                    },
                    enabled = lat.isNotBlank() && lng.isNotBlank() && !state.isSaving
                ) {
                    if (state.isSaving) CircularProgressIndicator(Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary)
                    else Text("Salvar e exibir no mapa")
                }
            },
            dismissButton = { OutlinedButton(onClick = { selected = null }) { Text("Cancelar") } }
        )
    }
}

// ── AdminFinesScreen ──────────────────────────────────────────────────────────

@Composable
fun AdminFinesScreen(viewModel: AdminFinesViewModel, onNavigateUp: () -> Unit) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }

    Scaffold(
        topBar = { PontoLivreTopBar(title = "Multas (${state.total})", onNavigateUp = onNavigateUp) }
    ) { padding ->
        if (state.isLoading) FullScreenLoading()
        else if (state.fines.isEmpty()) EmptyState("Nenhuma multa.", Icons.Default.CheckCircle)
        else LazyColumn(Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.fines) { fine ->
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Column(Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(fine.userName, fontWeight = FontWeight.Bold)
                                Text(fine.meterCode, style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            StatusBadge(fine.status)
                        }
                        Text(fine.reason, style = MaterialTheme.typography.bodySmall)
                        Row(Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically) {
                            Text("R$ ${"%.2f".format(fine.amount)}",
                                fontWeight = FontWeight.Bold, color = ErrorRed)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (fine.status == "PENDING") {
                                    OutlinedButton(onClick = { viewModel.updateStatus(fine.id, "PAID") },
                                        modifier = Modifier.height(32.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                                        Text("Pago", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                                OutlinedButton(onClick = { viewModel.delete(fine.id) },
                                    modifier = Modifier.height(32.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed)) {
                                    Text("Remover", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── AdminSupportScreen ────────────────────────────────────────────────────────

@Composable
fun AdminSupportScreen(viewModel: AdminSupportViewModel, onNavigateUp: () -> Unit) {
    val state by viewModel.state.collectAsState()
    var respondingTo by remember { mutableStateOf<SupportTicketModel?>(null) }
    var responseText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.load() }

    Scaffold(
        topBar = { PontoLivreTopBar(title = "Suporte (${state.total})", onNavigateUp = onNavigateUp) }
    ) { padding ->
        if (state.isLoading) FullScreenLoading()
        else if (state.tickets.isEmpty()) EmptyState("Nenhum chamado.", Icons.Default.SupportAgent)
        else LazyColumn(Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.tickets) { ticket ->
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(ticket.subject, fontWeight = FontWeight.Bold)
                                Text(ticket.userName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            StatusBadge(if (ticket.resolved) "CLOSED" else "ACTIVE")
                        }
                        Text(ticket.message, style = MaterialTheme.typography.bodySmall)
                        ticket.response?.let {
                            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                                Text(it, Modifier.padding(10.dp),
                                    style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        if (!ticket.resolved) {
                            OutlinedButton(
                                onClick = { respondingTo = ticket; responseText = "" },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Responder") }
                        }
                    }
                }
            }
        }
    }

    respondingTo?.let { ticket ->
        AlertDialog(
            onDismissRequest = { respondingTo = null },
            title = { Text("Responder: ${ticket.subject}") },
            text = {
                OutlinedTextField(value = responseText, onValueChange = { responseText = it },
                    label = { Text("Resposta") }, modifier = Modifier.fillMaxWidth(),
                    minLines = 4, maxLines = 6)
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.respond(ticket.id, responseText) { respondingTo = null }
                    },
                    enabled = responseText.isNotBlank() && !state.isResponding
                ) { Text("Enviar") }
            },
            dismissButton = { OutlinedButton(onClick = { respondingTo = null }) { Text("Cancelar") } }
        )
    }
}

// ── AdminActiveSessionsScreen ──────────────────────────────────────────────────

@Composable
fun AdminActiveSessionsScreen(viewModel: AdminSessionsViewModel, onNavigateUp: () -> Unit) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }

    Scaffold(
        topBar = { PontoLivreTopBar(title = "Sessões Ativas", onNavigateUp = onNavigateUp) }
    ) { padding ->
        if (state.isLoading) FullScreenLoading()
        else {
            val activeSessions = state.sessions.filter { it.status == "ACTIVE" }
            if (activeSessions.isEmpty()) {
                EmptyState("Nenhuma sessão ativa no momento.", Icons.Default.Timer)
            } else {
                LazyColumn(Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(activeSessions) { session ->
                        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Column {
                                        Text(session.userName, fontWeight = FontWeight.Bold)
                                        Text(session.meterCode,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    StatusBadge(session.status)
                                }
                                Text("Início: ${session.startTime.take(16).replace("T", " ")}",
                                    style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── AdminOccupiedMetersScreen ─────────────────────────────────────────────────

@Composable
fun AdminOccupiedMetersScreen(viewModel: AdminMetersViewModel, onNavigateUp: () -> Unit) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }

    Scaffold(
        topBar = { PontoLivreTopBar(title = "Vagas Ocupadas", onNavigateUp = onNavigateUp) }
    ) { padding ->
        if (state.isLoading) FullScreenLoading()
        else {
            val occupied = state.meters.filter { it.status == "OCCUPIED" }
            if (occupied.isEmpty()) {
                EmptyState("Nenhuma vaga ocupada no momento.", Icons.Default.DirectionsCar)
            } else {
                LazyColumn(Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(occupied) { meter ->
                        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                            Row(Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text(meter.code, fontWeight = FontWeight.Bold)
                                    Text(meter.description ?: "Sem descrição", style = MaterialTheme.typography.bodySmall)
                                }
                                StatusBadge(meter.status)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── AdminObstructedScreen ──────────────────────────────────────────────────────

@Composable
fun AdminObstructedScreen(meterViewModel: AdminMetersViewModel, sessionViewModel: AdminSessionsViewModel, onNavigateUp: () -> Unit) {
    val meterState by meterViewModel.state.collectAsState()
    val sessionState by sessionViewModel.state.collectAsState()
    var showFiscalConfirm by remember { mutableStateOf<String?>(null) }
    var fiscalACominho by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) { 
        meterViewModel.load()
        sessionViewModel.load()
    }

    Scaffold(
        topBar = { PontoLivreTopBar(title = "Vagas Obstruídas", onNavigateUp = onNavigateUp) }
    ) { padding ->
        if (meterState.isLoading || sessionState.isLoading) FullScreenLoading()
        else {
            val activeSessionMeterCodes = sessionState.sessions.filter { it.status == "ACTIVE" }.map { it.meterCode }
            
            val obstructed = meterState.meters.filter { 
                it.status == "OCCUPIED" && !activeSessionMeterCodes.contains(it.code)
            }

            if (obstructed.isEmpty()) {
                EmptyState("Nenhuma obstrução detectada.", Icons.Default.CheckCircle)
            } else {
                LazyColumn(Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(obstructed) { meter ->
                        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f))) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Column {
                                        Text(meter.code, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        Text(meter.description ?: "Sem descrição", style = MaterialTheme.typography.bodySmall)
                                    }
                                    Surface(color = ErrorRed, shape = RoundedCornerShape(50)) {
                                        Text("SEM SESSÃO", Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            color = androidx.compose.ui.graphics.Color.White, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                                Button(
                                    onClick = { showFiscalConfirm = meter.code },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                                ) {
                                    Icon(Icons.Default.Gavel, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("ACIONAR FISCAL")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    showFiscalConfirm?.let { code ->
        AlertDialog(
            onDismissRequest = { showFiscalConfirm = null },
            title = { Text("Acionar Fiscal?") },
            text = { Text("Deseja enviar um fiscal para a vaga $code? O veículo está ocupando a vaga sem uma sessão ativa.") },
            confirmButton = {
                Button(onClick = { 
                    showFiscalConfirm = null
                    fiscalACominho = code
                }) { Text("Confirmar") }
            },
            dismissButton = { TextButton(onClick = { showFiscalConfirm = null }) { Text("Cancelar") } }
        )
    }

    fiscalACominho?.let { code ->
        AlertDialog(
            onDismissRequest = { fiscalACominho = null },
            title = { Text("Solicitação Enviada") },
            text = { Text("Fiscal a caminho da vaga $code.") },
            confirmButton = {
                Button(onClick = { fiscalACominho = null }) { Text("OK") }
            }
        )
    }
}

// ── AdminSessionsScreen (Lista completa com filtros) ──────────────────────────

@Composable
fun AdminSessionsScreen(viewModel: AdminSessionsViewModel, onNavigateUp: () -> Unit) {
    val state by viewModel.state.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf<String?>(null) } // null = Todos

    val statusMap = mapOf(
        "ACTIVE" to "Ativa",
        "CLOSED" to "Encerrada",
        "OVERTIME" to "Excedida",
        "PENDING" to "Pendente",
        "EXPIRED" to "Expirada"
    )

    LaunchedEffect(Unit) { viewModel.load() }

    val filteredSessions = remember(state.sessions, searchQuery, selectedStatus) {
        state.sessions.filter { session ->
            val matchesSearch = searchQuery.isBlank() || 
                session.meterCode.contains(searchQuery, ignoreCase = true) ||
                session.userName.contains(searchQuery, ignoreCase = true)
            
            val matchesStatus = selectedStatus == null || session.status == selectedStatus
            
            matchesSearch && matchesStatus
        }
    }

    Scaffold(
        topBar = { PontoLivreTopBar(title = "Histórico de Sessões", onNavigateUp = onNavigateUp) }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            // BUSCA E FILTROS
            Card(
                Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Buscar por vaga ou usuário...") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                    
                    // Chips de Status
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedStatus == null,
                            onClick = { selectedStatus = null },
                            label = { Text("Todas") }
                        )
                        statusMap.forEach { (status, label) ->
                            FilterChip(
                                selected = selectedStatus == status,
                                onClick = { selectedStatus = status },
                                label = { Text(label) }
                            )
                        }
                    }
                }
            }

            if (state.isLoading) FullScreenLoading()
            else if (filteredSessions.isEmpty()) {
                EmptyState("Nenhuma sessão encontrada.", Icons.Default.History)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredSessions) { session ->
                        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Column {
                                        Text(session.userName, fontWeight = FontWeight.Bold)
                                        Text(session.meterCode,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    StatusBadge(session.status)
                                }
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(session.startTime.take(16).replace("T", " "),
                                        style = MaterialTheme.typography.bodySmall)
                                    Text("R$ ${"%.2f".format(session.amountCharged)}",
                                        fontWeight = FontWeight.Bold,
                                        color = if (session.amountCharged > 0) ErrorRed else SuccessGreen)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
