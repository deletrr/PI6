package com.pontolivre.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import java.math.BigDecimal

@Configuration
class ParkingRulesConfig {

    @Value("\${parking.rate-per-hour:2.00}")
    var ratePerHourRaw: String = "2.00"

    @Value("\${parking.free-tolerance-minutes:15}")
    var freeToleranceMinutes: Long = 15L

    @Value("\${parking.max-session-hours:2}")
    var maxSessionHours: Long = 2L

    @Value("\${parking.overtime-fine:10.00}")
    var overtimeFineRaw: String = "10.00"

    val ratePerHour: BigDecimal get() = BigDecimal(ratePerHourRaw)
    val overtimeFine: BigDecimal get() = BigDecimal(overtimeFineRaw)
}
