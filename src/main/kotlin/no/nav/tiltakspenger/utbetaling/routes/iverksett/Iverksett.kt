package no.nav.tiltakspenger.utbetaling.routes.iverksett

import no.nav.tiltakspenger.utbetaling.routes.utbetaling.UtbetalingDTOUt

interface Iverksett {
    suspend fun iverksett(utbetalingDTOUt: UtbetalingDTOUt): String
}
