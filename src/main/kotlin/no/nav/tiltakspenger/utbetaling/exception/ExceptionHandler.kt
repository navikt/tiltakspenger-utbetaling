package no.nav.tiltakspenger.utbetaling.exception

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import no.nav.tiltakspenger.utbetaling.exception.egendefinerteFeil.UtbetalingIkkeFunnetException

object ExceptionHandler {

    suspend fun handle(
        call: ApplicationCall,
        cause: Throwable,
    ) {
        when (cause) {
            is UtbetalingIkkeFunnetException -> {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ExceptionResponse(cause.message ?: cause.toString()),
                )
            }
        }
    }
}
