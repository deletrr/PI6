package com.pontolivre.service

import com.pontolivre.config.ParkingRulesConfig
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDateTime

class BillingServiceTest {

    private lateinit var billingService: BillingService
    private lateinit var rules: ParkingRulesConfig

    @BeforeEach
    fun setup() {
        rules = ParkingRulesConfig().apply {
            ratePerHourRaw       = "2.00"
            freeToleranceMinutes = 15L
            maxSessionHours      = 2L
            overtimeFineRaw      = "10.00"
        }
        billingService = BillingService(rules)
    }

    @Test
    fun `deve retornar zero para sessao de 5 minutos dentro da tolerancia`() {
        val start = LocalDateTime.of(2024, 6, 3, 10, 0)  // segunda
        val result = billingService.calculateCharge(start, start.plusMinutes(5))
        assertEquals(BigDecimal.ZERO, result.amountCharged)
        assertEquals(0, result.chargedHours)
        assertEquals(BillingCategory.TOLERANCE, result.category)
    }

    @Test
    fun `deve retornar zero para sessao exatamente de 15 minutos`() {
        val start = LocalDateTime.of(2024, 6, 3, 10, 0)
        val result = billingService.calculateCharge(start, start.plusMinutes(15))
        assertEquals(BigDecimal.ZERO, result.amountCharged)
    }

    @Test
    fun `deve cobrar 1 hora para sessao de 16 minutos dentro do comercial`() {
        val start = LocalDateTime.of(2024, 6, 3, 10, 0)
        val result = billingService.calculateCharge(start, start.plusMinutes(16))
        assertEquals(BigDecimal("2.00"), result.amountCharged)
        assertEquals(1, result.chargedHours)
        assertEquals(BillingCategory.REGULAR, result.category)
    }

    @Test
    fun `deve cobrar 1 hora para sessao de 60 minutos exatos`() {
        val start = LocalDateTime.of(2024, 6, 3, 10, 0)
        val result = billingService.calculateCharge(start, start.plusMinutes(60))
        assertEquals(BigDecimal("2.00"), result.amountCharged)
        assertEquals(1, result.chargedHours)
    }

    @Test
    fun `deve cobrar 1 hora para sessao de 65 minutos dentro da tolerancia de fracao`() {
        val start = LocalDateTime.of(2024, 6, 3, 10, 0)
        val result = billingService.calculateCharge(start, start.plusMinutes(65))
        assertEquals(BigDecimal("2.00"), result.amountCharged)
        assertEquals(1, result.chargedHours)
    }

    @Test
    fun `deve cobrar 2 horas para sessao de 66 minutos excedendo tolerancia de fracao`() {
        val start = LocalDateTime.of(2024, 6, 3, 10, 0)
        val result = billingService.calculateCharge(start, start.plusMinutes(66))
        assertEquals(BigDecimal("4.00"), result.amountCharged)
        assertEquals(2, result.chargedHours)
    }

    @Test
    fun `deve cobrar 2 horas para sessao de 2 horas exatas`() {
        val start = LocalDateTime.of(2024, 6, 3, 10, 0)
        val result = billingService.calculateCharge(start, start.plusHours(2))
        assertEquals(BigDecimal("4.00"), result.amountCharged)
        assertEquals(2, result.chargedHours)
    }

    @Test
    fun `deve retornar zero para sessao em domingo`() {
        val start = LocalDateTime.of(2024, 6, 2, 10, 0)  // domingo
        val result = billingService.calculateCharge(start, start.plusHours(2))
        assertEquals(BigDecimal.ZERO, result.amountCharged)
        assertEquals(BillingCategory.OUT_OF_HOURS, result.category)
    }

    @Test
    fun `deve retornar zero para sessao fora do horario comercial meia noite`() {
        val start = LocalDateTime.of(2024, 6, 3, 0, 0)  // segunda 00:00
        val result = billingService.calculateCharge(start, start.plusHours(2))
        assertEquals(BigDecimal.ZERO, result.amountCharged)
    }

    @Test
    fun `deve retornar zero para sessao sabado apos 13h`() {
        val start = LocalDateTime.of(2024, 6, 1, 14, 0)  // sábado 14:00
        val result = billingService.calculateCharge(start, start.plusHours(2))
        assertEquals(BigDecimal.ZERO, result.amountCharged)
    }

    @Test
    fun `deve cobrar corretamente para sessao no sabado dentro do horario`() {
        val start = LocalDateTime.of(2024, 6, 1, 8, 30)  // sábado 08:30
        val result = billingService.calculateCharge(start, start.plusMinutes(60))
        assertEquals(BigDecimal("2.00"), result.amountCharged)
        assertEquals(1, result.chargedHours)
    }

    @Test
    fun `deve cobrar apenas minutos dentro do horario comercial para sessao que cruza limite`() {
        val start = LocalDateTime.of(2024, 6, 3, 17, 30)  // segunda 17:30
        val result = billingService.calculateCharge(start, start.plusMinutes(60))
        // 30 min billable (17:30–18:00) > tolerância de 15min → 1 hora = R$2,00
        assertEquals(BigDecimal("2.00"), result.amountCharged)
        assertEquals(1, result.chargedHours)
    }

    @Test
    fun `deve detectar overtime apos 2 horas`() {
        val start = LocalDateTime.now().minusHours(3)
        assertTrue(billingService.isOvertimeExceeded(start))
    }

    @Test
    fun `nao deve detectar overtime com menos de 2 horas`() {
        val start = LocalDateTime.now().minusMinutes(90)
        assertFalse(billingService.isOvertimeExceeded(start))
    }
}
