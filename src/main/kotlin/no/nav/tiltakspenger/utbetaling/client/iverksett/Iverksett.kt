package no.nav.tiltakspenger.utbetaling.client.iverksett

import no.nav.dagpenger.kontrakter.iverksett.IverksettDto
import no.nav.tiltakspenger.utbetaling.client.iverksett.IverksettKlient.Response

interface Iverksett {
    suspend fun iverksett(iverksettDto: IverksettDto): Response
}
