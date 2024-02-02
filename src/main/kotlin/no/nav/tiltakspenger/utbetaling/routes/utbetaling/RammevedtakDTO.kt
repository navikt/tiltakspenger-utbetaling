package no.nav.tiltakspenger.utbetaling.routes.utbetaling

import java.time.LocalDate
import java.time.LocalDateTime

data class RammevedtakDTO(
    val sakId: String,
    val behandlingId: String,
    val personIdent: String,
    val fom: LocalDate,
    val tom: LocalDate,
    val vedtakUtfall: VedtakUtfall,
    val vedtaktidspunkt: LocalDateTime,
    val saksbehandler: String,
    val beslutter: String,
)

enum class VedtakUtfall {
    INNVILGET,
    AVSLÅTT,
    OPPHØRT,
}
