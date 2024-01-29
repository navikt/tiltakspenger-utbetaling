package no.nav.tiltakspenger.utbetaling.routes.utbetaling

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import mu.KotlinLogging
import no.nav.tiltakspenger.utbetaling.domene.BehandlingId
import no.nav.tiltakspenger.utbetaling.domene.Rammevedtak
import no.nav.tiltakspenger.utbetaling.domene.RammevedtakId
import no.nav.tiltakspenger.utbetaling.domene.SakId
import no.nav.tiltakspenger.utbetaling.service.UtbetalingService

private val LOG = KotlinLogging.logger {}

internal const val utbetalingPath = "/utbetaling"

fun Route.utbetaling(utbetalingService: UtbetalingService) {
    post("$utbetalingPath/rammevedtak") {
        LOG.info("Mottatt request på $utbetalingPath")
        val rammevedtakDTO = call.receive<RammevedtakDTO>()

        val rammevedtak = mapRammevedtak(rammevedtakDTO)
        val response = utbetalingService.mottaRammevedtakOgSendTilIverksett(rammevedtak)

        call.respond(status = HttpStatusCode.OK, response)
    }
}

private fun mapRammevedtak(dto: RammevedtakDTO) = Rammevedtak(
    id = RammevedtakId.random(),
    sakId = SakId.fromDb(dto.sakId),
    saksnummer = dto.saksnummer,
    behandlingId = BehandlingId.fromDb(dto.behandlingId),
    personIdent = dto.personIdent,
    fom = dto.fom,
    tom = dto.tom,
    iverksettingResultat = when (dto.iverksettingResultat) {
        IverksettingResultat.INNVILGET -> no.nav.tiltakspenger.utbetaling.domene.IverksettingResultat.INNVILGET
        IverksettingResultat.AVSLÅTT -> no.nav.tiltakspenger.utbetaling.domene.IverksettingResultat.AVSLÅTT
    },
    vedtakstidspunkt = dto.vedtaktidspunkt,
    saksbehandler = dto.saksbehandler,
    beslutter = dto.beslutter,
)
