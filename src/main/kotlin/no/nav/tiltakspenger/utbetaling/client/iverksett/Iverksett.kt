package no.nav.tiltakspenger.utbetaling.client.iverksett

import no.nav.tiltakspenger.utbetaling.client.iverksett.IverksettKlient.Response
import no.nav.utsjekk.kontrakter.iverksett.IverksettV2Dto

interface Iverksett {
    suspend fun iverksett(iverksettDto: IverksettV2Dto): Response
}
