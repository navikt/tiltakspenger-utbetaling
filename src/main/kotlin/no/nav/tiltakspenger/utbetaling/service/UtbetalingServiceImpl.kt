package no.nav.tiltakspenger.utbetaling.service

import no.nav.tiltakspenger.utbetaling.domene.BehandlingId
import no.nav.tiltakspenger.utbetaling.domene.GrunnlagDTO
import no.nav.tiltakspenger.utbetaling.domene.Satser
import no.nav.tiltakspenger.utbetaling.domene.Utbetaling
import no.nav.tiltakspenger.utbetaling.domene.UtbetalingDag
import no.nav.tiltakspenger.utbetaling.domene.UtbetalingDagStatus
import no.nav.tiltakspenger.utbetaling.domene.Vedtak
import no.nav.tiltakspenger.utbetaling.domene.VedtakId
import no.nav.tiltakspenger.utbetaling.domene.nyttVedtak
import no.nav.tiltakspenger.utbetaling.service.ports.IverksettGateway
import no.nav.tiltakspenger.utbetaling.service.ports.IverksettRespons
import no.nav.tiltakspenger.utbetaling.service.ports.VedtakRepo
import java.time.LocalDate

class UtbetalingServiceImpl(
    private val vedtakRepo: VedtakRepo,
    private val iverskettGateway: IverksettGateway,
) : UtbetalingService {

    override suspend fun mottaRammevedtakOgSendTilIverksett(vedtak: Vedtak): IverksettRespons =
        iverskettGateway.iverksett(vedtak).also {
            if (it.ok) vedtakRepo.lagre(vedtak)
        }

    override suspend fun mottaUtbetalingOgSendTilIverksett(utbetaling: Utbetaling): IverksettRespons {
        val forrigeVedtak = vedtakRepo.hentForrigeUtbetalingVedtak(utbetaling.sakId)
        checkNotNull(forrigeVedtak) { "Fant ikke forrige utbetalingvedtak" }

        val vedtak = forrigeVedtak.nyttVedtak(
            saksbehandler = utbetaling.saksbehandler,
            utløsendeId = utbetaling.utløsendeMeldekortId,
            utbetalinger = utbetaling.utbetalingDager,
        )

        return iverskettGateway.iverksett(vedtak).also {
            if (it.ok) vedtakRepo.lagre(vedtak)
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
                antallBarn = vedtak.antallBarn,
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
