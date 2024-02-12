package no.nav.tiltakspenger.utbetaling.repository

import kotliquery.TransactionalSession
import no.nav.tiltakspenger.utbetaling.domene.UtbetalingDag
import no.nav.tiltakspenger.utbetaling.domene.VedtakId

class UtbetalingDagerRepo() {
    fun lagreDagerForVedtak(utbetalingsdager: List<UtbetalingDag>) {
    }

    fun hentDagerForVedtak(vedtak: VedtakId, tx: TransactionalSession): List<UtbetalingDag> {
        return emptyList()
    }
}
