package com.pontolivre.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "wallet_transactions")
class WalletTransaction(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    var session: ParkingSession? = null,

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "transaction_type")
    var type: TransactionType,

    @Column(nullable = false, precision = 10, scale = 2)
    var amount: BigDecimal,

    @Column(name = "balance_before", nullable = false, precision = 10, scale = 2)
    var balanceBefore: BigDecimal,

    @Column(name = "balance_after", nullable = false, precision = 10, scale = 2)
    var balanceAfter: BigDecimal,

    @Column(nullable = false, length = 500)
    var description: String,

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "payment_method", columnDefinition = "payment_method")
    var paymentMethod: PaymentMethod? = null,

    @Column(name = "reference_code", length = 100)
    var referenceCode: String? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class TransactionType { CREDIT_PIX, CREDIT_CARD, DEBIT_SESSION, DEBIT_FINE, CREDIT_REFUND }
enum class PaymentMethod { PIX, CREDIT_CARD }
