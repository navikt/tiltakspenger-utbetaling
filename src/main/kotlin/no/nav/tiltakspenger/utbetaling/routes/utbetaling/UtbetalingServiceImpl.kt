package no.nav.tiltakspenger.utbetaling.routes.utbetaling

import io.ktor.server.config.ApplicationConfig
import no.nav.dagpenger.kontrakter.iverksett.IverksettDto
import no.nav.tiltakspenger.utbetaling.routes.iverksett.IverksettKlient

class UtbetalingServiceImpl(
    applicationConfig: ApplicationConfig,
    private val iverksettKlient: IverksettKlient = IverksettKlient(applicationConfig),
) : UtbetalingService {

    override suspend fun sendUtbetalingTilIverksett(utbetalingDTOUt: IverksettDto) {
        iverksettKlient.iverksett(utbetalingDTOUt)
    }
}
