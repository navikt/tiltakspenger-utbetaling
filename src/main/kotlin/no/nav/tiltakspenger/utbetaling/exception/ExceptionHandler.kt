package no.nav.tiltakspenger.utbetaling.exception

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import no.nav.tiltakspenger.utbetaling.exception.egendefinerteFeil.FeilFormatException
import no.nav.tiltakspenger.utbetaling.exception.egendefinerteFeil.KonfliktException
import no.nav.tiltakspenger.utbetaling.exception.egendefinerteFeil.TilgangException
import no.nav.tiltakspenger.utbetaling.exception.egendefinerteFeil.UkjentFeilException
import no.nav.tiltakspenger.utbetaling.exception.egendefinerteFeil.UtbetalingIkkeFunnetException

object ExceptionHandler {

    suspend fun handle(
        call: ApplicationCall,
        cause: Throwable,
    ) {
        when (cause) {
            is UtbetalingIkkeFunnetException -> {
                call.respond(
                    HttpStatusCode.NotFound,
                    ExceptionResponse(cause.message ?: cause.toString()),
                )
            }
            is FeilFormatException -> {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ExceptionResponse(cause.message ?: cause.toString()),
                )
            }
            is TilgangException -> {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ExceptionResponse(cause.message ?: cause.toString()),
                )
            }
            is KonfliktException -> {
                call.respond(
                    HttpStatusCode.Conflict,
                    ExceptionResponse(cause.message ?: cause.toString()),
                )
            }
            is UkjentFeilException -> {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ExceptionResponse(cause.message ?: cause.toString()),
                )
            }
            else -> {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    "En uhåndtert feil har oppstått",
                )
            }
        }
    }
}
