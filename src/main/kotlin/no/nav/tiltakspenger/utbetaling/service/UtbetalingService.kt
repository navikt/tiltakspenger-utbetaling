package no.nav.tiltakspenger.utbetaling.service

import no.nav.tiltakspenger.utbetaling.domene.BehandlingId
import no.nav.tiltakspenger.utbetaling.domene.GrunnlagDTO
import no.nav.tiltakspenger.utbetaling.domene.Utbetaling
import no.nav.tiltakspenger.utbetaling.domene.Vedtak
import no.nav.tiltakspenger.utbetaling.domene.VedtakId
import no.nav.tiltakspenger.utbetaling.service.ports.IverksettRespons

interface UtbetalingService {
    suspend fun mottaRammevedtakOgSendTilIverksett(vedtak: Vedtak): IverksettRespons

    suspend fun mottaUtbetalingOgSendTilIverksett(utbetaling: Utbetaling): IverksettRespons

    fun hentAlleVedtak(behandlingId: BehandlingId): List<Vedtak>

    fun hentVedtak(vedtakId: VedtakId): Vedtak?

    fun hentGrunnlag(grunnlagDTO: GrunnlagDTO): List<UtbetalingGrunnlagDag>
}
