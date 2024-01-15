package no.nav.tiltakspenger.utbetaling.routes.utbetaling

import java.time.LocalDate
import java.util.*

data class UtbetalingReqDTO(
    val meldekortId: UUID,
    val fom: LocalDate,
    val tom: LocalDate,
    val meldekortDager: List<MeldekortDag>, // Egen DTO?
    val saksbehandler: String,
)

data class MeldekortDag(
    val dato: LocalDate,
    val tiltak: Tiltak?,
    val status: MeldekortDagStatus,
)

data class Tiltak(
    val id: UUID,
    val periode: Periode,
    val typeBeskrivelse: String,
    val typeKode: String,
    val antDagerIUken: Float,
)

data class Periode(
    val fra: LocalDate,
    val til: LocalDate,
)

enum class MeldekortDagStatus(status: String) {
    IKKE_UTFYLT("Ikke utfylt"),
    DELTATT("Deltatt"),
    IKKE_DELTATT("Ikke deltatt"),
    FRAVÆR_SYK("Fravær syk"),
    FRAVÆR_SYKT_BARN("Fravær sykt barn"),
    FRAVÆR_VELFERD("Fravær velferd"),
    LØNN_FOR_TID_I_ARBEID("Lønn for tid i arbeid"),
}
