package no.nav.tiltakspenger.utbetaling.repository

import java.util.UUID

interface RammevedtakRepo {
    fun lagre()

    fun hent(id: UUID)
}
