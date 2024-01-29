package no.nav.tiltakspenger.utbetaling.domene

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class Rammevedtak(
    val id: RammevedtakId,
    val sakId: SakId,
    val saksnummer: String,
    val behandlingId: BehandlingId,
    val personIdent: String,
    val fom: LocalDate,
    val tom: LocalDate,
    val iverksettingResultat: IverksettingResultat,
    val vedtakstidspunkt: LocalDateTime,
    val saksbehandler: String,
    val beslutter: String,
)

enum class IverksettingResultat {
    INNVILGET,
    OPPHØRT,
    AVSLÅTT,
}
