package no.nav.tiltakspenger.utbetaling.domene

import java.time.LocalDate

class Satser() {
    companion object {
        private val satser = listOf(
            Sats(LocalDate.of(2023, 1, 1), LocalDate.of(2023, 12, 31), 268, 201, 52, 39),
            Sats(LocalDate.of(2024, 1, 1), LocalDate.of(9999, 12, 31), 285, 214, 53, 40),
        )

        fun sats(date: LocalDate): Sats {
            return satser.find { it.fom <= date && it.tom >= date }
                ?: throw IllegalArgumentException("Fant ingen sats for dato $date")
        }
    }
}

data class Sats(
    val fom: LocalDate,
    val tom: LocalDate,
    val sats: Int,
    val satsDelvis: Int,
    val satsBarnetillegg: Int,
    val satsBarnetilleggDelvis: Int,
)
