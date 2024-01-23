package no.nav.tiltakspenger.utbetaling.client.iverksett

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.request.accept
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.utils.EmptyContent.contentType
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.config.ApplicationConfig
import mu.KotlinLogging
import no.nav.dagpenger.kontrakter.iverksett.IverksettDto
import no.nav.tiltakspenger.utbetaling.Configuration
import no.nav.tiltakspenger.utbetaling.auth.defaultHttpClient
import no.nav.tiltakspenger.utbetaling.auth.defaultObjectMapper
import org.slf4j.LoggerFactory

private val log = KotlinLogging.logger {}

class IverksettKlient(
    private val config: Configuration.ClientConfig = Configuration.iverksettClientConfig(),
    private val objectMapper: ObjectMapper = defaultObjectMapper(),
    private val getToken: suspend () -> String,
    engine: HttpClientEngine? = null,
    private val httpClient: HttpClient = defaultHttpClient(
        objectMapper = objectMapper,
        engine = engine,
    ),
) : Iverksett {
    companion object {
        const val navCallIdHeader = "tiltakspenger-utbetaling"
    }

    override suspend fun iverksett(iverksettDto: IverksettDto): String {
        try {
            val httpResponse =
                httpClient.post("${config.baseUrl}/api/iverksetting") {
                    header(navCallIdHeader, navCallIdHeader)
                    bearerAuth(getToken())
                    accept(ContentType.Application.Json)
                    contentType(ContentType.Application.Json)
                    setBody(iverksettDto)
                }

            when (httpResponse.status) {
                HttpStatusCode.Accepted -> {
                    log.info("Iverksetting er mottat for behandlingId ${iverksettDto.behandlingId}")
                    return "Iverksetting er mottat for behandlingId ${iverksettDto.behandlingId} ${httpResponse.status}"
                }

                HttpStatusCode.BadRequest -> {
                    log.info("Ugyldig format på iverksetting for behandlingId ${iverksettDto.behandlingId}")
                    return "Ugyldig format på iverksetting for behandlingId ${iverksettDto.behandlingId} ${httpResponse.status}"
                }

                HttpStatusCode.Forbidden -> {
                    log.info("Ikke autorisert til å starte iverksetting for behandlingId ${iverksettDto.behandlingId}")
                    return "Ikke autorisert til å starte iverksetting for behandlingId ${iverksettDto.behandlingId} ${httpResponse.status}"
                }

                HttpStatusCode.Conflict -> {
                    log.info("Iverksetting er i konflikt med tidligere iverksetting for behandlingId ${iverksettDto.behandlingId}")
                    return "Iverksetting er i konflikt med tidligere iverksetting for behandlingId ${iverksettDto.behandlingId} ${httpResponse.status}"
                }

                else -> {
                    log.error("Kallet til tiltakspenger-iverksett feilet ${httpResponse.status} ${httpResponse.status.description}")
                    throw RuntimeException("Feil i kallet til iverksett")
                }
            }
        } catch (throwable: Throwable) {
            log.warn("Uhåndtert feil mot tiltakspenger-iverksett. Mottat feilmelding ${throwable.message}")
            return "Uhåndtert feil mot tiltakspenger-iverksett. Mottat feilmelding ${throwable.message}"
        }
    }
}
