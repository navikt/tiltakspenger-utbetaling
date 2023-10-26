package no.nav.tiltakspenger.utbetaling.routes.iverksett

import io.ktor.server.config.ApplicationConfig
import org.slf4j.LoggerFactory

class IverksettKlient(private val config: ApplicationConfig) {
    private val iverksettEndpoint = config.property("endpoints.iverksett").getString()
    private val log = LoggerFactory.getLogger(this::class.java)

    fun iverksett() {
        log.info("Iverksetter utbetaling på endepunktet $iverksettEndpoint")
    }
}
