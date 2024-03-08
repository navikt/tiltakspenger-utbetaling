package no.nav.tiltakspenger.utbetaling.service.ports

import no.nav.tiltakspenger.utbetaling.domene.Vedtak

interface IverksettGateway {
    suspend fun iverksett(vedtak: Vedtak): IverksettRespons
}

data class IverksettRespons(
    val ok: Boolean,
    val opprinneligStatusCodeValue: Int,
    val opprinneligStatusCodeMessage: String,
)
