package no.nav.tiltakspenger.utbetaling.routes.utbetaling

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import mu.KotlinLogging
import no.nav.dagpenger.kontrakter.felles.StønadTypeTiltakspenger
import no.nav.dagpenger.kontrakter.iverksett.IverksettDto
import no.nav.dagpenger.kontrakter.iverksett.UtbetalingDto
import no.nav.dagpenger.kontrakter.iverksett.VedtakType
import no.nav.dagpenger.kontrakter.iverksett.VedtaksdetaljerDto
import no.nav.dagpenger.kontrakter.iverksett.Vedtaksresultat
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

private val LOG = KotlinLogging.logger {}

internal const val utbetalingPath = "/utbetaling"

fun Route.utbetaling(utbetalingService: UtbetalingService) {
    post(utbetalingPath) {
        LOG.info("Mottatt request på $utbetalingPath")
        val utbetalingDTOUt = utbetalingsObjektMock // call.receive<UtbetalingDTOUt>()

        utbetalingService.sendUtbetalingTilIverksett(utbetalingDTOUt)

        call.respond(status = HttpStatusCode.OK, "Kallet fungerte")
    }
}

private val utbetalingsObjektMock: IverksettDto =
    IverksettDto(
        sakId = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"),
        behandlingId = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"),
        saksreferanse = "",
        personIdent = "26878898396",
        vedtak = VedtaksdetaljerDto(
            vedtakstype = VedtakType.UTBETALINGSVEDTAK,
            vedtakstidspunkt = LocalDateTime.now(),
            resultat = Vedtaksresultat.INNVILGET,
            saksbehandlerId = "1233489712",
            beslutterId = "beh_12039k1nmn1230194",
            utbetalinger = listOf(
                UtbetalingDto(
                    belopPerDag = 268,
                    fraOgMedDato = LocalDate.parse("2023-10-25"),
                    tilOgMedDato = LocalDate.parse("2023-10-25"),
                    stonadstype = StønadTypeTiltakspenger.TILTAKSPENGER,
                ),
            ),
        ),
    )
