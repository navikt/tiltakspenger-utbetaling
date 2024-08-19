package no.nav.tiltakspenger.utbetaling.service

import no.nav.tiltakspenger.libs.common.BehandlingId
import no.nav.tiltakspenger.libs.common.VedtakId
import no.nav.tiltakspenger.utbetaling.client.iverksett.IverksettKlient
import no.nav.tiltakspenger.utbetaling.client.iverksett.IverksettKlient.Response
import no.nav.tiltakspenger.utbetaling.domene.Satser
import no.nav.tiltakspenger.utbetaling.domene.TiltakType
import no.nav.tiltakspenger.utbetaling.domene.Utbetaling
import no.nav.tiltakspenger.utbetaling.domene.UtbetalingDag
import no.nav.tiltakspenger.utbetaling.domene.UtbetalingDagStatus
import no.nav.tiltakspenger.utbetaling.domene.Vedtak
import no.nav.tiltakspenger.utbetaling.domene.antallBarn
import no.nav.tiltakspenger.utbetaling.domene.nyttUtbetalingVedtak
import no.nav.tiltakspenger.utbetaling.repository.StatistikkRepo
import no.nav.tiltakspenger.utbetaling.repository.VedtakRepo
import no.nav.tiltakspenger.utbetaling.routes.utbetaling.GrunnlagDTO
import no.nav.utsjekk.kontrakter.felles.Personident
import no.nav.utsjekk.kontrakter.felles.Satstype
import no.nav.utsjekk.kontrakter.felles.StønadTypeTiltakspenger
import no.nav.utsjekk.kontrakter.iverksett.ForrigeIverksettingV2Dto
import no.nav.utsjekk.kontrakter.iverksett.IverksettV2Dto
import no.nav.utsjekk.kontrakter.iverksett.StønadsdataTiltakspengerV2Dto
import no.nav.utsjekk.kontrakter.iverksett.UtbetalingV2Dto
import no.nav.utsjekk.kontrakter.iverksett.VedtaksdetaljerV2Dto
import java.time.LocalDate

class UtbetalingServiceImpl(
    private val vedtakRepo: VedtakRepo,
    private val iverksettKlient: IverksettKlient,
    private val statistikkRepo: StatistikkRepo,
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

        // litt kjipt å bruke helved sine dto'er som domene klasser egentlig. Burde kanskje lage våre egne?
        val iverksettDTO = mapIverksettDTO(vedtak)
        return iverksettKlient.iverksett(iverksettDTO).also {
            if (it.statusCode.value == 202) {
                // her er det nok fint med en transaksjon. Regner med at det fikser Hestad når han flytter utbetaling inn i vedtak
                vedtakRepo.lagre(vedtak)
                statistikkRepo.lagre(mapStatistikk(iverksettDTO))
            }
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
    IverksettV2Dto(
        sakId = vedtak.sakId.verdi,
        behandlingId = vedtak.id.uuidPart(), // GeneriskIdSomUUID(vedtak.id.uuid()),
        personident = Personident(
            verdi = vedtak.ident,
        ),
        vedtak = VedtaksdetaljerV2Dto(
            vedtakstidspunkt = vedtak.vedtakstidspunkt, // tidspunkt fra meldekortbehanling
            saksbehandlerId = vedtak.saksbehandler,
            beslutterId = vedtak.saksbehandler,
            utbetalinger = vedtak.utbetalinger
                .asSequence()
                .sortedBy { it.dato }
                .groupBy { it.løpenr }
                .map { (_, dager) ->
                    dager
                        .lagUtbetalingDtoMedTiltaktypePerDag(vedtak)
                        .fold(emptyList<UtbetalingDtoMedTiltaktype>()) { periodisertliste, nesteDag ->
                            periodisertliste.slåSammen(nesteDag)
                        }
                }
                .flatten()
                .filter { it.beløpPerDag > 0 }
                .toUtbetalingDto(vedtak.brukerNavkontor)
                .toList(),
        ),
        forrigeIverksetting = vedtak.forrigeVedtak?.let {
            ForrigeIverksettingV2Dto(
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
    val brukersNavKontor: String,
)

private fun List<UtbetalingDtoMedTiltaktype>.toUtbetalingDto(brukersNavKontor: String): List<UtbetalingV2Dto> {
    return this.map {
        UtbetalingV2Dto(
            beløp = it.beløpPerDag.toUInt(),
            satstype = Satstype.DAGLIG,
            fraOgMedDato = it.fraOgMedDato,
            tilOgMedDato = it.tilOgMedDato,
            stønadsdata = StønadsdataTiltakspengerV2Dto(
                stønadstype = it.stønadsdata.stønadstype.mapStønadstype(),
                barnetillegg = it.stønadsdata.barnetillegg,
                brukersNavKontor = brukersNavKontor,
            ),
        )
    }
}

private fun List<UtbetalingDag>.lagUtbetalingDtoMedTiltaktypePerDag(vedtak: Vedtak): List<UtbetalingDtoMedTiltaktype> {
    val dager = this.map { dag ->
        UtbetalingDtoMedTiltaktype(
            beløpPerDag = dag.mapSats(),
            fraOgMedDato = dag.dato,
            tilOgMedDato = dag.dato,
            stønadsdata = StønadsdataDtoMedTiltaktype(
                stønadstype = dag.tiltaktype,
                barnetillegg = false,
                brukersNavKontor = vedtak.brukerNavkontor,
            ),
        )
    }

    return if (vedtak.utfallsperioder.any { it.antallBarn > 0 }) {
        dager + this.map { dag ->
            UtbetalingDtoMedTiltaktype(
                beløpPerDag = dag.mapBarnetilleggSats(vedtak.utfallsperioder.antallBarn(dag.dato)),
                fraOgMedDato = dag.dato,
                tilOgMedDato = dag.dato,
                stønadsdata = StønadsdataDtoMedTiltaktype(
                    stønadstype = dag.tiltaktype,
                    barnetillegg = true,
                    brukersNavKontor = vedtak.brukerNavkontor,
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
