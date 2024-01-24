package no.nav.tiltakspenger.utbetaling.routes.utbetaling

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import mu.KotlinLogging
import no.nav.dagpenger.kontrakter.iverksett.IverksettDto
import no.nav.tiltakspenger.utbetaling.service.UtbetalingService

private val LOG = KotlinLogging.logger {}

internal const val utbetalingPath = "/utbetaling"

fun Route.utbetaling(utbetalingService: UtbetalingService) {
    post(utbetalingPath) {
        LOG.info("Mottatt request på $utbetalingPath")
        val utbetalingDTOUt = call.receive<IverksettDto>()

        val response = utbetalingService.sendUtbetalingTilIverksett(utbetalingDTOUt)

        call.respond(status = HttpStatusCode.OK, response)
    }
}
