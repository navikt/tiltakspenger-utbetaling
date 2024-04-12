package no.nav.tiltakspenger.utbetaling.service

import no.nav.tiltakspenger.utbetaling.client.iverksett.IverksettKlient
import no.nav.tiltakspenger.utbetaling.client.iverksett.IverksettKlient.Response
import no.nav.tiltakspenger.utbetaling.domene.BehandlingId
import no.nav.tiltakspenger.utbetaling.domene.Satser
import no.nav.tiltakspenger.utbetaling.domene.TiltakType
import no.nav.tiltakspenger.utbetaling.domene.Utbetaling
import no.nav.tiltakspenger.utbetaling.domene.UtbetalingDag
import no.nav.tiltakspenger.utbetaling.domene.UtbetalingDagStatus
import no.nav.tiltakspenger.utbetaling.domene.Utfallsperiode
import no.nav.tiltakspenger.utbetaling.domene.Vedtak
import no.nav.tiltakspenger.utbetaling.domene.VedtakId
import no.nav.tiltakspenger.utbetaling.domene.antallBarn
import no.nav.tiltakspenger.utbetaling.domene.nyttUtbetalingVedtak
import no.nav.tiltakspenger.utbetaling.repository.VedtakRepo
import no.nav.tiltakspenger.utbetaling.routes.utbetaling.GrunnlagDTO
import no.nav.utsjekk.kontrakter.felles.BrukersNavKontor
import no.nav.utsjekk.kontrakter.felles.Personident
import no.nav.utsjekk.kontrakter.felles.StønadTypeTiltakspenger
import no.nav.utsjekk.kontrakter.iverksett.ForrigeIverksettingDto
import no.nav.utsjekk.kontrakter.iverksett.IverksettDto
import no.nav.utsjekk.kontrakter.iverksett.StønadsdataTiltakspengerDto
import no.nav.utsjekk.kontrakter.iverksett.UtbetalingDto
import no.nav.utsjekk.kontrakter.iverksett.VedtaksdetaljerDto
import java.time.LocalDate

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

        val vedtak = forrigeVedtak.nyttUtbetalingVedtak(
            saksbehandler = utbetaling.saksbehandler,
            utløsendeId = utbetaling.utløsendeMeldekortId,
            utbetalinger = utbetaling.utbetalingDager,
        )

        return iverksettKlient.iverksett(mapIverksettDTO(vedtak)).also {
            if (it.statusCode.value == 202) vedtakRepo.lagre(vedtak)
        }
    }

    override fun hentAlleVedtak(behandlingId: BehandlingId): List<Vedtak> {
        val sakId = vedtakRepo.hentSakIdForBehandling(behandlingId)
        checkNotNull(sakId) { "Klarte ikke å finne sakId for behandling $behandlingId" }
        return vedtakRepo.hentAlleVedtakForSak(sakId)
    }

    override fun hentVedtak(vedtakId: VedtakId): Vedtak? {
        return vedtakRepo.hentVedtak(vedtakId)
    }

    override fun hentGrunnlag(grunnlagDTO: GrunnlagDTO): List<UtbetalingGrunnlagDag> {
        val vedtak = vedtakRepo.hentVedtakForBehandling(grunnlagDTO.behandlingId)
            ?: throw IllegalStateException("Fant ikke vedtak")

        return grunnlagDTO.fom.datesUntil(grunnlagDTO.tom.plusDays(1)).map {
            val satsForDag = Satser.sats(it)
            UtbetalingGrunnlagDag(
                antallBarn = vedtak.utfallsperioder.antallBarn(it),
                sats = satsForDag.sats,
                satsDelvis = satsForDag.satsDelvis,
                satsBarn = satsForDag.satsBarnetillegg,
                satsBarnDelvis = satsForDag.satsBarnetilleggDelvis,
                fom = it,
                tom = it,
            )
        }.toList()
    }
}

data class UtbetalingGrunnlagDag(
    val antallBarn: Int,
    val sats: Int,
    val satsDelvis: Int,
    val satsBarn: Int,
    val satsBarnDelvis: Int,
    val fom: LocalDate,
    val tom: LocalDate,
) {
    override fun equals(other: Any?): Boolean {
        return other != null &&
            other is UtbetalingGrunnlagDag &&
            this.antallBarn == other.antallBarn &&
            this.sats == other.sats &&
            this.satsDelvis == other.satsDelvis &&
            this.satsBarn == other.satsBarn &&
            this.satsBarnDelvis == other.satsBarnDelvis
    }

    override fun hashCode(): Int {
        var result = antallBarn
        result = 31 * result + sats
        result = 31 * result + satsDelvis
        result = 31 * result + satsBarn
        result = 31 * result + satsBarnDelvis
        result = 31 * result + fom.hashCode()
        result = 31 * result + tom.hashCode()
        return result
    }
}

fun mapIverksettDTO(vedtak: Vedtak) =
    IverksettDto(
        sakId = vedtak.sakId.verdi,
        behandlingId = vedtak.id.uuidPart(), // GeneriskIdSomUUID(vedtak.id.uuid()),
        personident = Personident(
            verdi = vedtak.ident,
        ),
        vedtak = VedtaksdetaljerDto(
            vedtakstidspunkt = vedtak.vedtakstidspunkt, // tidspunkt fra meldekortbehanling
            saksbehandlerId = vedtak.saksbehandler,
            beslutterId = vedtak.saksbehandler,
            brukersNavKontor = BrukersNavKontor(
                enhet = vedtak.brukerNavkontor,
            ),
            utbetalinger = vedtak.utbetalinger
                .asSequence()
                .sortedBy { it.dato }
                .groupBy { it.løpenr }
                .map { (_, dager) ->
                    dager
                        .lagUtbetalingDtoMedTiltaktypePerDag(vedtak.utfallsperioder)
                        .fold(emptyList<UtbetalingDtoMedTiltaktype>()) { periodisertliste, nesteDag ->
                            periodisertliste.slåSammen(nesteDag)
                        }
                }
                .flatten()
                .filter { it.beløpPerDag > 0 }
                .toUtbetalingDto()
                .toList(),
        ),
        forrigeIverksetting = vedtak.forrigeVedtak?.let {
            ForrigeIverksettingDto(
                behandlingId = it.uuidPart(), // GeneriskIdSomUUID(it.uuid()),
            )
        },
    )

data class UtbetalingDtoMedTiltaktype(
    val beløpPerDag: Int,
    val fraOgMedDato: LocalDate,
    val tilOgMedDato: LocalDate,
    val stønadsdata: StønadsdataDtoMedTiltaktype,
)
data class StønadsdataDtoMedTiltaktype(
    val stønadstype: TiltakType,
    val barnetillegg: Boolean,
)

private fun List<UtbetalingDtoMedTiltaktype>.toUtbetalingDto(): List<UtbetalingDto> {
    return this.map {
        UtbetalingDto(
            beløpPerDag = it.beløpPerDag,
            fraOgMedDato = it.fraOgMedDato,
            tilOgMedDato = it.tilOgMedDato,
            stønadsdata = StønadsdataTiltakspengerDto(
                stønadstype = it.stønadsdata.stønadstype.mapStønadstype(),
                barnetillegg = it.stønadsdata.barnetillegg,
            ),
        )
    }
}

private fun List<UtbetalingDag>.lagUtbetalingDtoMedTiltaktypePerDag(utfallsperioder: List<Utfallsperiode>): List<UtbetalingDtoMedTiltaktype> {
    val dager = this.map { dag ->
        UtbetalingDtoMedTiltaktype(
            beløpPerDag = dag.mapSats(),
            fraOgMedDato = dag.dato,
            tilOgMedDato = dag.dato,
            stønadsdata = StønadsdataDtoMedTiltaktype(
                stønadstype = dag.tiltaktype,
                barnetillegg = false,
            ),
        )
    }

    return if (utfallsperioder.any { it.antallBarn > 0 }) {
        dager + this.map { dag ->
            UtbetalingDtoMedTiltaktype(
                beløpPerDag = dag.mapBarnetilleggSats(utfallsperioder.antallBarn(dag.dato)),
                fraOgMedDato = dag.dato,
                tilOgMedDato = dag.dato,
                stønadsdata = StønadsdataDtoMedTiltaktype(
                    stønadstype = dag.tiltaktype,
                    barnetillegg = true,
                ),
            )
        }
    } else {
        dager
    }
}

private fun List<UtbetalingDtoMedTiltaktype>.slåSammen(neste: UtbetalingDtoMedTiltaktype): List<UtbetalingDtoMedTiltaktype> {
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

fun UtbetalingDag.mapSats(): Int = when (this.status) {
    UtbetalingDagStatus.FullUtbetaling -> Satser.sats(this.dato).sats
    UtbetalingDagStatus.DelvisUtbetaling -> Satser.sats(this.dato).satsDelvis
    UtbetalingDagStatus.IngenUtbetaling -> 0
}

fun UtbetalingDag.mapBarnetilleggSats(antallBarn: Int): Int = when (this.status) {
    UtbetalingDagStatus.FullUtbetaling -> Satser.sats(this.dato).satsBarnetillegg * antallBarn
    UtbetalingDagStatus.DelvisUtbetaling -> Satser.sats(this.dato).satsBarnetilleggDelvis * antallBarn
    UtbetalingDagStatus.IngenUtbetaling -> 0
}

private fun TiltakType.mapStønadstype(): StønadTypeTiltakspenger = when (this) {
    TiltakType.AMBF1 -> TODO()
    TiltakType.ABOPPF -> TODO()
    TiltakType.ABUOPPF -> TODO()
    TiltakType.ABIST -> TODO()
    TiltakType.ABTBOPPF -> TODO()
    TiltakType.ARBFORB -> StønadTypeTiltakspenger.ARBEIDSFORBEREDENDE_TRENING
    TiltakType.AMO -> TODO()
    TiltakType.AMOE -> TODO()
    TiltakType.AMOB -> TODO()
    TiltakType.AMOY -> TODO()
    TiltakType.PRAKSORD -> TODO()
    TiltakType.PRAKSKJERM -> TODO()
    TiltakType.ARBRRHBAG -> TODO()
    TiltakType.ARBRRHBSM -> TODO()
    TiltakType.ARBRRHDAG -> StønadTypeTiltakspenger.ARBEIDSRETTET_REHABILITERING
    TiltakType.ARBRDAGSM -> TODO()
    TiltakType.ARBRRDOGN -> TODO()
    TiltakType.ARBDOGNSM -> TODO()
    TiltakType.ARBTREN -> StønadTypeTiltakspenger.ARBEIDSTRENING
    TiltakType.AVKLARAG -> StønadTypeTiltakspenger.AVKLARING
    TiltakType.AVKLARUS -> TODO()
    TiltakType.AVKLARSP -> TODO()
    TiltakType.AVKLARKV -> TODO()
    TiltakType.AVKLARSV -> TODO()
    TiltakType.DIGIOPPARB -> StønadTypeTiltakspenger.DIGITAL_JOBBKLUBB
    TiltakType.ENKELAMO -> StønadTypeTiltakspenger.ENKELTPLASS_AMO
    TiltakType.ENKFAGYRKE -> StønadTypeTiltakspenger.ENKELTPLASS_VGS_OG_HØYERE_YRKESFAG
    TiltakType.KAT -> TODO()
    TiltakType.VALS -> TODO()
    TiltakType.FORSAMOENK -> TODO()
    TiltakType.FORSFAGENK -> TODO()
    TiltakType.FORSHOYUTD -> TODO()
    TiltakType.FUNKSJASS -> TODO()
    TiltakType.GRUPPEAMO -> StønadTypeTiltakspenger.GRUPPE_AMO
    TiltakType.GRUFAGYRKE -> StønadTypeTiltakspenger.GRUPPE_VGS_OG_HØYERE_YRKESFAG
    TiltakType.HOYEREUTD -> StønadTypeTiltakspenger.HØYERE_UTDANNING
    TiltakType.INDJOBSTOT -> StønadTypeTiltakspenger.INDIVIDUELL_JOBBSTØTTE
    TiltakType.IPSUNG -> StønadTypeTiltakspenger.INDIVIDUELL_KARRIERESTØTTE_UNG
    TiltakType.INDOPPFOLG -> TODO()
    TiltakType.INKLUTILS -> TODO()
    TiltakType.JOBBKLUBB -> TODO()
    TiltakType.JOBBFOKUS -> TODO()
    TiltakType.JOBBK -> StønadTypeTiltakspenger.JOBBKLUBB
    TiltakType.JOBBBONUS -> TODO()
    TiltakType.MENTOR -> TODO()
    TiltakType.NETTAMO -> TODO()
    TiltakType.INDOPPFAG -> StønadTypeTiltakspenger.OPPFØLGING
    TiltakType.INDOPPFSP -> TODO()
    TiltakType.INDOPPRF -> TODO()
    TiltakType.REFINO -> TODO()
    TiltakType.SPA -> TODO()
    TiltakType.SUPPEMP -> TODO()
    TiltakType.TILPERBED -> TODO()
    TiltakType.UTDYRK -> TODO()
    TiltakType.UTBHLETTPS -> TODO()
    TiltakType.UTBHPSLD -> TODO()
    TiltakType.UTBHSAMLI -> TODO()
    TiltakType.UTVAOONAV -> StønadTypeTiltakspenger.UTVIDET_OPPFØLGING_I_NAV
    TiltakType.UTVOPPFOPL -> StønadTypeTiltakspenger.UTVIDET_OPPFØLGING_I_OPPLÆRING
    TiltakType.OPPLT2AAR -> TODO()
    TiltakType.FORSOPPLEV -> StønadTypeTiltakspenger.FORSØK_OPPLÆRING_LENGRE_VARIGHET
    TiltakType.UTEN_TILTAK -> throw IllegalStateException("Skal ikke være mulig å sende utbetaling for dag uten tiltak")
}
