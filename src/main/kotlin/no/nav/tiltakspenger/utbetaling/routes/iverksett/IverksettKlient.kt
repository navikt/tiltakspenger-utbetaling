package no.nav.tiltakspenger.utbetaling.routes.iverksett

import io.ktor.client.HttpClient
import io.ktor.client.request.accept
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.config.ApplicationConfig
import no.nav.dagpenger.kontrakter.iverksett.IverksettDto
import org.slf4j.LoggerFactory

class IverksettKlient(
    private val config: ApplicationConfig,
    private val client: HttpClient = httpClientWithRetry(timeout = 30L),
    private val iverksettCredentialsClient: IverksettCredentialsClient = IverksettCredentialsClient(config),
) : Iverksett {
    private val iverksettEndpoint = config.property("endpoints.iverksett").getString()
    private val log = LoggerFactory.getLogger(this::class.java)

    override suspend fun iverksett(iverksettDto: IverksettDto): String {
        try {
            val token = iverksettCredentialsClient.getToken()
            val res = client.post("$iverksettEndpoint/api/iverksetting") {
                accept(ContentType.Application.Json)
                contentType(ContentType.Application.Json)
                setBody(objectMapper.writeValueAsString(iverksettDto))
                bearerAuth(token)
            }
            log.info("status fra dp-iverkett er ${res.status}")

            when (res.status) {
                HttpStatusCode.Accepted -> {
                    log.info("Iverksetting er mottat for behandlingId ${iverksettDto.behandlingId}")
                    return "Iverksetting er mottat for behandlingId ${iverksettDto.behandlingId} ${res.status}"
                }
                HttpStatusCode.BadRequest -> {
                    log.info("Ugyldig format på iverksetting for behandlingId ${iverksettDto.behandlingId}")
                    return "Ugyldig format på iverksetting for behandlingId ${iverksettDto.behandlingId} ${res.status}"
                }
                HttpStatusCode.Forbidden -> {
                    log.info("Ikke autorisert til å starte iverksetting for behandlingId ${iverksettDto.behandlingId}")
                    return "Ikke autorisert til å starte iverksetting for behandlingId ${iverksettDto.behandlingId} ${res.status}"
                }
                HttpStatusCode.Conflict -> {
                    log.info("Iverksetting er i konflikt med tidligere iverksetting for behandlingId ${iverksettDto.behandlingId}")
                    return "Iverksetting er i konflikt med tidligere iverksetting for behandlingId ${iverksettDto.behandlingId} ${res.status}"
                }

                else -> {
                    log.error("Kallet til tiltakspenger-iverksett feilet ${res.status} ${res.status.description}")
                    throw RuntimeException("Feil i kallet til iverksett")
                }
            }
        } catch (throwable: Throwable) {
            log.warn("Uhåndtert feil mot tiltakspenger-iverksett. Mottat feilmelding ${throwable.message}")
            return "Uhåndtert feil mot tiltakspenger-iverksett. Mottat feilmelding ${throwable.message}"
        }
    }
}
