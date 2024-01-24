package no.nav.tiltakspenger.utbetaling.repository

import no.nav.tiltakspenger.utbetaling.domene.Rammevedtak
import no.nav.tiltakspenger.utbetaling.domene.RammevedtakId

interface RammevedtakRepo {
    fun lagre(rammevedtak: Rammevedtak)

    fun hent(id: RammevedtakId): Rammevedtak?
}
