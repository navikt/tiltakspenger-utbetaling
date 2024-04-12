package no.nav.tiltakspenger.utbetaling.client.iverksett

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.request.accept
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import mu.KotlinLogging
import no.nav.tiltakspenger.utbetaling.Configuration
import no.nav.tiltakspenger.utbetaling.auth.defaultHttpClient
import no.nav.tiltakspenger.utbetaling.auth.defaultObjectMapper
import no.nav.utsjekk.kontrakter.iverksett.IverksettDto

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

    data class Response(
        val statusCode: HttpStatusCode,
        val melding: String,
    )
    override suspend fun iverksett(iverksettDto: IverksettDto): Response {
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
                    return Response(
                        statusCode = httpResponse.status,
                        melding = "Iverksetting er mottat for behandlingId ${iverksettDto.behandlingId} ${httpResponse.status}",
                    )
                }

                HttpStatusCode.BadRequest -> {
                    log.info("Ugyldig format på iverksetting for behandlingId ${iverksettDto.behandlingId}")
                    return Response(
                        statusCode = httpResponse.status,
                        melding = "Ugyldig format på iverksetting for behandlingId ${iverksettDto.behandlingId} ${httpResponse.status}",
                    )
                }

                HttpStatusCode.Forbidden -> {
                    log.info("Ikke autorisert til å starte iverksetting for behandlingId ${iverksettDto.behandlingId}")
                    return Response(
                        statusCode = httpResponse.status,
                        melding = "Ikke autorisert til å starte iverksetting for behandlingId ${iverksettDto.behandlingId} ${httpResponse.status}",
                    )
                }

                HttpStatusCode.Conflict -> {
                    log.info("Iverksetting er i konflikt med tidligere iverksetting for behandlingId ${iverksettDto.behandlingId}")
                    return Response(
                        statusCode = httpResponse.status,
                        melding = "Iverksetting er i konflikt med tidligere iverksetting for behandlingId ${iverksettDto.behandlingId} ${httpResponse.status}",
                    )
                }

                else -> {
                    log.error("Kallet til tiltakspenger-iverksett feilet ${httpResponse.status} ${httpResponse.status.description}")
                    return Response(
                        statusCode = HttpStatusCode.BadRequest,
                        melding = "Kallet til tiltakspenger-iverksett feilet ${httpResponse.status} ${httpResponse.status.description}",
                    )
                }
            }
        } catch (throwable: Throwable) {
            log.warn("Uhåndtert feil mot tiltakspenger-iverksett. Mottat feilmelding ${throwable.message}")
            return Response(
                statusCode = HttpStatusCode.BadRequest,
                melding = "Uhåndtert feil mot tiltakspenger-iverksett. Mottat feilmelding ${throwable.message}",
            )
        }
    }
}
