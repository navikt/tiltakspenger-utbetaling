package no.nav.tiltakspenger.utbetaling.repository

import no.nav.tiltakspenger.utbetaling.domene.BehandlingId
import no.nav.tiltakspenger.utbetaling.domene.SakId
import no.nav.tiltakspenger.utbetaling.domene.Vedtak
import no.nav.tiltakspenger.utbetaling.domene.VedtakId

interface VedtakRepo {
    fun lagre(vedtak: Vedtak)

    fun hentVedtak(vedtakId: VedtakId): Vedtak?

    fun hentAlleVedtakForSak(sakId: SakId): List<Vedtak>

    fun hentSakIdForBehandling(behandlingId: BehandlingId): SakId?

    fun hentVedtakForBehandling(behandlingId: BehandlingId): Vedtak?

    fun hentForrigeUtbetalingVedtak(sakId: SakId): Vedtak?
}
