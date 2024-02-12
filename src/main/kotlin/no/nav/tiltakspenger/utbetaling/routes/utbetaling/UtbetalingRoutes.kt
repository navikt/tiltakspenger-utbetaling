package no.nav.tiltakspenger.utbetaling.routes.utbetaling

import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import mu.KotlinLogging
import no.nav.tiltakspenger.utbetaling.service.UtbetalingService

private val LOG = KotlinLogging.logger {}

internal const val utbetalingPath = "/utbetaling"

fun Route.utbetaling(utbetalingService: UtbetalingService) {
    post("$utbetalingPath/rammevedtak") {
        LOG.info("Mottatt rammevedtak på $utbetalingPath/rammevedtak")
        val rammevedtakDTO = call.receive<RammevedtakDTO>()

        val rammevedtak = mapRammevedtak(rammevedtakDTO)
        val response = utbetalingService.mottaRammevedtakOgSendTilIverksett(rammevedtak)

        call.respond(status = response.statusCode, response.melding)
    }

    post("$utbetalingPath/utbetalingvedtak") {
        LOG.info("Mottatt utbetalingvedtak på $utbetalingPath/utbetalingvedtak")
        val utbetalingDTO = call.receive<UtbetalingDTO>()

        val utbetaling = mapUtbetalingVedtak(utbetalingDTO)
        val response = utbetalingService.mottaUtbetalingOgSendTilIverksett(utbetaling)

        call.respond(status = response.statusCode, response.melding)
    }
}
