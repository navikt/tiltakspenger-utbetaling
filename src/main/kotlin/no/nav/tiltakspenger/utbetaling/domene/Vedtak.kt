package no.nav.tiltakspenger.utbetaling.domene

import no.nav.dagpenger.kontrakter.felles.StønadTypeTiltakspenger
import no.nav.dagpenger.kontrakter.iverksett.StønadsdataTiltakspengerDto
import no.nav.dagpenger.kontrakter.iverksett.UtbetalingDto
import no.nav.tiltakspenger.utbetaling.service.BARNETILLEGG_SATS
import no.nav.tiltakspenger.utbetaling.service.REDUSERT_BARNETILLEGG_SATS
import no.nav.tiltakspenger.utbetaling.service.REDUSERT_SATS
import no.nav.tiltakspenger.utbetaling.service.SATS
import java.time.LocalDate
import java.time.LocalDateTime

data class Vedtak(
    val id: VedtakId,
    val sakId: SakId,
    val gjeldendeVedtakId: String,
    val ident: String,
    val fom: LocalDate,
    val tom: LocalDate,
    val vedtakstidspunkt: LocalDateTime,
    val antallBarn: Int,
    val brukerNavkontor: String,
    val saksbehandler: String,
    val beslutter: String,
    val utbetalinger: List<UtbetalingDag>,
    val forrigeVedtak: VedtakId?,
)

enum class VedtakUtfall {
    INNVILGET,
    OPPHØRT,
    AVSLÅTT,
}

fun UtbetalingDto.erLik(other: UtbetalingDto): Boolean {
    return other.stønadsdata.stønadstype == this.stønadsdata.stønadstype && other.beløpPerDag == this.beløpPerDag
}

fun Vedtak.toUtbetalingDto(): List<UtbetalingDto> {
    val utbetDager = utbetalinger.sortedBy { it.dato }
    val perioder = mutableListOf<UtbetalingDto>()

    perioder.add(lagDto(utbetDager.first()))
    utbetDager.takeLast(utbetDager.size - 1).forEach { dag ->
        val dto = lagDto(dag)
        if (dto.erLik(perioder.last())) {
            val oppdatertDag = perioder.last().copy(tilOgMedDato = dag.dato)
            perioder.removeLast()
            perioder.add(oppdatertDag)
        } else {
            perioder.add(dto)
        }
    }
    if (antallBarn > 0) {
        return perioder + this.toUtbetalingBarnDto()
    }
    return perioder
}

fun Vedtak.toUtbetalingBarnDto(): List<UtbetalingDto> {
    val utbetDager = utbetalinger.sortedBy { it.dato }
    val perioder = mutableListOf<UtbetalingDto>()

    perioder.add(lagBarneDto(antallBarn, utbetDager.first()))
    utbetDager.takeLast(utbetDager.size - 1).forEach { dag ->
        val dto = lagBarneDto(antallBarn, dag)
        if (dto.erLik(perioder.last())) {
            val oppdatertDag = perioder.last().copy(tilOgMedDato = dag.dato)
            perioder.removeLast()
            perioder.add(oppdatertDag)
        } else {
            perioder.add(dto)
        }
    }
    return perioder
}

private fun lagDto(dag: UtbetalingDag): UtbetalingDto {
    return UtbetalingDto(
        beløpPerDag = when (dag.status) {
            UtbetalingDagStatus.IngenUtbetaling -> 0
            UtbetalingDagStatus.FullUtbetaling -> SATS
            UtbetalingDagStatus.DelvisUtbetaling -> REDUSERT_SATS
        },
        fraOgMedDato = dag.dato,
        tilOgMedDato = dag.dato,
        stønadsdata = StønadsdataTiltakspengerDto(
            stønadstype = when (dag.tiltaktype) {
                TiltakType.GRUPPEAMO -> StønadTypeTiltakspenger.GRUPPE_AMO
                TiltakType.ENKELTAMO -> StønadTypeTiltakspenger.ENKELTPLASS_AMO
            },
            barnetillegg = false,
        ),
    )
}
private fun lagBarneDto(ant: Int, dag: UtbetalingDag): UtbetalingDto {
    return UtbetalingDto(
        beløpPerDag = when (dag.status) {
            UtbetalingDagStatus.IngenUtbetaling -> 0
            UtbetalingDagStatus.FullUtbetaling -> BARNETILLEGG_SATS * ant
            UtbetalingDagStatus.DelvisUtbetaling -> REDUSERT_BARNETILLEGG_SATS * ant
        },
        fraOgMedDato = dag.dato,
        tilOgMedDato = dag.dato,
        stønadsdata = StønadsdataTiltakspengerDto(
            stønadstype = when (dag.tiltaktype) {
                TiltakType.GRUPPEAMO -> StønadTypeTiltakspenger.GRUPPE_AMO
                TiltakType.ENKELTAMO -> StønadTypeTiltakspenger.ENKELTPLASS_AMO
            },
            barnetillegg = true,
        ),
    )
}
