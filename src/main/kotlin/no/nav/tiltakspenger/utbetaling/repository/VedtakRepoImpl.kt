package no.nav.tiltakspenger.utbetaling.repository

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.tiltakspenger.utbetaling.db.DataSource
import no.nav.tiltakspenger.utbetaling.domene.SakId
import no.nav.tiltakspenger.utbetaling.domene.Vedtak
import no.nav.tiltakspenger.utbetaling.domene.VedtakId
import org.intellij.lang.annotations.Language

class VedtakRepoImpl(
    private val utbetalingRepo: UtbetalingRepo = UtbetalingRepo(),
) : VedtakRepo {
    override fun lagre(vedtak: Vedtak) {
        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { tx ->
                tx.run(
                    queryOf(
                        sqlLagre,
                        mapOf(
                            "id" to vedtak.id.toString(),
                            "sakId" to vedtak.sakId.toString(),
                            "utlosendeId" to vedtak.utløsendeId,
                            "ident" to vedtak.ident,
                            "antallBarn" to vedtak.antallBarn,
                            "brukerNavnkontor" to vedtak.brukerNavkontor,
                            "vedtakstidspunkt" to vedtak.vedtakstidspunkt,
                            "saksbehandler" to vedtak.saksbehandler,
                            "beslutter" to vedtak.beslutter,
                            "forrigeVedtakId" to vedtak.forrigeVedtak?.toString(),
                        ),
                    ).asUpdate,
                ).also {
                    utbetalingRepo.lagreUtbetalingForVedtak(vedtak.id, vedtak.utbetalinger, tx)
                }
            }
        }
    }

    override fun hentVedtak(vedtakId: VedtakId): Vedtak? {
        return sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                txSession.run(
                    queryOf(
                        sqlHentForVedtak,
                        mapOf(
                            "id" to vedtakId.toString(),
                        ),
                    ).map { row ->
                        row.toVedtak(txSession)
                    }.asSingle,
                )
            }
        }
    }

    override fun hentForrigeUtbetalingVedtak(sakId: SakId): Vedtak? {
        return sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                txSession.run(
                    queryOf(
                        sqlHentForSak,
                        mapOf(
                            "sakId" to sakId.toString(),
                        ),
                    ).map { row ->
                        row.toVedtak(txSession)
                    }.asSingle,
                )
            }
        }
    }

    private fun Row.toVedtak(tx: TransactionalSession): Vedtak {
        val vedtakId = VedtakId.fromDb(string("id"))
        return Vedtak(
            id = vedtakId,
            sakId = SakId.fromDb(string("sakId")),
            utløsendeId = string("utløsendeId"),
            ident = string("ident"),
            antallBarn = int("antallBarn"),
            brukerNavkontor = string("brukerNavkontor"),
            vedtakstidspunkt = localDateTime("vedtakstidspunkt"),
            saksbehandler = string("saksbehandler"),
            beslutter = string("beslutter"),
            utbetalinger = utbetalingRepo.hentDagerForVedtak(vedtakId, tx),
            forrigeVedtak = stringOrNull("forrigeVedtakId")?.let { VedtakId.fromDb(it) },
        )
    }

    @Language("PostgreSQL")
    private val sqlLagre = """
        insert into vedtak (
            id,              
            sakId,           
            utløsendeId,    
            ident, 
            antallBarn,
            brukerNavkontor,  
            vedtakstidspunkt,
            saksbehandler,   
            beslutter,
            forrigeVedtakId
        ) values (
            :id,              
            :sakId,                   
            :utlosendeId,    
            :ident,   
            :antallBarn,
            :brukerNavnkontor,    
            :vedtakstidspunkt,
            :saksbehandler,   
            :beslutter,
            :forrigeVedtakId
        )
    """.trimIndent()

    @Language("PostgreSQL")
    private val sqlHentForVedtak = """
        select * from vedtak
        where id = :id
    """.trimIndent()

    @Language("PostgreSQL")
    private val sqlHentForSak = """
        select * from vedtak
        where sakId = :sakId
          and forrigeVedtakId is null
    """.trimIndent()
}
