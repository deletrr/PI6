package com.pontolivre.dto

import com.pontolivre.entity.*
import jakarta.validation.constraints.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

// ── Auth ──────────────────────────────────────────────────────────────────────

data class RegisterRequest(
    @field:NotBlank @field:Size(min = 3, max = 150)
    val name: String,

    @field:NotBlank @field:Email
    val email: String,

    @field:NotBlank @field:Size(min = 8)
    val password: String,

    @field:NotBlank
    val cpf: String,

    val phone: String? = null
)

data class LoginRequest(
    @field:NotBlank @field:Email
    val email: String,

    @field:NotBlank
    val password: String
)

data class AuthResponse(
    val token: String,
    val user: UserResponse
)

// ── User ──────────────────────────────────────────────────────────────────────

data class UserResponse(
    val id: UUID,
    val name: String,
    val email: String,
    val cpf: String,
    val phone: String?,
    val role: UserRole,
    val balance: BigDecimal,
    val active: Boolean,
    val createdAt: LocalDateTime
)

data class UpdateUserRequest(
    @field:Size(min = 3, max = 150)
    val name: String? = null,

    val phone: String? = null,

    @field:Size(min = 8)
    val password: String? = null
)

data class AdminUpdateUserRequest(
    val name: String? = null,
    val phone: String? = null,
    val role: UserRole? = null,
    val active: Boolean? = null,
    val balance: BigDecimal? = null
)

fun User.toResponse() = UserResponse(
    id = id,
    name = name,
    email = email,
    cpf = cpf,
    phone = phone,
    role = role,
    balance = balance,
    active = active,
    createdAt = createdAt
)

// ── ParkingMeter ──────────────────────────────────────────────────────────────

data class ParkingMeterResponse(
    val id: UUID,
    val code: String,
    val description: String?,
    val latitude: Double?,
    val longitude: Double?,
    val status: ParkingStatus,
    val mqttTopic: String,
    val lastSeen: LocalDateTime?,
    val orphan: Boolean,
    val active: Boolean
)

data class CreateParkingMeterRequest(
    @field:NotBlank @field:Size(max = 20)
    val code: String,

    val description: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)

data class UpdateParkingMeterRequest(
    val description: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val active: Boolean? = null,
    val orphan: Boolean? = null
)

fun ParkingMeter.toResponse() = ParkingMeterResponse(
    id = id,
    code = code,
    description = description,
    latitude = latitude,
    longitude = longitude,
    status = status,
    mqttTopic = mqttTopic,
    lastSeen = lastSeen,
    orphan = orphan,
    active = active
)

// ── Vehicle ──────────────────────────────────────────────────────────────────

data class CreateVehicleRequest(
    @field:NotBlank @field:Size(max = 100)
    val model: String,

    @field:NotBlank @field:Size(min = 7, max = 10)
    val plate: String,

    @field:NotBlank @field:Size(max = 50)
    val color: String
)

data class VehicleResponse(
    val id: UUID,
    val userId: UUID,
    val model: String,
    val plate: String,
    val color: String,
    val createdAt: LocalDateTime
)

fun Vehicle.toResponse() = VehicleResponse(
    id = id,
    userId = user.id,
    model = model,
    plate = plate,
    color = color,
    createdAt = createdAt
)

// ── Session ───────────────────────────────────────────────────────────────────

data class StartSessionRequest(
    @field:NotBlank
    val meterCode: String,

    @field:NotBlank
    @field:Size(min = 7, max = 10)
    val vehiclePlate: String
)

data class ClaimSessionRequest(
    @field:NotBlank
    val sessionCode: String,

    @field:NotNull
    val vehicleId: UUID
)

data class SessionResponse(
    val id: UUID,
    val userId: UUID,
    val userName: String,
    val meterCode: String,
    val vehiclePlate: String?,
    val meterDescription: String?,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime?,
    val freeUntil: LocalDateTime,
    val chargedHours: Int,
    val amountCharged: BigDecimal,
    val status: SessionStatus,
    val overtime: Boolean,
    val elapsedMinutes: Long?,
    val estimatedCost: BigDecimal?
)

fun ParkingSession.toResponse(
    elapsedMinutes: Long? = null,
    estimatedCost: BigDecimal? = null
) = SessionResponse(
    id = id,
    userId = user?.id ?: UUID(0,0),
    userName = user?.name ?: "Pendente",
    meterCode = parkingMeter.code,
    vehiclePlate = vehiclePlate ?: vehicle?.plate,
    meterDescription = parkingMeter.description,
    startTime = startTime,
    endTime = endTime,
    freeUntil = freeUntil,
    chargedHours = chargedHours,
    amountCharged = amountCharged,
    status = status,
    overtime = overtime,
    elapsedMinutes = elapsedMinutes,
    estimatedCost = estimatedCost
)

// ── Wallet ────────────────────────────────────────────────────────────────────

data class RechargeRequest(
    @field:NotNull @field:DecimalMin("5.00")
    val amount: BigDecimal,

    @field:NotNull
    val paymentMethod: PaymentMethod,

    // Cartão de crédito (dados fictícios)
    val cardNumber: String? = null,
    val cardHolder: String? = null,
    val cardExpiry: String? = null,
    val cardCvv: String? = null
)

data class RechargeResponse(
    val transactionId: UUID,
    val amount: BigDecimal,
    val newBalance: BigDecimal,
    val paymentMethod: PaymentMethod,
    val referenceCode: String,
    val pixQrCode: String?,
    val pixKey: String?,
    val approved: Boolean
)

data class WalletTransactionResponse(
    val id: UUID,
    val sessionId: UUID?,
    val type: TransactionType,
    val amount: BigDecimal,
    val balanceBefore: BigDecimal,
    val balanceAfter: BigDecimal,
    val description: String,
    val paymentMethod: PaymentMethod?,
    val referenceCode: String?,
    val createdAt: LocalDateTime
)

fun WalletTransaction.toResponse() = WalletTransactionResponse(
    id = id,
    sessionId = session?.id,
    type = type,
    amount = amount,
    balanceBefore = balanceBefore,
    balanceAfter = balanceAfter,
    description = description,
    paymentMethod = paymentMethod,
    referenceCode = referenceCode,
    createdAt = createdAt
)

// ── Fine ──────────────────────────────────────────────────────────────────────

data class FineResponse(
    val id: UUID,
    val userId: UUID,
    val userName: String,
    val sessionId: UUID,
    val meterCode: String,
    val amount: BigDecimal,
    val reason: String,
    val status: FineStatus,
    val paidAt: LocalDateTime?,
    val createdAt: LocalDateTime
)

data class UpdateFineRequest(
    val status: FineStatus
)

fun Fine.toResponse() = FineResponse(
    id = id,
    userId = user.id,
    userName = user.name,
    sessionId = session.id,
    meterCode = session.parkingMeter.code,
    amount = amount,
    reason = reason,
    status = status,
    paidAt = paidAt,
    createdAt = createdAt
)

// ── Support ───────────────────────────────────────────────────────────────────

data class CreateSupportTicketRequest(
    @field:NotBlank @field:Size(max = 200)
    val subject: String,

    @field:NotBlank
    val message: String
)

data class RespondSupportTicketRequest(
    @field:NotBlank
    val response: String
)

data class SupportTicketResponse(
    val id: UUID,
    val userId: UUID,
    val userName: String,
    val subject: String,
    val message: String,
    val response: String?,
    val resolved: Boolean,
    val createdAt: LocalDateTime
)

fun SupportTicket.toResponse() = SupportTicketResponse(
    id = id,
    userId = user.id,
    userName = user.name,
    subject = subject,
    message = message,
    response = response,
    resolved = resolved,
    createdAt = createdAt
)

// ── Dashboard ─────────────────────────────────────────────────────────────────

data class DashboardResponse(
    val totalUsers: Long,
    val totalMeters: Long,
    val freeMeters: Long,
    val occupiedMeters: Long,
    val orphanMeters: Long,
    val activeSessions: Long,
    val todayRevenue: BigDecimal,
    val pendingFines: Long
)

// ── MQTT Logs ─────────────────────────────────────────────────────────────────

data class MqttLogResponse(
    val id: Long,
    val topic: String,
    val payload: String,
    val meterCode: String?,
    val processed: Boolean,
    val createdAt: LocalDateTime
)

fun MqttLog.toResponse() = MqttLogResponse(
    id = id,
    topic = topic,
    payload = payload,
    meterCode = meterCode,
    processed = processed,
    createdAt = createdAt
)

// ── Pagination ────────────────────────────────────────────────────────────────

data class PageResponse<T>(
    val content: List<T>,
    val totalElements: Long,
    val totalPages: Int,
    val page: Int,
    val size: Int
)
