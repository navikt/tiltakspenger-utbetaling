package no.nav.tiltakspenger.utbetaling

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.routing.routing
import mu.KotlinLogging
import no.nav.tiltakspenger.utbetaling.Configuration.httpPort
import no.nav.tiltakspenger.utbetaling.auth.AzureTokenProvider
import no.nav.tiltakspenger.utbetaling.client.iverksett.IverksettKlient
import no.nav.tiltakspenger.utbetaling.db.flywayMigrate
import no.nav.tiltakspenger.utbetaling.exception.ExceptionHandler
import no.nav.tiltakspenger.utbetaling.repository.RammevedtakRepoImpl
import no.nav.tiltakspenger.utbetaling.routes.healthRoutes
import no.nav.tiltakspenger.utbetaling.routes.utbetaling.utbetaling
import no.nav.tiltakspenger.utbetaling.service.UtbetalingServiceImpl

fun main() {
    System.setProperty("logback.configurationFile", Configuration.logbackConfigurationFile())

    val log = KotlinLogging.logger {}
    val securelog = KotlinLogging.logger("tjenestekall")

    Thread.setDefaultUncaughtExceptionHandler { _, e ->
        log.error { "Uncaught exception logget i securelog" }
        securelog.error(e) { e.message }
    }

    embeddedServer(Netty, port = httpPort(), module = Application::module).start(wait = true)
}

fun Application.module() {
    flywayMigrate()
    val tokenProvider = AzureTokenProvider(config = Configuration.oauthConfigIverksett())
    val iverksettKlient = IverksettKlient(getToken = tokenProvider::getToken)
    val vedtakRepo = RammevedtakRepoImpl()
    val utbetalingService = UtbetalingServiceImpl(iverksettKlient)

    jacksonSerialization()
    routing {
        healthRoutes()
        utbetaling(utbetalingService)
    }
}

fun Application.configureExceptions() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            ExceptionHandler.handle(call, cause)
        }
    }
}

fun Application.jacksonSerialization() {
    install(ContentNegotiation) {
        jackson {
            configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            registerModule(JavaTimeModule())
            registerModule(KotlinModule.Builder().build())
        }
    }
}
