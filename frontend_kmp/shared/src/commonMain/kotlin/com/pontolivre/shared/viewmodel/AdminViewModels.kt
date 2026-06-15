package com.pontolivre.shared.viewmodel

import com.pontolivre.shared.api.*
import com.pontolivre.shared.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

// ── AdminDashboardViewModel ───────────────────────────────────────────────────

data class AdminDashboardUiState(
    val dashboard: DashboardModel? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class AdminDashboardViewModel {
    private val scope = CoroutineScope(Dispatchers.Main)
    private val _state = MutableStateFlow(AdminDashboardUiState())
    val state: StateFlow<AdminDashboardUiState> = _state

    fun load() {
        scope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val r = DashboardApi.getDashboard()) {
                is ApiResult.Success -> _state.value = _state.value.copy(
                    dashboard = r.data, isLoading = false
                )
                is ApiResult.Error -> _state.value = _state.value.copy(
                    isLoading = false, error = r.message
                )
            }
        }
    }
}

// ── AdminUsersViewModel ───────────────────────────────────────────────────────

data class AdminUsersUiState(
    val users: List<UserModel> = emptyList(),
    val total: Long = 0,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class AdminUsersViewModel {
    private val scope = CoroutineScope(Dispatchers.Main)
    private val _state = MutableStateFlow(AdminUsersUiState())
    val state: StateFlow<AdminUsersUiState> = _state

    fun load(search: String? = null) {
        scope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val r = UserApi.listUsers(search)) {
                is ApiResult.Success -> _state.value = _state.value.copy(
                    users = r.data.content, total = r.data.totalElements, isLoading = false
                )
                is ApiResult.Error -> _state.value = _state.value.copy(
                    isLoading = false, error = r.message
                )
            }
        }
    }

    fun updateUser(id: String, req: AdminUpdateUserRequest, onDone: () -> Unit) {
        scope.launch {
            _state.value = _state.value.copy(isSaving = true)
            when (val r = UserApi.updateUser(id, req)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(
                        isSaving = false, successMessage = "Usuário atualizado."
                    )
                    load()
                    onDone()
                }
                is ApiResult.Error -> _state.value = _state.value.copy(
                    isSaving = false, error = r.message
                )
            }
        }
    }

    fun deleteUser(id: String) {
        scope.launch {
            when (val r = UserApi.deleteUser(id)) {
                is ApiResult.Success -> { load(); _state.value = _state.value.copy(successMessage = "Usuário desativado.") }
                is ApiResult.Error   -> _state.value = _state.value.copy(error = r.message)
            }
        }
    }

    fun clearMessages() { _state.value = _state.value.copy(error = null, successMessage = null) }
}

// ── AdminMetersViewModel ──────────────────────────────────────────────────────

data class AdminMetersUiState(
    val meters: List<ParkingMeterModel> = emptyList(),
    val orphans: List<ParkingMeterModel> = emptyList(),
    val total: Long = 0,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class AdminMetersViewModel {
    private val scope = CoroutineScope(Dispatchers.Main)
    private val _state = MutableStateFlow(AdminMetersUiState())
    val state: StateFlow<AdminMetersUiState> = _state

    fun load(search: String? = null) {
        scope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val metersResult = ParkingMeterApi.listAll(search)
            val orphansResult = ParkingMeterApi.listOrphans()
            val meters = if (metersResult is ApiResult.Success) metersResult.data.content else emptyList()
            val orphans = if (orphansResult is ApiResult.Success) orphansResult.data else emptyList()
            val total = if (metersResult is ApiResult.Success) metersResult.data.totalElements else 0L
            _state.value = _state.value.copy(meters = meters, orphans = orphans, total = total, isLoading = false)
        }
    }

    fun createMeter(req: CreateParkingMeterRequest, onDone: () -> Unit) {
        scope.launch {
            _state.value = _state.value.copy(isSaving = true, error = null)
            when (val r = ParkingMeterApi.create(req)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(isSaving = false, successMessage = "Parquímetro criado.")
                    load()
                    onDone()
                }
                is ApiResult.Error -> _state.value = _state.value.copy(isSaving = false, error = r.message)
            }
        }
    }

    fun updateMeter(id: String, req: UpdateParkingMeterRequest, onDone: () -> Unit) {
        scope.launch {
            _state.value = _state.value.copy(isSaving = true, error = null)
            when (val r = ParkingMeterApi.update(id, req)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(isSaving = false, successMessage = "Parquímetro atualizado.")
                    load()
                    onDone()
                }
                is ApiResult.Error -> _state.value = _state.value.copy(isSaving = false, error = r.message)
            }
        }
    }

    fun searchAddress(cep: String, onResult: (String) -> Unit) {
        scope.launch {
            when (val r = ExternalApi.searchCep(cep)) {
                is ApiResult.Success -> {
                    val addr = "${r.data.logradouro}, ${r.data.bairro}, ${r.data.localidade}-${r.data.uf}"
                    onResult(addr)
                }
                is ApiResult.Error -> _state.value = _state.value.copy(error = r.message)
            }
        }
    }

    fun searchCoordinates(address: String, onResult: (Double, Double) -> Unit) {
        scope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val r = ExternalApi.searchCoordinates(address)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(isLoading = false)
                    onResult(r.data.first, r.data.second)
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(isLoading = false, error = r.message)
                }
            }
        }
    }

    fun assignCoordinates(id: String, lat: Double, lng: Double, description: String?, onDone: () -> Unit) {
        scope.launch {
            _state.value = _state.value.copy(isSaving = true)
            val req = UpdateParkingMeterRequest(description = description, latitude = lat, longitude = lng, orphan = false)
            when (val r = ParkingMeterApi.update(id, req)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(isSaving = false, successMessage = "Coordenadas definidas.")
                    load()
                    onDone()
                }
                is ApiResult.Error -> _state.value = _state.value.copy(isSaving = false, error = r.message)
            }
        }
    }

    fun deleteMeter(id: String) {
        scope.launch {
            when (val r = ParkingMeterApi.delete(id)) {
                is ApiResult.Success -> { load(); _state.value = _state.value.copy(successMessage = "Parquímetro desativado.") }
                is ApiResult.Error   -> _state.value = _state.value.copy(error = r.message)
            }
        }
    }

    fun clearMessages() { _state.value = _state.value.copy(error = null, successMessage = null) }
}

// ── AdminFinesViewModel ───────────────────────────────────────────────────────

data class AdminFinesUiState(
    val fines: List<FineModel> = emptyList(),
    val total: Long = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class AdminFinesViewModel {
    private val scope = CoroutineScope(Dispatchers.Main)
    private val _state = MutableStateFlow(AdminFinesUiState())
    val state: StateFlow<AdminFinesUiState> = _state

    fun load() {
        scope.launch {
            _state.value = _state.value.copy(isLoading = true)
            when (val r = FineApi.getAllFines()) {
                is ApiResult.Success -> _state.value = _state.value.copy(
                    fines = r.data.content, total = r.data.totalElements, isLoading = false
                )
                is ApiResult.Error -> _state.value = _state.value.copy(isLoading = false, error = r.message)
            }
        }
    }

    fun updateStatus(id: String, status: String) {
        scope.launch {
            when (val r = FineApi.updateFine(id, status)) {
                is ApiResult.Success -> { load(); _state.value = _state.value.copy(successMessage = "Multa atualizada.") }
                is ApiResult.Error   -> _state.value = _state.value.copy(error = r.message)
            }
        }
    }

    fun delete(id: String) {
        scope.launch {
            when (val r = FineApi.deleteFine(id)) {
                is ApiResult.Success -> { load(); _state.value = _state.value.copy(successMessage = "Multa removida.") }
                is ApiResult.Error   -> _state.value = _state.value.copy(error = r.message)
            }
        }
    }

    fun clearMessages() { _state.value = _state.value.copy(error = null, successMessage = null) }
}

// ── AdminSupportViewModel ─────────────────────────────────────────────────────

data class AdminSupportUiState(
    val tickets: List<SupportTicketModel> = emptyList(),
    val total: Long = 0,
    val isLoading: Boolean = false,
    val isResponding: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class AdminSupportViewModel {
    private val scope = CoroutineScope(Dispatchers.Main)
    private val _state = MutableStateFlow(AdminSupportUiState())
    val state: StateFlow<AdminSupportUiState> = _state

    fun load(resolved: Boolean? = null) {
        scope.launch {
            _state.value = _state.value.copy(isLoading = true)
            when (val r = SupportApi.getAllTickets(resolved)) {
                is ApiResult.Success -> _state.value = _state.value.copy(
                    tickets = r.data.content, total = r.data.totalElements, isLoading = false
                )
                is ApiResult.Error -> _state.value = _state.value.copy(isLoading = false, error = r.message)
            }
        }
    }

    fun respond(id: String, response: String, onDone: () -> Unit) {
        scope.launch {
            _state.value = _state.value.copy(isResponding = true)
            when (val r = SupportApi.respond(id, response)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(isResponding = false, successMessage = "Resposta enviada.")
                    load()
                    onDone()
                }
                is ApiResult.Error -> _state.value = _state.value.copy(isResponding = false, error = r.message)
            }
        }
    }

    fun clearMessages() { _state.value = _state.value.copy(error = null, successMessage = null) }
}

// ── AdminSessionsViewModel ────────────────────────────────────────────────────

data class AdminSessionsUiState(
    val sessions: List<SessionModel> = emptyList(),
    val total: Long = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

class AdminSessionsViewModel {
    private val scope = CoroutineScope(Dispatchers.Main)
    private val _state = MutableStateFlow(AdminSessionsUiState())
    val state: StateFlow<AdminSessionsUiState> = _state

    fun load() {
        scope.launch {
            _state.value = _state.value.copy(isLoading = true)
            when (val r = SessionApi.getAllSessions()) {
                is ApiResult.Success -> _state.value = _state.value.copy(
                    sessions = r.data.content, total = r.data.totalElements, isLoading = false
                )
                is ApiResult.Error -> _state.value = _state.value.copy(isLoading = false, error = r.message)
            }
        }
    }
}
