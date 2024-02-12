package no.nav.tiltakspenger.utbetaling.routes.utbetaling

import io.kotest.matchers.shouldBe
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import io.ktor.http.path
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import io.ktor.server.util.url
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import no.nav.tiltakspenger.utbetaling.client.iverksett.IverksettKlient
import no.nav.tiltakspenger.utbetaling.domene.SakId
import no.nav.tiltakspenger.utbetaling.domene.Vedtak
import no.nav.tiltakspenger.utbetaling.jacksonSerialization
import no.nav.tiltakspenger.utbetaling.routes.defaultRequest
import no.nav.tiltakspenger.utbetaling.service.UtbetalingServiceImpl
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

internal class UtbetalingRoutesTest {
    private val utbetalingServiceMock = mockk<UtbetalingServiceImpl>()

    @Test
    fun `sjekk at man kan kalle rammevedtak`() {
        val okResponse = IverksettKlient.Response(
            statusCode = HttpStatusCode.OK,
            melding = "ok",
        )
        val capturedVedtak = slot<Vedtak>()
        coEvery { utbetalingServiceMock.mottaRammevedtakOgSendTilIverksett(capture(capturedVedtak)) } returns okResponse
        testApplication {
            application {
                jacksonSerialization()
                routing {
                    utbetaling(
                        utbetalingServiceMock,
                    )
                }
            }
            val respons = defaultRequest(
                HttpMethod.Post,
                url {
                    protocol = URLProtocol.HTTPS
                    path("$utbetalingPath/rammevedtak")
                },
            ) {
                setBody(rammevedtakJson)
            }

            respons.status shouldBe HttpStatusCode.OK
            respons.bodyAsText() shouldBe "ok"

            val expectedVedtak = Vedtak(
                id = capturedVedtak.captured.id,
                sakId = SakId.fromDb("sak_01HGD8E4RY7KSZ1YVVB1NK1XGH"),
                gjeldendeVedtakId = "ved_01HGD8E4RYT11M0P0AX99F05X8",
                ident = "12345678901",
                fom = LocalDate.of(2024, 1, 1),
                tom = LocalDate.of(2024, 3, 1),
                antallBarn = 0,
                brukerNavkontor = "0219",
                vedtakstidspunkt = LocalDateTime.of(2024, 1, 24, 14, 35, 47),
                saksbehandler = "saksbehandler",
                beslutter = "beslutter",
                utbetalinger = emptyList(),
                forrigeVedtak = null,
            )

            capturedVedtak.captured shouldBe expectedVedtak
        }
    }

    private val rammevedtakJson = """
        {
            "sakId": "sak_01HGD8E4RY7KSZ1YVVB1NK1XGH",
            "gjeldendeVedtakId": "ved_01HGD8E4RYT11M0P0AX99F05X8",
            "ident": "12345678901",
            "fom": "2024-01-01",
            "tom": "2024-03-01",
            "antallBarn": 0,
            "brukerNavkontor": "0219",
            "vedtaktidspunkt": "2024-01-24T14:35:47",
            "saksbehandler": "saksbehandler",
            "beslutter": "beslutter"
        }
    """.trimIndent()
}
