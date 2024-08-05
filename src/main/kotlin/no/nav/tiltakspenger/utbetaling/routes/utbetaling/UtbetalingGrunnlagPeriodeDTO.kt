package no.nav.tiltakspenger.utbetaling.routes.utbetaling

import no.nav.tiltakspenger.libs.common.BehandlingId
import no.nav.tiltakspenger.utbetaling.service.UtbetalingGrunnlagDag
import java.time.LocalDate

data class GrunnlagDTO(
    val behandlingId: BehandlingId,
    val fom: LocalDate,
    val tom: LocalDate,
)

data class UtbetalingGrunnlagPeriodeDTO(
    val antallBarn: Int,
    val sats: Int,
    val satsDelvis: Int,
    val satsBarn: Int,
    val satsBarnDelvis: Int,
    val fom: LocalDate,
    val tom: LocalDate,
)

fun mapGrunnlag(dager: List<UtbetalingGrunnlagDag>): List<UtbetalingGrunnlagPeriodeDTO> {
    val grunnlagDager = dager.fold(emptyList<UtbetalingGrunnlagDag>()) { liste, nesteDag ->
        liste.slåSammen(nesteDag)
    }
    return grunnlagDager.map {
        UtbetalingGrunnlagPeriodeDTO(
            antallBarn = it.antallBarn,
            sats = it.sats,
            satsDelvis = it.satsDelvis,
            satsBarn = it.satsBarn,
            satsBarnDelvis = it.satsBarnDelvis,
            fom = it.fom,
            tom = it.tom,
        )
    }
}

private fun List<UtbetalingGrunnlagDag>.slåSammen(neste: UtbetalingGrunnlagDag): List<UtbetalingGrunnlagDag> {
    if (this.isEmpty()) return listOf(neste)
    val forrige = this.last()
    return if (forrige == neste) {
        this.dropLast(1) + forrige.copy(
            tom = neste.tom,
        )
    } else {
        this + neste
    }
}
