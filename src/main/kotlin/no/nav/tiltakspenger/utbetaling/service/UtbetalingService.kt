package no.nav.tiltakspenger.utbetaling.service

import no.nav.tiltakspenger.libs.common.BehandlingId
import no.nav.tiltakspenger.libs.common.VedtakId
import no.nav.tiltakspenger.utbetaling.client.iverksett.IverksettKlient.Response
import no.nav.tiltakspenger.utbetaling.domene.Utbetaling
import no.nav.tiltakspenger.utbetaling.domene.Vedtak
import no.nav.tiltakspenger.utbetaling.routes.utbetaling.GrunnlagDTO

interface UtbetalingService {
    suspend fun mottaRammevedtakOgSendTilIverksett(vedtak: Vedtak): Response

    suspend fun mottaUtbetalingOgSendTilIverksett(utbetaling: Utbetaling): Response

    fun hentAlleVedtak(behandlingId: BehandlingId): List<Vedtak>

    fun hentVedtak(vedtakId: VedtakId): Vedtak?

    fun hentGrunnlag(grunnlagDTO: GrunnlagDTO): List<UtbetalingGrunnlagDag>
}
