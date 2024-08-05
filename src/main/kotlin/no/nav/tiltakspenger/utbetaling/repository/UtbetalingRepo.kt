package no.nav.tiltakspenger.utbetaling.repository

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import no.nav.tiltakspenger.libs.common.VedtakId
import no.nav.tiltakspenger.utbetaling.domene.TiltakType
import no.nav.tiltakspenger.utbetaling.domene.UtbetalingDag
import no.nav.tiltakspenger.utbetaling.domene.UtbetalingDagStatus
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
                lagreEnDag(vedtakId, it, tx)
            }
        }
    }

    private fun lagreEnDag(vedtakId: VedtakId, utbetalingDag: UtbetalingDag, tx: TransactionalSession) {
        tx.run(
            queryOf(
                sqlLagreUtbetalingDag,
                mapOf(
                    "vedtakId" to vedtakId.toString(),
                    "meldekortId" to utbetalingDag.meldekortId.toString(),
                    "dato" to utbetalingDag.dato,
                    "status" to utbetalingDag.status.name,
                    "tiltaktype" to utbetalingDag.tiltaktype.name,
                ),
            ).asUpdate,
        )
    }

    fun hentDagerForVedtak(vedtakId: VedtakId, tx: TransactionalSession): List<UtbetalingDag> {
        return tx.run(
            queryOf(
                sqlHentDager,
                mapOf(
                    "vedtakId" to vedtakId.toString(),
                ),
            ).map { row ->
                row.toDag()
            }.asList,
        )
    }

    private fun Row.toDag() = UtbetalingDag(
        dato = localDate("dato"),
        tiltaktype = TiltakType.valueOf(string("tiltaktype")),
        status = UtbetalingDagStatus.valueOf(string("status")),
        meldekortId = UUID.fromString(string("meldekortId")),
        løpenr = int("løpenr"),
    )

    @Language("PostgreSQL")
    private val sqlHentDager = """
        select u.*, p.løpenr
          from utbetalingdag u
        
        inner join meldekortPeriode p
                on p.meldekortId = u.meldekortId
               and p.vedtakid = u.vedtakid
            
        where p.vedtakId = :vedtakId
    """.trimIndent()

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
                vedtakId,
                meldekortId,
                dato,           
                status,
                tiltaktype
            ) values (
                :vedtakId,
                :meldekortId,              
                :dato,                   
                :status,
                :tiltaktype
            )
    """.trimIndent()
}
