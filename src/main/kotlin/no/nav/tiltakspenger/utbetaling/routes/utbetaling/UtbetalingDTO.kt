package no.nav.tiltakspenger.utbetaling.routes.utbetaling

import no.nav.tiltakspenger.utbetaling.domene.SakId
import no.nav.tiltakspenger.utbetaling.domene.TiltakType
import no.nav.tiltakspenger.utbetaling.domene.Utbetaling
import no.nav.tiltakspenger.utbetaling.domene.UtbetalingDag
import no.nav.tiltakspenger.utbetaling.domene.UtbetalingDagStatus
import java.time.LocalDate
import java.util.UUID

data class UtbetalingDTO(
    val sakId: String,
    val utløsendeMeldekortId: String,
    val utbetalingDager: List<UtbetalingDagDTO>,
    val saksbehandler: String,
)

data class UtbetalingDagDTO(
    val dato: LocalDate,
    val tiltaktype: String,
    val status: UtbetalingDagStatusDTO,
    val meldekortId: UUID,
    val løpenr: Int,
)

enum class UtbetalingDagStatusDTO {
    IngenUtbetaling,
    FullUtbetaling,
    DelvisUtbetaling,
}

fun mapUtbetalingVedtak(dto: UtbetalingDTO) = Utbetaling(
    sakId = SakId(dto.sakId),
    utløsendeMeldekortId = dto.utløsendeMeldekortId,
    utbetalingDager = dto.utbetalingDager.map { dag ->
        UtbetalingDag(
            dato = dag.dato,
            tiltaktype = TiltakType.valueOf(dag.tiltaktype),
            status = when (dag.status) {
                UtbetalingDagStatusDTO.FullUtbetaling -> UtbetalingDagStatus.FullUtbetaling
                UtbetalingDagStatusDTO.DelvisUtbetaling -> UtbetalingDagStatus.DelvisUtbetaling
                UtbetalingDagStatusDTO.IngenUtbetaling -> UtbetalingDagStatus.IngenUtbetaling
            },
            meldekortId = dag.meldekortId,
            løpenr = dag.løpenr,
        )
    },
    saksbehandler = dto.saksbehandler,
)
