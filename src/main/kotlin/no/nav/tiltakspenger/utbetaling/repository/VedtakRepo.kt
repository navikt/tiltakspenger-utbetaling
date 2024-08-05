package no.nav.tiltakspenger.utbetaling.repository

import no.nav.tiltakspenger.libs.common.BehandlingId
import no.nav.tiltakspenger.libs.common.VedtakId
import no.nav.tiltakspenger.utbetaling.domene.SakId
import no.nav.tiltakspenger.utbetaling.domene.Vedtak

interface VedtakRepo {
    fun lagre(vedtak: Vedtak)

    fun hentVedtak(vedtakId: VedtakId): Vedtak?

    fun hentAlleVedtakForSak(sakId: SakId): List<Vedtak>

    fun hentSakIdForBehandling(behandlingId: BehandlingId): SakId?

    fun hentVedtakForBehandling(behandlingId: BehandlingId): Vedtak?

    fun hentForrigeUtbetalingVedtak(sakId: SakId): Vedtak?
}
