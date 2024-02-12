package no.nav.tiltakspenger.utbetaling.routes.utbetaling

import no.nav.tiltakspenger.utbetaling.domene.SakId
import no.nav.tiltakspenger.utbetaling.domene.Vedtak
import no.nav.tiltakspenger.utbetaling.domene.VedtakId
import java.time.LocalDate
import java.time.LocalDateTime

data class RammevedtakDTO(
    val sakId: String,
    val gjeldendeVedtakId: String,
    val ident: String,
    val fom: LocalDate,
    val tom: LocalDate,
    val antallBarn: Int,
    val brukerNavkontor: String,
    val vedtaktidspunkt: LocalDateTime,
    val saksbehandler: String,
    val beslutter: String,
)

fun mapRammevedtak(dto: RammevedtakDTO) = Vedtak(
    id = VedtakId.random(),
    sakId = SakId.fromDb(dto.sakId),
    gjeldendeVedtakId = dto.gjeldendeVedtakId,
    ident = dto.ident,
    fom = dto.fom,
    tom = dto.tom,
    antallBarn = dto.antallBarn,
    brukerNavkontor = dto.brukerNavkontor,
    vedtakstidspunkt = dto.vedtaktidspunkt,
    saksbehandler = dto.saksbehandler,
    beslutter = dto.beslutter,
    utbetalinger = emptyList(),
    forrigeVedtak = null,
)
