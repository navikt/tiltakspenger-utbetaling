package no.nav.tiltakspenger.utbetaling.routes.utbetaling

import no.nav.dagpenger.kontrakter.iverksett.IverksettDto

interface UtbetalingService {
    suspend fun sendUtbetalingTilIverksett(utbetalingDTOUt: IverksettDto)
}
