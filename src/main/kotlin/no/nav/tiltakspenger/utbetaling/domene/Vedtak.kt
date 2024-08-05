package no.nav.tiltakspenger.utbetaling.domene

import no.nav.tiltakspenger.libs.common.VedtakId
import java.time.LocalDate
import java.time.LocalDateTime

data class Vedtak(
    val id: VedtakId,
    val sakId: SakId,
    val utløsendeId: String,
    val ident: String,
    val vedtakstidspunkt: LocalDateTime,
    val brukerNavkontor: String,
    val saksbehandler: String,
    val beslutter: String,
    val utbetalinger: List<UtbetalingDag>,
    val utfallsperioder: List<Utfallsperiode>,
    val forrigeVedtak: VedtakId?,
)

fun List<Utfallsperiode>.antallBarn(date: LocalDate): Int {
    return this.find { it.fom <= date && it.tom >= date }?.antallBarn
        ?: throw IllegalArgumentException("Fant ingen barn for dato $date")
}

fun Vedtak.nyttUtbetalingVedtak(
    saksbehandler: String,
    utløsendeId: String,
    utbetalinger: List<UtbetalingDag>,
) = this.copy(
    id = VedtakId.random(),
    utløsendeId = utløsendeId,
    vedtakstidspunkt = LocalDateTime.now(),
    saksbehandler = saksbehandler,
    beslutter = saksbehandler,
    utbetalinger = utbetalinger,
    forrigeVedtak = this.id,
)

enum class VedtakUtfall {
    INNVILGET,
    OPPHØRT,
    AVSLÅTT,
}
