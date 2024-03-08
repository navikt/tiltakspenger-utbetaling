package no.nav.tiltakspenger.utbetaling.routes.utbetaling

import java.time.LocalDate

data class VedtakDTO(
    val id: String,
    val fom: LocalDate,
    val tom: LocalDate,
    val sats: Int,
    val satsDelvis: Int,
    val satsBarnetillegg: Int,
    val satsBarnetilleggDelvis: Int,
    val antallBarn: Int,
    val totalbeløp: Int,
    val vedtakDager: List<VedtakDagDTO>,
)

data class VedtakDagDTO(
    val beløp: Int,
    val dato: LocalDate,
    val tiltakType: String,
    val status: String,
)
