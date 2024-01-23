package no.nav.tiltakspenger.utbetaling.service

import no.nav.dagpenger.kontrakter.iverksett.IverksettDto
import no.nav.tiltakspenger.utbetaling.client.iverksett.IverksettKlient

class UtbetalingServiceImpl(
    private val iverksettKlient: IverksettKlient,
) : UtbetalingService {

    override suspend fun sendUtbetalingTilIverksett(utbetalingDTOUt: IverksettDto) {
        iverksettKlient.iverksett(utbetalingDTOUt)
    }
}
