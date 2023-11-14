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
import no.nav.tiltakspenger.utbetaling.exception.egendefinerteFeil.FeilFormatException
import no.nav.tiltakspenger.utbetaling.exception.egendefinerteFeil.KonfliktException
import no.nav.tiltakspenger.utbetaling.exception.egendefinerteFeil.TilgangException
import no.nav.tiltakspenger.utbetaling.exception.egendefinerteFeil.UkjentFeilException
import org.slf4j.LoggerFactory

class IverksettClient(
    private val config: ApplicationConfig,
    private val client: HttpClient = httpClientWithRetry(timeout = 30L),
    private val iverksettCredentialsClient: IverksettCredentialsClient = IverksettCredentialsClient(config),
) : Iverksett {
    private val iverksettEndpoint = config.property("endpoints.iverksett").getString()
    private val log = LoggerFactory.getLogger(this::class.java)

    override suspend fun iverksett(iverksettDto: IverksettDto): String {
        val res = try {
            val token = iverksettCredentialsClient.getToken()
            client.post("$iverksettEndpoint/api/iverksetting") {
                accept(ContentType.Application.Json)
                contentType(ContentType.Application.Json)
                setBody(objectMapper.writeValueAsString(iverksettDto))
                bearerAuth(token)
            }
        } catch (throwable: Throwable) {
            log.warn("Uhåndtert feil mot tiltakspenger-iverksett. Mottat feilmelding ${throwable.message}")
            throw UkjentFeilException("Uhåndtert feil mot tiltakspenger-iverksett. Mottat feilmelding ${throwable.message}")
        }

        when (res.status) {
            HttpStatusCode.Accepted -> {
                log.info("Iverksetting er mottat for behandlingId ${iverksettDto.behandlingId}")
                return "Iverksetting er mottat for behandlingId ${iverksettDto.behandlingId} ${res.status}"
            }
            HttpStatusCode.BadRequest -> {
                log.info("Ugyldig format på iverksetting for behandlingId ${iverksettDto.behandlingId}")
                throw FeilFormatException("Ugyldig format på iverksetting for behandlingId ${iverksettDto.behandlingId}")
            }
            HttpStatusCode.Forbidden -> {
                log.info("Ikke autorisert til å starte iverksetting for behandlingId ${iverksettDto.behandlingId}")
                throw TilgangException("Ikke autorisert til å starte iverksetting for behandlingId ${iverksettDto.behandlingId}")
            }
            HttpStatusCode.Conflict -> {
                log.info("Iverksetting er i konflikt med tidligere iverksetting for behandlingId ${iverksettDto.behandlingId}")
                throw KonfliktException("Iverksetting er i konflikt med tidligere iverksetting for behandlingId ${iverksettDto.behandlingId}")
            }
            else -> {
                log.error("Kallet til tiltakspenger-iverksett feilet ${res.status} ${res.status.description}")
                throw UkjentFeilException("Feil i kallet til iverksett. Iverksett svaret med status=${res.status} ")
            }
        }
    }
}
