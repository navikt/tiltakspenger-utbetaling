package no.nav.tiltakspenger.utbetaling.service

import no.nav.dagpenger.kontrakter.felles.GeneriskIdSomUUID
import no.nav.dagpenger.kontrakter.felles.Personident
import no.nav.dagpenger.kontrakter.iverksett.IverksettDto
import no.nav.dagpenger.kontrakter.iverksett.VedtaksdetaljerDto
import no.nav.tiltakspenger.utbetaling.client.iverksett.IverksettKlient
import no.nav.tiltakspenger.utbetaling.client.iverksett.IverksettKlient.Response
import no.nav.tiltakspenger.utbetaling.domene.Rammevedtak
import no.nav.tiltakspenger.utbetaling.repository.RammevedtakRepo

class UtbetalingServiceImpl(
    private val rammevedtakRepo: RammevedtakRepo,
    private val iverksettKlient: IverksettKlient,
) : UtbetalingService {

    override suspend fun mottaRammevedtakOgSendTilIverksett(rammevedtak: Rammevedtak): Response =
        iverksettKlient.iverksett(mapIverksettDTO(rammevedtak)).also {
            if (it.statusCode.value == 202) rammevedtakRepo.lagre(rammevedtak)
        }
}

private fun mapIverksettDTO(rammevedtak: Rammevedtak) =
    IverksettDto(
        sakId = GeneriskIdSomUUID(rammevedtak.sakId.uuid()),
        behandlingId = GeneriskIdSomUUID(rammevedtak.behandlingId.uuid()),
        personident = Personident(
            verdi = rammevedtak.personIdent,
        ),
        vedtak = VedtaksdetaljerDto(
            vedtakstidspunkt = rammevedtak.vedtakstidspunkt,
            saksbehandlerId = rammevedtak.saksbehandler,
            beslutterId = rammevedtak.beslutter,
            brukersNavKontor = null,
            utbetalinger = emptyList(),
        ),
        forrigeIverksetting = null,
    )
