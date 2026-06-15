package com.pontolivre.shared.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── Auth ──────────────────────────────────────────────────────────────────────

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val cpf: String,
    val phone: String? = null
)

@Serializable
data class AuthResponse(val token: String, val user: UserModel)

// ── User ──────────────────────────────────────────────────────────────────────

@Serializable
data class UserModel(
    val id: String,
    val name: String,
    val email: String,
    val cpf: String,
    val phone: String? = null,
    val role: String,
    val balance: Double,
    val active: Boolean,
    val createdAt: String,
    val vehicles: List<VehicleModel> = emptyList()
)

@Serializable
data class VehicleModel(
    val id: String,
    val model: String,
    val plate: String,
    val color: String,
    val createdAt: String
)

@Serializable
data class CreateVehicleRequest(
    val model: String,
    val plate: String,
    val color: String
)

@Serializable
data class UpdateUserRequest(
    val name: String? = null,
    val phone: String? = null,
    val password: String? = null
)

@Serializable
data class AdminUpdateUserRequest(
    val name: String? = null,
    val phone: String? = null,
    val role: String? = null,
    val active: Boolean? = null,
    val balance: Double? = null
)

// ── ParkingMeter ──────────────────────────────────────────────────────────────

@Serializable
data class ParkingMeterModel(
    val id: String,
    val code: String,
    val description: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val status: String,
    val mqttTopic: String,
    val lastSeen: String? = null,
    val orphan: Boolean,
    val active: Boolean
)

@Serializable
data class CreateParkingMeterRequest(
    val code: String,
    val description: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)

@Serializable
data class UpdateParkingMeterRequest(
    val description: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val active: Boolean? = null,
    val orphan: Boolean? = null
)

// ── Session ───────────────────────────────────────────────────────────────────

@Serializable
data class StartSessionRequest(
    val meterCode: String,
    val vehiclePlate: String
)

@Serializable
data class ClaimSessionRequest(
    val sessionCode: String,
    val vehicleId: String
)

@Serializable
data class SessionModel(
    val id: String,
    val userId: String,
    val userName: String,
    val meterCode: String,
    val vehiclePlate: String? = null,
    val meterDescription: String? = null,
    val startTime: String,
    val endTime: String? = null,
    val freeUntil: String,
    val chargedHours: Int,
    val amountCharged: Double,
    val status: String,
    val overtime: Boolean,
    val elapsedMinutes: Long? = null,
    val estimatedCost: Double? = null
)

// ── Wallet ────────────────────────────────────────────────────────────────────

@Serializable
data class RechargeRequest(
    val amount: Double,
    val paymentMethod: String,
    val cardNumber: String? = null,
    val cardHolder: String? = null,
    val cardExpiry: String? = null,
    val cardCvv: String? = null
)

@Serializable
data class RechargeResponse(
    val transactionId: String,
    val amount: Double,
    val newBalance: Double,
    val paymentMethod: String,
    val referenceCode: String,
    val pixQrCode: String? = null,
    val pixKey: String? = null,
    val approved: Boolean
)

@Serializable
data class WalletTransactionModel(
    val id: String,
    val sessionId: String? = null,
    val type: String,
    val amount: Double,
    val balanceBefore: Double,
    val balanceAfter: Double,
    val description: String,
    val paymentMethod: String? = null,
    val referenceCode: String? = null,
    val createdAt: String
)

// ── Fine ──────────────────────────────────────────────────────────────────────

@Serializable
data class FineModel(
    val id: String,
    val userId: String,
    val userName: String,
    val sessionId: String,
    val meterCode: String,
    val amount: Double,
    val reason: String,
    val status: String,
    val paidAt: String? = null,
    val createdAt: String
)

// ── Support ───────────────────────────────────────────────────────────────────

@Serializable
data class CreateSupportTicketRequest(val subject: String, val message: String)

@Serializable
data class SupportTicketModel(
    val id: String,
    val userId: String,
    val userName: String,
    val subject: String,
    val message: String,
    val response: String? = null,
    val resolved: Boolean,
    val createdAt: String
)

// ── Dashboard ─────────────────────────────────────────────────────────────────

@Serializable
data class DashboardModel(
    val totalUsers: Long,
    val totalMeters: Long,
    val freeMeters: Long,
    val occupiedMeters: Long,
    val orphanMeters: Long,
    val activeSessions: Long,
    val todayRevenue: Double,
    val pendingFines: Long
)

// ── MQTT Logs ─────────────────────────────────────────────────────────────────

@Serializable
data class MqttLogModel(
    val id: Long,
    val topic: String,
    val payload: String,
    val meterCode: String? = null,
    val processed: Boolean,
    val createdAt: String
)

// ── Pagination ────────────────────────────────────────────────────────────────

@Serializable
data class PageResponse<T>(
    val content: List<T>,
    val totalElements: Long,
    val totalPages: Int,
    val page: Int,
    val size: Int
)

// ── Error ─────────────────────────────────────────────────────────────────────

@Serializable
data class ErrorResponse(val status: Int, val message: String)

// ── Balance wrapper ───────────────────────────────────────────────────────────

@Serializable
data class BalanceResponse(val balance: Double)
