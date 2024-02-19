package no.nav.tiltakspenger.utbetaling.routes.utbetaling

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import mu.KotlinLogging
import no.nav.tiltakspenger.utbetaling.domene.BehandlingId
import no.nav.tiltakspenger.utbetaling.domene.VedtakId
import no.nav.tiltakspenger.utbetaling.service.UtbetalingService

private val LOG = KotlinLogging.logger {}

internal const val utbetalingPath = "/utbetaling"

fun Route.utbetaling(utbetalingService: UtbetalingService) {
    post("$utbetalingPath/rammevedtak") {
        LOG.info { "Mottatt rammevedtak på $utbetalingPath/rammevedtak" }
        val rammevedtakDTO = call.receive<RammevedtakDTO>()

        val rammevedtak = mapRammevedtak(rammevedtakDTO)
        val response = utbetalingService.mottaRammevedtakOgSendTilIverksett(rammevedtak)

        call.respond(status = response.statusCode, response.melding)
    }

    post("$utbetalingPath/utbetalingvedtak") {
        LOG.info { "Mottatt utbetalingvedtak på $utbetalingPath/utbetalingvedtak" }
        val utbetalingDTO = call.receive<UtbetalingDTO>()

        val utbetaling = mapUtbetalingVedtak(utbetalingDTO)
        val response = utbetalingService.mottaUtbetalingOgSendTilIverksett(utbetaling)

        call.respond(status = response.statusCode, response.melding)
    }

    get("$utbetalingPath/hentAlleForBehandling/{behandlingId}") {
        val id = call.parameters["behandlingId"]
        LOG.info { "hent alle utbetalingsvedtak for behandling $id" }

        checkNotNull(id) { "Mangler BehandlingId" }
        val behandlingId = BehandlingId.fromDb(id)
        val vedtak = utbetalingService.hentAlleVedtak(behandlingId).filterNot { it.utbetalinger.isEmpty() }

        call.respond(status = HttpStatusCode.OK, mapAlleVedtak(vedtak))
    }

    get("$utbetalingPath/hentVedtak/{vedtakId}") {
        val id = call.parameters["vedtakId"]
        LOG.info { "hent vedtak for id $id" }

        checkNotNull(id) { "Mangler VedtakId" }
        val vedtakId = VedtakId.fromDb(id)

        val vedtak = utbetalingService.hentVedtak(vedtakId)
        checkNotNull(vedtak) { "Fant ikke vedtak" }

        call.respond(status = HttpStatusCode.OK, mapVedtak(vedtak))
    }
}
