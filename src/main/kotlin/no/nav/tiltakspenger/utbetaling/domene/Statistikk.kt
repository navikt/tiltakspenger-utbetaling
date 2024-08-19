package no.nav.tiltakspenger.utbetaling.domene

import java.time.LocalDate

data class Statistikk(
    val posteringId: String,
    val sakId: String,
    val beløp: Double,
    val beløpBeskrivelse: String,
    val årsak: String,
    val posteringDato: LocalDate,
    val gyldigFraDatoPostering: LocalDate,
    val gyldigTilDatoPostering: LocalDate,
)
