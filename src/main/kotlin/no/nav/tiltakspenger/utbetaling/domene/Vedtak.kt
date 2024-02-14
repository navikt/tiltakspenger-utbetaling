package no.nav.tiltakspenger.utbetaling.domene

import java.time.LocalDateTime

data class Vedtak(
    val id: VedtakId,
    val sakId: SakId,
    val utløsendeId: String,
    val ident: String,
    val vedtakstidspunkt: LocalDateTime,
    val antallBarn: Int,
    val brukerNavkontor: String,
    val saksbehandler: String,
    val beslutter: String,
    val utbetalinger: List<UtbetalingDag>,
    val forrigeVedtak: VedtakId?,
)

fun Vedtak.nyttVedtak(
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
