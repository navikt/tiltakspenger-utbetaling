package no.nav.tiltakspenger.utbetaling.repository

import kotliquery.TransactionalSession
import kotliquery.queryOf
import no.nav.tiltakspenger.utbetaling.domene.UtbetalingDag
import no.nav.tiltakspenger.utbetaling.domene.VedtakId
import org.intellij.lang.annotations.Language
import java.util.UUID

class UtbetalingRepo() {
    fun lagreUtbetalingForVedtak(vedtakId: VedtakId, utbetalingsdager: List<UtbetalingDag>, tx: TransactionalSession) {
        utbetalingsdager.groupBy({ it.meldekortId }, { it }).forEach { (meldekortId, dager) ->
            lagrePeriode(vedtakId, meldekortId, dager, tx)
        }
    }

    private fun lagrePeriode(vedtakId: VedtakId, meldekortId: UUID, dager: List<UtbetalingDag>, tx: TransactionalSession) {
        tx.run(
            queryOf(
                sqlLagreMeldekortPeriode,
                mapOf(
                    "vedtakId" to vedtakId.toString(),
                    "meldekortId" to meldekortId.toString(),
                    "lopenr" to dager.first().løpenr,
                ),
            ).asUpdate,
        ).also {
            dager.forEach {
                lagreEnDag(it, tx)
            }
        }
    }

    private fun lagreEnDag(utbetalingDag: UtbetalingDag, tx: TransactionalSession) {
        tx.run(
            queryOf(
                sqlLagreUtbetalingDag,
                mapOf(
                    "meldekortId" to utbetalingDag.meldekortId.toString(),
                    "dato" to utbetalingDag.dato,
                    "status" to utbetalingDag.status.name,
                    "tiltakstype" to utbetalingDag.tiltaktype.name,
                ),
            ).asUpdate,
        )
    }

    fun hentDagerForVedtak(vedtak: VedtakId, tx: TransactionalSession): List<UtbetalingDag> {
        return emptyList()
    }

    @Language("PostgreSQL")
    private val sqlLagreMeldekortPeriode = """
            insert into meldekortPeriode (      
                meldekortId,
                vedtakId,           
                løpenr
            ) values (
                :meldekortId,              
                :vedtakId,                   
                :lopenr
            )
    """.trimIndent()

    @Language("PostgreSQL")
    private val sqlLagreUtbetalingDag = """
            insert into utbetalingdag (      
                meldekortId,
                dato,           
                status,
                tiltaktype
            ) values (
                :meldekortId,              
                :dato,                   
                :status,
                :tiltaktype
            )
    """.trimIndent()
}
