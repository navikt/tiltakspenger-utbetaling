package no.nav.tiltakspenger.utbetaling

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.routing.routing
import mu.KotlinLogging
import no.nav.tiltakspenger.utbetaling.exception.ExceptionHandler
import no.nav.tiltakspenger.utbetaling.routes.healthRoutes
import no.nav.tiltakspenger.utbetaling.routes.utbetaling.UtbetalingServiceImpl
import no.nav.tiltakspenger.utbetaling.routes.utbetaling.utbetaling

fun main(args: Array<String>) {
    System.setProperty("logback.configurationFile", "egenLogback.xml")

    val log = KotlinLogging.logger {}
    val securelog = KotlinLogging.logger("tjenestekall")

    Thread.setDefaultUncaughtExceptionHandler { _, e ->
        log.error { "Uncaught exception logget i securelog" }
        log.error(e) { e.message }
    }

    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val utbetalingService = UtbetalingServiceImpl(environment.config)

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
