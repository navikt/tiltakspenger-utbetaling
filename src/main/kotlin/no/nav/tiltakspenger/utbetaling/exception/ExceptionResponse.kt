package no.nav.tiltakspenger.utbetaling.exception

import kotlinx.serialization.Serializable
    @Serializable
    class ExceptionResponse(
        val message: String,
        val code: Int
    )
