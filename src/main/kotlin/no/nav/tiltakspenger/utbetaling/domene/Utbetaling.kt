package no.nav.tiltakspenger.utbetaling.domene

import java.time.LocalDate
import java.util.*

data class Utbetaling(
    val sakId: SakId,
    val utløsendeMeldekortId: UUID,
    val utbetalingDager: List<UtbetalingDag>,
    val saksbehandler: String,
)

data class UtbetalingDag(
    val dato: LocalDate,
    val tiltaktype: TiltakType,
    val status: UtbetalingDagStatus,
)

enum class UtbetalingDagStatus {
    IngenUtbetaling,
    FullUtbetaling,
    DelvisUtbetaling,
}

enum class TiltakType {
    GRUPPEAMO,
    ENKELTAMO,
}
