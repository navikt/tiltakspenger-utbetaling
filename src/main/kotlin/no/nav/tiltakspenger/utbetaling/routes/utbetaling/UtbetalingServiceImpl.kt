package no.nav.tiltakspenger.utbetaling.routes.utbetaling

import io.ktor.server.config.ApplicationConfig
import no.nav.dagpenger.kontrakter.iverksett.IverksettDto
import no.nav.tiltakspenger.utbetaling.routes.iverksett.IverksettClient

class UtbetalingServiceImpl(
    applicationConfig: ApplicationConfig,
    private val iverksettClient: IverksettClient = IverksettClient(applicationConfig),
) : UtbetalingService {

    override suspend fun sendUtbetalingTilIverksett(utbetalingDTOUt: IverksettDto) {
        iverksettClient.iverksett(utbetalingDTOUt)
    }
}
