package no.nav.tiltakspenger.utbetaling.routes.utbetaling

interface UtbetalingService {
    fun sendUtbetalingTilIverksett(utbetalingDTOUt: UtbetalingDTOUt)
}
