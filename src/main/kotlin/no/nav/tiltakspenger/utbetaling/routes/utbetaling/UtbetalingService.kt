package no.nav.tiltakspenger.utbetaling.routes.utbetaling

interface UtbetalingService {
    suspend fun sendUtbetalingTilIverksett(utbetalingDTOUt: UtbetalingDTOUt)
}
