package no.nav.tiltakspenger.utbetaling.domene

import java.time.LocalDate

data class GrunnlagDTO(
    val behandlingId: BehandlingId,
    val fom: LocalDate,
    val tom: LocalDate,
)
