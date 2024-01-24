package no.nav.tiltakspenger.utbetaling.service

import no.nav.dagpenger.kontrakter.iverksett.IverksettDto

interface UtbetalingService {
    suspend fun sendUtbetalingTilIverksett(utbetalingDTOUt: IverksettDto)
}
