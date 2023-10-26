package no.nav.tiltakspenger.utbetaling.routes.iverksett

import com.fasterxml.jackson.databind.ObjectMapper
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
    private val client: HttpClient = HttpClient(),
    private val iverksettCredentialsClient: IverksettCredentialsClient = IverksettCredentialsClient(config),
) : Iverksett {
    private val iverksettEndpoint = config.property("endpoints.iverksett").getString()
    private val log = LoggerFactory.getLogger(this::class.java)
    private val objectMapper = ObjectMapper()

    override suspend fun iverksett(utbetalingDTOUt: UtbetalingDTOUt): String {
        val token = iverksettCredentialsClient.getToken()
        val res = client.post(iverksettEndpoint) {
            accept(ContentType.Application.Json)
            setBody(objectMapper.writeValueAsString(utbetalingDTOUt))
            bearerAuth(token)
        }
        return "IKKE NOE HER ER FERDIG ENDA"
    }
}
