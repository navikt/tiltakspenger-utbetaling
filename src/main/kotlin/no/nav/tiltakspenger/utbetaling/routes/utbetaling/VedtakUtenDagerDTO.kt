package no.nav.tiltakspenger.utbetaling.routes.utbetaling

import no.nav.tiltakspenger.utbetaling.domene.Vedtak
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
        VedtakUtenDagerDTO(
            id = vedtak.id.toString(),
            fom = vedtak.utbetalinger.minByOrNull { it.dato }!!.dato,
            tom = vedtak.utbetalinger.maxByOrNull { it.dato }!!.dato,
            beløp = vedtak.utbetalinger.sumOf { it.mapSats() + it.mapBarnetilleggSats(vedtak.antallBarn) },
        )
    }
}
