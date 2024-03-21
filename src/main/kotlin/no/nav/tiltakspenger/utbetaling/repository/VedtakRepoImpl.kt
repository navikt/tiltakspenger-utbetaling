package no.nav.tiltakspenger.utbetaling.repository

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.tiltakspenger.utbetaling.db.DataSource
import no.nav.tiltakspenger.utbetaling.domene.BehandlingId
import no.nav.tiltakspenger.utbetaling.domene.SakId
import no.nav.tiltakspenger.utbetaling.domene.Vedtak
import no.nav.tiltakspenger.utbetaling.domene.VedtakId
import org.intellij.lang.annotations.Language

class VedtakRepoImpl(
    private val utbetalingRepo: UtbetalingRepo = UtbetalingRepo(),
    private val utfallsperiodeDAO: UtfallsperiodeDAO = UtfallsperiodeDAO(),
) : VedtakRepo {
    override fun lagre(vedtak: Vedtak) {
        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { tx ->
                tx.run(
                    queryOf(
                        sqlLagre,
                        mapOf(
                            "id" to vedtak.id.toString(),
                            "sakId" to vedtak.sakId.verdi,
                            "utlosendeId" to vedtak.utløsendeId,
                            "ident" to vedtak.ident,
                            "brukerNavnkontor" to vedtak.brukerNavkontor,
                            "vedtakstidspunkt" to vedtak.vedtakstidspunkt,
                            "saksbehandler" to vedtak.saksbehandler,
                            "beslutter" to vedtak.beslutter,
                            "forrigeVedtakId" to vedtak.forrigeVedtak?.toString(),
                        ),
                    ).asUpdate,
                ).also {
                    utbetalingRepo.lagreUtbetalingForVedtak(vedtak.id, vedtak.utbetalinger, tx)
                    utfallsperiodeDAO.lagre(vedtak.id, vedtak.utfallsperioder, tx)
                }
            }
        }
    }

    override fun hentVedtak(vedtakId: VedtakId): Vedtak? {
        return sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                txSession.run(
                    queryOf(
                        sqlHentVedtak,
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

    override fun hentSakIdForBehandling(behandlingId: BehandlingId): SakId? {
        return sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                txSession.run(
                    queryOf(
                        sqlHentSakId,
                        mapOf(
                            "behandlingId" to behandlingId.toString(),
                        ),
                    ).map { row ->
                        row.toSakId()
                    }.asSingle,
                )
            }
        }
    }

    override fun hentAlleVedtakForSak(sakId: SakId): List<Vedtak> {
        return sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                txSession.run(
                    queryOf(
                        sqlHentAlleVedtakForSak,
                        mapOf(
                            "sakId" to sakId.verdi,
                        ),
                    ).map { row ->
                        row.toVedtak(txSession)
                    }.asList,
                )
            }
        }
    }

    override fun hentVedtakForBehandling(behandlingId: BehandlingId): Vedtak? {
        return sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                txSession.run(
                    queryOf(
                        sqlHentVedtakForBehandling,
                        mapOf(
                            "behandlingId" to behandlingId.toString(),
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
                        sqlHentSisteVedtakForSak,
                        mapOf(
                            "sakId" to sakId.verdi,
                        ),
                    ).map { row ->
                        row.toVedtak(txSession)
                    }.asSingle,
                )
            }
        }
    }

    private fun Row.toSakId(): SakId? {
        return stringOrNull("sakId")?.let { SakId(it) }
    }

    private fun Row.toVedtak(tx: TransactionalSession): Vedtak {
        val vedtakId = VedtakId.fromDb(string("id"))
        return Vedtak(
            id = vedtakId,
            sakId = SakId(string("sakId")),
            utløsendeId = string("utløsendeId"),
            ident = string("ident"),
            brukerNavkontor = string("brukerNavkontor"),
            vedtakstidspunkt = localDateTime("vedtakstidspunkt"),
            saksbehandler = string("saksbehandler"),
            beslutter = string("beslutter"),
            utbetalinger = utbetalingRepo.hentDagerForVedtak(vedtakId, tx),
            utfallsperioder = utfallsperiodeDAO.hent(vedtakId, tx),
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
            :brukerNavnkontor,    
            :vedtakstidspunkt,
            :saksbehandler,   
            :beslutter,
            :forrigeVedtakId
        )
    """.trimIndent()

    @Language("PostgreSQL")
    private val sqlHentVedtak = """
        select * from vedtak
        where id = :id
    """.trimIndent()

    @Language("PostgreSQL")
    private val sqlHentAlleVedtakForSak = """
        select * from vedtak
        where sakId = :sakId
    """.trimIndent()

    @Language("PostgreSQL")
    private val sqlHentSakId = """
        select sakid from vedtak
        where utløsendeid = :behandlingId
    """.trimIndent()

    @Language("PostgreSQL")
    private val sqlHentSisteVedtakForSak = """
        select * from vedtak
        where sakId = :sakId
        order by vedtakstidspunkt desc limit 1
    """.trimIndent()

    @Language("PostgreSQL")
    private val sqlHentVedtakForBehandling = """
        select * from vedtak
        where utløsendeId = :behandlingId
    """.trimIndent()
}
