package no.nav.tiltakspenger.utbetaling.client.iverksett

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import kotlinx.coroutines.test.runTest
import no.nav.tiltakspenger.utbetaling.domene.BehandlingId
import no.nav.tiltakspenger.utbetaling.domene.SakId
import no.nav.utsjekk.kontrakter.felles.Personident
import no.nav.utsjekk.kontrakter.iverksett.IverksettDto
import no.nav.utsjekk.kontrakter.iverksett.VedtaksdetaljerDto
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.time.LocalDateTime

internal class IverksettKlientTest {

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @Test
    fun `happy case`() = runTest {
        val iverksettKlient = IverksettKlient(
            getToken = { "token" },
            engine = MockEngine {
                respond(content = "")
            },
        )

        assertDoesNotThrow {
            iverksettKlient.iverksett(okIverksettDto)
        }
    }

//    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
//    @Test
//    fun `håndterer feil`() = runTest {
//        val iverksettKlient = IverksettKlient(
//            getToken = { "token" },
//            engine = MockEngine {
//                respond(status = HttpStatusCode.Forbidden, content = "")
//            },
//        )
//
//        iverksettKlient.iverksett(okIverksettDto) shouldBe IverksettKlient.Response(
//            statusCode = HttpStatusCode.Forbidden,
//            melding = "Iverksetting er mottat for behandlingId ${okIverksettDto.behandlingId} ${HttpStatusCode.BadRequest}"
//
//        )
//    }

    private val okIverksettDto = IverksettDto(
        sakId = SakId("sakId").verdi,
        behandlingId = BehandlingId.random().uuidPart(),
        personident = Personident(
            verdi = "21848397986",
        ),
        vedtak = VedtaksdetaljerDto(
            vedtakstidspunkt = LocalDateTime.of(2024, 1, 1, 1, 1),
            saksbehandlerId = "saksbehandler",
            beslutterId = "beslutter",
            brukersNavKontor = null,
            utbetalinger = emptyList(),
        ),
        forrigeIverksetting = null,
    )
}
