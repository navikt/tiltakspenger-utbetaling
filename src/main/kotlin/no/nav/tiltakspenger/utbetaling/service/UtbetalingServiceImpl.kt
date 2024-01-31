package no.nav.tiltakspenger.utbetaling.service

import no.nav.dagpenger.kontrakter.felles.Personident
import no.nav.dagpenger.kontrakter.iverksett.IverksettDto
import no.nav.dagpenger.kontrakter.iverksett.VedtakType
import no.nav.dagpenger.kontrakter.iverksett.VedtaksdetaljerDto
import no.nav.dagpenger.kontrakter.iverksett.VedtaksperiodeDto
import no.nav.dagpenger.kontrakter.iverksett.VedtaksperiodeType
import no.nav.dagpenger.kontrakter.iverksett.Vedtaksresultat
import no.nav.tiltakspenger.utbetaling.client.iverksett.IverksettKlient
import no.nav.tiltakspenger.utbetaling.domene.IverksettingResultat
import no.nav.tiltakspenger.utbetaling.domene.Rammevedtak
import no.nav.tiltakspenger.utbetaling.repository.RammevedtakRepo

class UtbetalingServiceImpl(
    private val rammevedtakRepo: RammevedtakRepo,
    private val iverksettKlient: IverksettKlient,
) : UtbetalingService {

    override suspend fun mottaRammevedtakOgSendTilIverksett(rammevedtak: Rammevedtak) {
        rammevedtakRepo.lagre(rammevedtak)
        iverksettKlient.iverksett(mapIverksettDTO(rammevedtak))
    }
}

private fun mapIverksettDTO(rammevedtak: Rammevedtak) =
    IverksettDto(
        sakId = rammevedtak.sakId.uuid(),
        saksreferanse = rammevedtak.saksnummer,
        behandlingId = rammevedtak.behandlingId.uuid(),
        personident = Personident(
            verdi = rammevedtak.personIdent,
        ),
        vedtak = VedtaksdetaljerDto(
            vedtakstype = VedtakType.RAMMEVEDTAK,
            vedtakstidspunkt = rammevedtak.vedtakstidspunkt,
            resultat = when (rammevedtak.iverksettingResultat) {
                IverksettingResultat.INNVILGET -> Vedtaksresultat.INNVILGET
                IverksettingResultat.AVSLÅTT -> Vedtaksresultat.AVSLÅTT
                IverksettingResultat.OPPHØRT -> Vedtaksresultat.OPPHØRT
            },
            saksbehandlerId = rammevedtak.saksbehandler,
            beslutterId = rammevedtak.beslutter,
            brukersNavKontor = null,
            utbetalinger = emptyList(),
            vedtaksperioder = listOf(
                VedtaksperiodeDto(
                    fraOgMedDato = rammevedtak.fom,
                    tilOgMedDato = rammevedtak.tom,
                    periodeType = VedtaksperiodeType.HOVEDPERIODE,
                ),
            ),

        ),
        forrigeIverksetting = null,
    )
