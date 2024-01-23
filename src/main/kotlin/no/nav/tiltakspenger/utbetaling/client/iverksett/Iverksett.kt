package no.nav.tiltakspenger.utbetaling.client.iverksett

import no.nav.dagpenger.kontrakter.iverksett.IverksettDto

interface Iverksett {
    suspend fun iverksett(iverksettDto: IverksettDto): String
}
