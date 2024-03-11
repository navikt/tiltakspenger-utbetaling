package no.nav.tiltakspenger.utbetaling.routes.utbetaling

import no.nav.tiltakspenger.utbetaling.domene.SakId
import no.nav.tiltakspenger.utbetaling.domene.UtfallForPeriode
import no.nav.tiltakspenger.utbetaling.domene.Utfallsperiode
import no.nav.tiltakspenger.utbetaling.domene.Vedtak
import no.nav.tiltakspenger.utbetaling.domene.VedtakId
import java.time.LocalDate
import java.time.LocalDateTime

data class RammevedtakDTO(
    val sakId: String,
    val utløsendeId: String,
    val ident: String,
    val brukerNavkontor: String,
    val utfallsperioder: List<UtfallsperiodeDTO>,
    val vedtaktidspunkt: LocalDateTime,
    val saksbehandler: String,
    val beslutter: String,
)

data class UtfallsperiodeDTO(
    val fom: LocalDate,
    val tom: LocalDate,
    val antallBarn: Int,
    val utfall: UtfallForPeriodeDTO,
)

enum class UtfallForPeriodeDTO {
    GIR_RETT_TILTAKSPENGER,
    GIR_IKKE_RETT_TILTAKSPENGER,
    KREVER_MANUELL_VURDERING,
}

fun mapRammevedtak(dto: RammevedtakDTO) = Vedtak(
    id = VedtakId.random(),
    sakId = SakId.fromDb(dto.sakId),
    utløsendeId = dto.utløsendeId,
    ident = dto.ident,
    brukerNavkontor = dto.brukerNavkontor,
    vedtakstidspunkt = dto.vedtaktidspunkt,
    saksbehandler = dto.saksbehandler,
    beslutter = dto.beslutter,
    utbetalinger = emptyList(),
    utfallsperioder = dto.utfallsperioder.map {
        Utfallsperiode(
            fom = it.fom,
            tom = it.tom,
            antallBarn = it.antallBarn,
            utfall = when (it.utfall) {
                UtfallForPeriodeDTO.GIR_RETT_TILTAKSPENGER -> UtfallForPeriode.GIR_RETT_TILTAKSPENGER
                UtfallForPeriodeDTO.GIR_IKKE_RETT_TILTAKSPENGER -> UtfallForPeriode.GIR_IKKE_RETT_TILTAKSPENGER
                UtfallForPeriodeDTO.KREVER_MANUELL_VURDERING -> UtfallForPeriode.KREVER_MANUELL_VURDERING
            },
        )
    },
    forrigeVedtak = null,
)
