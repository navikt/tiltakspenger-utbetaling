package no.nav.tiltakspenger.utbetaling.client.iverksett

import no.nav.dagpenger.kontrakter.felles.BrukersNavKontor
import no.nav.dagpenger.kontrakter.felles.GeneriskIdSomUUID
import no.nav.dagpenger.kontrakter.felles.Personident
import no.nav.dagpenger.kontrakter.felles.StønadTypeTiltakspenger
import no.nav.dagpenger.kontrakter.iverksett.ForrigeIverksettingDto
import no.nav.dagpenger.kontrakter.iverksett.IverksettDto
import no.nav.dagpenger.kontrakter.iverksett.StønadsdataTiltakspengerDto
import no.nav.dagpenger.kontrakter.iverksett.UtbetalingDto
import no.nav.dagpenger.kontrakter.iverksett.VedtaksdetaljerDto
import no.nav.tiltakspenger.utbetaling.domene.TiltakType
import no.nav.tiltakspenger.utbetaling.domene.UtbetalingDag
import no.nav.tiltakspenger.utbetaling.domene.Vedtak
import no.nav.tiltakspenger.utbetaling.service.mapBarnetilleggSats
import no.nav.tiltakspenger.utbetaling.service.mapSats
import no.nav.tiltakspenger.utbetaling.service.ports.IverksettRespons
import java.time.LocalDate

object IverksettMapper {
    fun mapResponse(iverksettResponse: IverksettKlient.Response): IverksettRespons =
        IverksettRespons(
            ok = iverksettResponse.statusCode.value == 202,
            opprinneligStatusCodeValue = iverksettResponse.statusCode.value,
            opprinneligStatusCodeMessage = iverksettResponse.melding,
        )

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
                    gjelderFom = LocalDate.of(1970, 1, 1), // finne ut hva vi setter denne til
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
                    }
                    .flatten()
                    .filter { it.beløpPerDag > 0 },
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

    private fun UtbetalingDag.mapStønadstype(): StønadTypeTiltakspenger = when (this.tiltaktype) {
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
    }
}
