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
import no.nav.tiltakspenger.utbetaling.domene.nyttVedtak
import no.nav.tiltakspenger.utbetaling.repository.VedtakRepo
import java.time.LocalDate

const val SATS = 285
const val REDUSERT_SATS = 214
const val BARNETILLEGG_SATS = 55
const val REDUSERT_BARNETILLEGG_SATS = 42

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

        val vedtak = forrigeVedtak.nyttVedtak(
            saksbehandler = utbetaling.saksbehandler,
            utløsendeId = utbetaling.utløsendeMeldekortId,
            utbetalinger = utbetaling.utbetalingDager,
        )

        return iverksettKlient.iverksett(mapIverksettDTO(vedtak)).also {
            if (it.statusCode.value == 202) vedtakRepo.lagre(vedtak)
        }
    }
}

fun mapIverksettDTO(vedtak: Vedtak) =
    IverksettDto(
        sakId = GeneriskIdSomUUID(vedtak.sakId.uuid()),
        behandlingId = GeneriskIdSomUUID(vedtak.id.uuid()),
        personident = Personident(
            verdi = vedtak.ident,
        ),
        vedtak = VedtaksdetaljerDto(
            vedtakstidspunkt = vedtak.vedtakstidspunkt, // tidspunkt fra meldekortbehanling
            saksbehandlerId = vedtak.saksbehandler,
            beslutterId = vedtak.saksbehandler,
            brukersNavKontor = BrukersNavKontor(
                enhet = vedtak.brukerNavkontor,
                gjelderFom = LocalDate.of(2024, 1, 1), // finne ut hva vi setter denne til
            ),
            utbetalinger = vedtak.utbetalinger
                .sortedBy { it.dato }
                .groupBy { it.løpenr }
                .map { (_, dager) ->
                    dager
                        .lagUtbetalingDtoPrDag(vedtak.antallBarn)
                        .fold(emptyList<UtbetalingDto>()) { periodisertliste, nesteDag ->
                            periodisertliste.slåSammen(nesteDag)
                        }
                }.flatten(),
        ),
        forrigeIverksetting = vedtak.forrigeVedtak?.let {
            ForrigeIverksettingDto(
                behandlingId = GeneriskIdSomUUID(it.uuid()),
            )
        },
    )

private fun List<UtbetalingDag>.lagUtbetalingDtoPrDag(antallBarn: Int): List<UtbetalingDto> {
    val dager = this.map { dag ->
        UtbetalingDto(
            beløpPerDag = dag.mapSats(),
            fraOgMedDato = dag.dato,
            tilOgMedDato = dag.dato,
            stønadsdata = StønadsdataTiltakspengerDto(
                stønadstype = dag.mapStønadstype(),
                barnetillegg = false,
            ),
        )
    }

    return if (antallBarn > 0) {
        dager + this.map { dag ->
            UtbetalingDto(
                beløpPerDag = dag.mapBarnetilleggSats(antallBarn),
                fraOgMedDato = dag.dato,
                tilOgMedDato = dag.dato,
                stønadsdata = StønadsdataTiltakspengerDto(
                    stønadstype = dag.mapStønadstype(),
                    barnetillegg = true,
                ),
            )
        }
    } else {
        dager
    }
}

private fun List<UtbetalingDto>.slåSammen(neste: UtbetalingDto): List<UtbetalingDto> {
    if (this.isEmpty()) return listOf(neste)
    val forrige = this.last()
    return if (forrige.beløpPerDag == neste.beløpPerDag && forrige.stønadsdata.stønadstype == neste.stønadsdata.stønadstype) {
        this.dropLast(1) + forrige.copy(
            tilOgMedDato = neste.tilOgMedDato,
        )
    } else {
        this + neste
    }
}

private fun UtbetalingDag.mapSats(): Int = when (this.status) {
    UtbetalingDagStatus.FullUtbetaling -> SATS
    UtbetalingDagStatus.DelvisUtbetaling -> REDUSERT_SATS
    UtbetalingDagStatus.IngenUtbetaling -> 0
}

private fun UtbetalingDag.mapBarnetilleggSats(antallBarn: Int): Int = when (this.status) {
    UtbetalingDagStatus.FullUtbetaling -> BARNETILLEGG_SATS * antallBarn
    UtbetalingDagStatus.DelvisUtbetaling -> REDUSERT_BARNETILLEGG_SATS * antallBarn
    UtbetalingDagStatus.IngenUtbetaling -> 0
}

private fun UtbetalingDag.mapStønadstype(): StønadTypeTiltakspenger = when (this.tiltaktype) {
    TiltakType.GRUPPEAMO -> StønadTypeTiltakspenger.GRUPPE_AMO
    TiltakType.ENKELTAMO -> StønadTypeTiltakspenger.ENKELTPLASS_AMO
}
