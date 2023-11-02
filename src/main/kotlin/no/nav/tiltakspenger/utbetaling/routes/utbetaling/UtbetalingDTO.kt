package no.nav.tiltakspenger.utbetaling.routes.utbetaling

import java.util.UUID

data class UtbetalingDTOUt(
    val sakId: UUID,
    val behandlingId: String,
    val personIdent: String,
    val vedtak: Vedtak,
    val forrigeIverksetting: ForrigeIverksetting,
)

data class Vedtak(
    val vedtaksType: String = "RAMMEVEDTAK",
    val vedtakstidspunkt: String,
    val resultat: Resultat,
    val saksbehandlerId: String,
    val beslutterId: String,
    val utbetalinger: List<Utbetaling>,
)

data class ForrigeIverksetting(
    val behandlingId: UUID,
)

enum class Resultat {
    INNVILGET, AVSLÅTT
}

enum class Stonadstype {
    TILTAKSPENGER,
}

data class Utbetaling(
    val belopPerDag: Int,
    val fraOgMedDato: String,
    val tilOgMedDato: String,
    val stonadstype: Stonadstype,
)
