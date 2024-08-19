package no.nav.tiltakspenger.utbetaling.service

import no.nav.tiltakspenger.utbetaling.domene.Statistikk
import no.nav.utsjekk.kontrakter.iverksett.IverksettV2Dto
import java.time.LocalDate
import java.time.temporal.ChronoUnit

fun mapStatistikk(iverksettDto: IverksettV2Dto): Statistikk {
    return Statistikk(
        posteringId = iverksettDto.behandlingId,
        sakId = iverksettDto.sakId,
        beløp = beregnBeløp(iverksettDto),
        beløpBeskrivelse = "",
        årsak = "",
        posteringDato = LocalDate.now(),
        gyldigFraDatoPostering = iverksettDto.vedtak.utbetalinger.minOf { it.fraOgMedDato },
        gyldigTilDatoPostering = iverksettDto.vedtak.utbetalinger.maxOf { it.tilOgMedDato },
    )
}

private fun beregnBeløp(iverksettDto: IverksettV2Dto): Double {
    return iverksettDto.vedtak.utbetalinger.sumOf {
        (it.beløp * ChronoUnit.DAYS.between(it.tilOgMedDato, it.fraOgMedDato).toUInt()).toDouble()
    }
}
