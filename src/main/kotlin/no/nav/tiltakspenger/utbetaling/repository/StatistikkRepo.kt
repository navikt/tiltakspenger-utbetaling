package no.nav.tiltakspenger.utbetaling.repository

import no.nav.tiltakspenger.utbetaling.domene.Statistikk

interface StatistikkRepo {
    fun lagre(statistikk: Statistikk)
}
