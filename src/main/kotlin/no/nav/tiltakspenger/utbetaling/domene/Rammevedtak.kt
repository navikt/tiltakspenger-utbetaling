package no.nav.tiltakspenger.utbetaling.domene

import java.time.LocalDate
import java.time.LocalDateTime

data class Rammevedtak(
    val id: RammevedtakId,
    val sakId: SakId,
    val behandlingId: BehandlingId,
    val personIdent: String,
    val fom: LocalDate,
    val tom: LocalDate,
    val vedtakUtfall: VedtakUtfall,
    val vedtakstidspunkt: LocalDateTime,
    val saksbehandler: String,
    val beslutter: String,
)

enum class VedtakUtfall {
    INNVILGET,
    OPPHØRT,
    AVSLÅTT,
}
