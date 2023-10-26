package no.nav.tiltakspenger.utbetaling.routes.utbetaling

import io.ktor.server.config.*
import no.nav.tiltakspenger.utbetaling.routes.iverksett.IverksettKlient

class UtbetalingServiceImpl(
    applicationConfig: ApplicationConfig,
    private val iverksettKlient: IverksettKlient = IverksettKlient(applicationConfig)
): UtbetalingService {

    override fun sendUtbetalingTilIverksett(utbetalingDTOUt: UtbetalingDTOUt) {
        iverksettKlient.iverksett()
    }

}