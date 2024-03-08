package no.nav.tiltakspenger.utbetaling.client.iverksett

import no.nav.tiltakspenger.utbetaling.domene.Vedtak
import no.nav.tiltakspenger.utbetaling.service.ports.IverksettGateway
import no.nav.tiltakspenger.utbetaling.service.ports.IverksettRespons

class IverksettGatewayImpl(
    private val iverksettKlient: IverksettKlient,
) : IverksettGateway {
    override suspend fun iverksett(vedtak: Vedtak): IverksettRespons {
        return IverksettMapper.mapResponse(iverksettKlient.iverksett(IverksettMapper.mapIverksettDTO(vedtak)))
    }
}
