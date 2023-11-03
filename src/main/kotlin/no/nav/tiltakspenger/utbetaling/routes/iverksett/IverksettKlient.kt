package no.nav.tiltakspenger.utbetaling.routes.iverksett

import io.ktor.client.HttpClient
import io.ktor.client.request.accept
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.server.config.ApplicationConfig
import no.nav.tiltakspenger.utbetaling.routes.utbetaling.UtbetalingDTOUt
import org.slf4j.LoggerFactory

class IverksettKlient(
    private val config: ApplicationConfig,
    private val client: HttpClient = httpClientWithRetry(timeout = 30L),
    private val iverksettCredentialsClient: IverksettCredentialsClient = IverksettCredentialsClient(config),
) : Iverksett {
    private val iverksettEndpoint = config.property("endpoints.iverksett").getString()
    private val log = LoggerFactory.getLogger(this::class.java)

    override suspend fun iverksett(utbetalingDTOUt: UtbetalingDTOUt): String {
        val token = iverksettCredentialsClient.getToken()
        val res = client.post("$iverksettEndpoint/api/iverksetting") {
            accept(ContentType.Application.Json)
            setBody(objectMapper.writeValueAsString(utbetalingDTOUt))
            bearerAuth(token)
        }
        log.info("status fra dp-iverkett er ${res.status}")

        return "IKKE NOE HER ER FERDIG ENDA"
    }
}
