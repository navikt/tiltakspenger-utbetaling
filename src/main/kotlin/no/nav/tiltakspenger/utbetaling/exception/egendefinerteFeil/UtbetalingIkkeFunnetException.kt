package no.nav.tiltakspenger.utbetaling.exception.egendefinerteFeil

class UtbetalingIkkeFunnetException(behandlingId: String) :
    RuntimeException("Fant ikke behandlingen $behandlingId")
