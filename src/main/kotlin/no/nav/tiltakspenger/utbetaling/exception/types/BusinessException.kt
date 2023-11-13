package no.nav.tiltakspenger.utbetaling.exception

open class BusinessException(val messageCode: MessageCode, message: String?) : RuntimeException(message)
