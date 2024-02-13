package no.nav.tiltakspenger.utbetaling.service

import no.nav.dagpenger.kontrakter.felles.BrukersNavKontor
import no.nav.dagpenger.kontrakter.felles.GeneriskIdSomUUID
import no.nav.dagpenger.kontrakter.felles.Personident
import no.nav.dagpenger.kontrakter.felles.StønadTypeTiltakspenger
import no.nav.dagpenger.kontrakter.iverksett.ForrigeIverksettingDto
import no.nav.dagpenger.kontrakter.iverksett.IverksettDto
import no.nav.dagpenger.kontrakter.iverksett.StønadsdataTiltakspengerDto
import no.nav.dagpenger.kontrakter.iverksett.UtbetalingDto
import no.nav.dagpenger.kontrakter.iverksett.VedtaksdetaljerDto
import no.nav.tiltakspenger.utbetaling.client.iverksett.IverksettKlient
import no.nav.tiltakspenger.utbetaling.client.iverksett.IverksettKlient.Response
import no.nav.tiltakspenger.utbetaling.domene.TiltakType
import no.nav.tiltakspenger.utbetaling.domene.Utbetaling
import no.nav.tiltakspenger.utbetaling.domene.UtbetalingDag
import no.nav.tiltakspenger.utbetaling.domene.UtbetalingDagStatus
import no.nav.tiltakspenger.utbetaling.domene.Vedtak
import no.nav.tiltakspenger.utbetaling.repository.VedtakRepo
import java.time.LocalDate

const val SATS = 285
const val REDUSERT_SATS = 214
const val BARNETILLEGG_SATS = 55
const val REDUSERT_BARNETILLEGG_SATS = 214

class UtbetalingServiceImpl(
    private val vedtakRepo: VedtakRepo,
    private val iverksettKlient: IverksettKlient,
) : UtbetalingService {

    override suspend fun mottaRammevedtakOgSendTilIverksett(vedtak: Vedtak): Response =
        iverksettKlient.iverksett(mapIverksettDTO(vedtak)).also {
            if (it.statusCode.value == 202) vedtakRepo.lagre(vedtak)
        }

    override suspend fun mottaUtbetalingOgSendTilIverksett(utbetaling: Utbetaling): Response {
        val forrigeVedtak = vedtakRepo.hentForrigeUtbetalingVedtak(utbetaling.sakId)
        checkNotNull(forrigeVedtak) { "Fant ikke forrige utbetalingvedtak" }

        return iverksettKlient.iverksett(mapIverksettDTOmedUtbetalinger(forrigeVedtak, utbetaling)).also {
            if (it.statusCode.value == 202) vedtakRepo.lagre(forrigeVedtak)
        }
    }
}

fun mapIverksettDTOmedUtbetalinger(vedtak: Vedtak, utbetaling: Utbetaling) =
    IverksettDto(
        sakId = GeneriskIdSomUUID(vedtak.sakId.uuid()),
        behandlingId = GeneriskIdSomUUID(vedtak.id.uuid()),
        personident = Personident(
            verdi = vedtak.ident,
        ),
        vedtak = VedtaksdetaljerDto(
            vedtakstidspunkt = vedtak.vedtakstidspunkt, // tidspunkt fra meldekortbehanling
            saksbehandlerId = utbetaling.saksbehandler,
            beslutterId = utbetaling.saksbehandler,
            brukersNavKontor = BrukersNavKontor(
                enhet = vedtak.brukerNavkontor,
                gjelderFom = LocalDate.of(2024, 1, 1), // finne ut hva vi setter denne til
            ),
            utbetalinger = utbetaling.utbetalingDager.sortedBy { it.dato }.map { dag ->
                UtbetalingDto(
                    beløpPerDag = dag.mapSats(),
                    fraOgMedDato = dag.dato,
                    tilOgMedDato = dag.dato,
                    stønadsdata = StønadsdataTiltakspengerDto(
                        stønadstype = dag.mapStønadstype(),
                        barnetillegg = false,
                    ),
                )
            }.lagBarnetillegg(vedtak.antallBarn).fold(emptyList()) { acc, utbetalingDto -> acc.slåSammen(utbetalingDto) },
        ),
        forrigeIverksetting = ForrigeIverksettingDto(
            behandlingId = GeneriskIdSomUUID(vedtak.id.uuid()),
        ),
    )

private fun mapIverksettDTO(vedtak: Vedtak) =
    IverksettDto(
        sakId = GeneriskIdSomUUID(vedtak.sakId.uuid()),
        behandlingId = GeneriskIdSomUUID(vedtak.id.uuid()),
        personident = Personident(
            verdi = vedtak.ident,
        ),
        vedtak = VedtaksdetaljerDto(
            vedtakstidspunkt = vedtak.vedtakstidspunkt,
            saksbehandlerId = vedtak.saksbehandler,
            beslutterId = vedtak.beslutter,
            brukersNavKontor = BrukersNavKontor(
                enhet = vedtak.brukerNavkontor,
                gjelderFom = LocalDate.now(), // hva skal vi sette denne til? Får vi denne fra NORG?
            ),
            utbetalinger = emptyList(), //  vedtak.utbetalinger,
        ),
        forrigeIverksetting = ForrigeIverksettingDto(
            behandlingId = GeneriskIdSomUUID(vedtak.id.uuid()),
        ),
    )

private fun List<UtbetalingDto>.lagBarnetillegg(antallBarn: Int): List<UtbetalingDto> =
    if (antallBarn == 0) {
        this
    } else {
        this + this.map { it.copy(beløpPerDag = BARNETILLEGG_SATS * antallBarn) }
    }

private fun List<UtbetalingDto>.slåSammen(neste: UtbetalingDto): List<UtbetalingDto> {
    if (this.isEmpty()) return listOf(neste)
    val forrige = this.last()
    if (forrige.beløpPerDag == neste.beløpPerDag && forrige.stønadsdata.stønadstype == neste.stønadsdata.stønadstype) {
        return this.dropLast(1) + forrige.copy(
            tilOgMedDato = neste.tilOgMedDato,
        )
    } else {
        return this + neste
    }
}

private fun UtbetalingDag.mapSats(): Int = when (this.status) {
    UtbetalingDagStatus.FullUtbetaling -> SATS
    UtbetalingDagStatus.DelvisUtbetaling -> REDUSERT_SATS
    UtbetalingDagStatus.IngenUtbetaling -> 0
}

private fun UtbetalingDag.mapStønadstype(): StønadTypeTiltakspenger = when (this.tiltaktype) {
    TiltakType.GRUPPEAMO -> StønadTypeTiltakspenger.GRUPPE_AMO
    TiltakType.ENKELTAMO -> StønadTypeTiltakspenger.ENKELTPLASS_AMO
}
