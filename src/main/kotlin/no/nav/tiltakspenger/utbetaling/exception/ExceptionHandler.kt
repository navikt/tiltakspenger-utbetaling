package no.nav.tiltakspenger.utbetaling.exception

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import no.nav.tiltakspenger.utbetaling.exception.types.ParameterException
import no.nav.tiltakspenger.utbetaling.exception.types.UnauthorizedException

object ExceptionHandler {

    suspend fun handle(
        call: ApplicationCall,
        cause: Throwable,
    ) {
        when (cause) {
            is UnauthorizedException -> {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ExceptionResponse(cause.message ?: cause.toString(), HttpStatusCode.Unauthorized.value),
                )
            }

            is EntityNotFoundException -> {
                call.respond(
                    HttpStatusCode.NotFound,
                    ExceptionResponse(cause.message ?: cause.toString(), HttpStatusCode.NotFound.value),
                )
            }

            is ParameterException -> {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ExceptionResponse(cause.message ?: cause.toString(), HttpStatusCode.BadRequest.value),
                )
            }

            is BusinessException -> {
                call.respond(
                    HttpStatusCode.PreconditionFailed,
                    ExceptionResponse(cause.message ?: cause.toString(), cause.messageCode.value),
                )
            }
        }
    }
}
