package no.nav.tiltakspenger.utbetaling.routes.utbetaling

import no.nav.tiltakspenger.utbetaling.domene.Vedtak
import no.nav.tiltakspenger.utbetaling.domene.antallBarn
import no.nav.tiltakspenger.utbetaling.service.mapBarnetilleggSats
import no.nav.tiltakspenger.utbetaling.service.mapSats
import java.time.LocalDate

data class VedtakUtenDagerDTO(
    val id: String,
    val fom: LocalDate,
    val tom: LocalDate,
    val beløp: Int,
)

fun mapAlleVedtak(vedtakListe: List<Vedtak>): List<VedtakUtenDagerDTO> {
    return vedtakListe.map { vedtak ->
        val utbetalingerForSisteLøpenummer = vedtak.utbetalinger.groupBy { it.løpenr }.maxBy { it.key }.value
        VedtakUtenDagerDTO(
            id = vedtak.id.toString(),
            fom = utbetalingerForSisteLøpenummer.minBy { it.dato }.dato,
            tom = utbetalingerForSisteLøpenummer.maxBy { it.dato }.dato,
            beløp = utbetalingerForSisteLøpenummer.sumOf { it.mapSats() + it.mapBarnetilleggSats(vedtak.utfallsperioder.antallBarn(it.dato)) },
        )
    }.sortedByDescending { it.fom }
}
