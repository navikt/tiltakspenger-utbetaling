package no.nav.tiltakspenger.utbetaling.routes.utbetaling

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import mu.KotlinLogging
import java.util.UUID

private val LOG = KotlinLogging.logger {}

internal const val utbetalingPath = "/utbetaling"

fun Route.utbetaling(utbetalingService: UtbetalingService) {
    post("$utbetalingPath") {
        LOG.info("Mottatt request på $utbetalingPath")
        val utbetalingDTOUt = utbetalingsObjektMock//call.receive<UtbetalingDTOUt>()

        utbetalingService.sendUtbetalingTilIverksett(utbetalingDTOUt)

        call.respond(status = HttpStatusCode.OK, "Kallet fungerte")
    }
}

private val utbetalingsObjektMock: UtbetalingDTOUt =
    UtbetalingDTOUt(
        sakId = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"),
        behandlingId = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
        personIdent = "1231241344",
        vedtak = Vedtak(
            vedtakstidspunkt = "2023-10-25T10:03:29.980Z",
            resultat = Resultat.INNVILGET,
            saksbehandlerId = "1233489712",
            beslutterId = "beh_12039k1nmn1230194",
            utbetalinger = listOf(
                Utbetaling(
                    belopPerDag = 0,
                    fraOgMedDato = "2023-10-25",
                    tilOgMedDato = "2023-10-25",
                    stonadstype = Stonadstype.TILTAKSPENGER
                ),
            )
        ),
        forrigeIverksetting = ForrigeIverksetting(
            behandlingId = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6")
        )
    )
