package no.nav.tiltakspenger.utbetaling.client.iverksett

import no.nav.tiltakspenger.utbetaling.client.iverksett.IverksettKlient.Response
import no.nav.utsjekk.kontrakter.iverksett.IverksettDto

interface Iverksett {
    suspend fun iverksett(iverksettDto: IverksettDto): Response
}
