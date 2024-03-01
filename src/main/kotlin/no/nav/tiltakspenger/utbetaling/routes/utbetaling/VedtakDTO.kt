package no.nav.tiltakspenger.utbetaling.routes.utbetaling

import no.nav.tiltakspenger.utbetaling.domene.Satser
import no.nav.tiltakspenger.utbetaling.domene.Vedtak
import no.nav.tiltakspenger.utbetaling.service.mapBarnetilleggSats
import no.nav.tiltakspenger.utbetaling.service.mapSats
import java.time.LocalDate

data class VedtakDTO(
    val id: String,
    val fom: LocalDate,
    val tom: LocalDate,
    val sats: Int,
    val satsDelvis: Int,
    val satsBarnetillegg: Int,
    val satsBarnetilleggDelvis: Int,
    val antallBarn: Int,
    val totalbeløp: Int,
    val vedtakDager: List<VedtakDagDTO>,
)

data class VedtakDagDTO(
    val beløp: Int,
    val dato: LocalDate,
    val tiltakType: String,
    val status: String,
)

fun mapVedtak(vedtak: Vedtak): VedtakDTO {
    val sisteLøpenummer = vedtak.utbetalinger.maxOfOrNull { it.løpenr } ?: 0
    val utbetalingerForSisteLøpenummer = vedtak.utbetalinger.filter { it.løpenr == sisteLøpenummer }
    val førsteUtbetalingsDag = utbetalingerForSisteLøpenummer.minByOrNull { it.dato }?.dato ?: LocalDate.of(1970, 1, 1)
    val sisteUtbetalingsDag = utbetalingerForSisteLøpenummer.maxByOrNull { it.dato }?.dato ?: LocalDate.of(9999, 12, 31)
    val sats = Satser.sats(førsteUtbetalingsDag)
    return VedtakDTO(
        id = vedtak.id.toString(),
        fom = førsteUtbetalingsDag,
        tom = sisteUtbetalingsDag,
        sats = sats.sats,
        satsDelvis = sats.satsDelvis,
        satsBarnetillegg = sats.satsBarnetillegg,
        satsBarnetilleggDelvis = sats.satsBarnetilleggDelvis,
        antallBarn = vedtak.antallBarn,
        totalbeløp = utbetalingerForSisteLøpenummer.sumOf { it.mapSats() + it.mapBarnetilleggSats(vedtak.antallBarn) },
        vedtakDager = utbetalingerForSisteLøpenummer.sortedBy { it.dato }.map {
            VedtakDagDTO(
                beløp = it.mapSats() + it.mapBarnetilleggSats(vedtak.antallBarn),
                dato = it.dato,
                tiltakType = it.tiltaktype.name,
                status = it.status.name,
            )
        },
    )
}
