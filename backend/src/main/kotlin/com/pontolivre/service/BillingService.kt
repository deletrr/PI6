package com.pontolivre.service

import com.pontolivre.config.ParkingRulesConfig
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.ceil

@Service
class BillingService(private val rules: ParkingRulesConfig) {

    /**
     * RN01 + RN02 + RN03
     * Calcula o valor a cobrar dado o início e fim da sessão.
     */
    fun calculateCharge(startTime: LocalDateTime, endTime: LocalDateTime): BillingResult {
        val totalMinutes = ChronoUnit.MINUTES.between(startTime, endTime)

        // RN02 — tolerância gratuita
        if (totalMinutes <= rules.freeToleranceMinutes) {
            return BillingResult(
                amountCharged = BigDecimal.ZERO,
                chargedHours = 0,
                billableMinutes = 0,
                category = BillingCategory.TOLERANCE
            )
        }

        // RN03 — calcular minutos faturáveis dentro do horário comercial
        val billableMinutes = countBillableMinutes(startTime, endTime)

        if (billableMinutes == 0L) {
            return BillingResult(
                amountCharged = BigDecimal.ZERO,
                chargedHours = 0,
                billableMinutes = 0,
                category = BillingCategory.OUT_OF_HOURS
            )
        }

        // RN01 — R$2,00/hora, fração > 5min gera próxima hora cheia
        val fullHours = (billableMinutes / 60).toInt()
        val extraMinutes = billableMinutes % 60
        
        val hours = if (extraMinutes > 5) {
            fullHours + 1
        } else {
            if (fullHours == 0) 1 else fullHours
        }

        val amount = rules.ratePerHour.multiply(BigDecimal(hours))

        return BillingResult(
            amountCharged = amount,
            chargedHours = hours,
            billableMinutes = billableMinutes,
            category = BillingCategory.REGULAR
        )
    }

    /**
     * RN03 — conta minutos dentro do horário de cobrança:
     *   Seg-Sex: 08:00–18:00
     *   Sáb:     08:00–13:00
     *   Dom e feriados: sem cobrança
     */
    private fun countBillableMinutes(start: LocalDateTime, end: LocalDateTime): Long {
        var current = start
        var billableMinutes = 0L

        while (current.isBefore(end)) {
            if (isChargeable(current)) billableMinutes++
            current = current.plusMinutes(1)
        }

        return billableMinutes
    }

    private fun isChargeable(dateTime: LocalDateTime): Boolean {
        val hour = dateTime.hour
        val minute = dateTime.minute
        val totalMinuteOfDay = hour * 60 + minute

        return when (dateTime.dayOfWeek) {
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY -> totalMinuteOfDay in (8 * 60)..(18 * 60 - 1)

            DayOfWeek.SATURDAY -> totalMinuteOfDay in (8 * 60)..(13 * 60 - 1)

            else -> false
        }
    }

    /**
     * Calcula estimativa de custo para uma sessão em andamento.
     */
    fun estimateCurrentCost(startTime: LocalDateTime): BigDecimal {
        val now = LocalDateTime.now()
        if (now.isBefore(startTime)) return BigDecimal.ZERO
        return calculateCharge(startTime, now).amountCharged
    }

    fun isOvertimeExceeded(startTime: LocalDateTime): Boolean {
        val elapsed = ChronoUnit.HOURS.between(startTime, LocalDateTime.now())
        return elapsed >= rules.maxSessionHours
    }
}

enum class BillingCategory {
    TOLERANCE,
    OUT_OF_HOURS,
    REGULAR
}

data class BillingResult(
    val amountCharged: BigDecimal,
    val chargedHours: Int,
    val billableMinutes: Long,
    val category: BillingCategory
)
