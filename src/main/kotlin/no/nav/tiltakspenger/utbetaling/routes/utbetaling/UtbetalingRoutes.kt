package no.nav.tiltakspenger.utbetaling.routes.utbetaling

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import mu.KotlinLogging

private val LOG = KotlinLogging.logger {}

internal const val utbetalingPath = "/utbetaling"


fun Route.utbetaling() {
    post("$utbetalingPath/{id}"){
        LOG.info("Mottatt request på $utbetalingPath/id")

        val id = call.parameters["id"]?.let {
            LOG.info { "iden var ikke null! Og er $it" }
        }

        call.respond(status = HttpStatusCode.OK, "Kallet fungerte")

    }
}