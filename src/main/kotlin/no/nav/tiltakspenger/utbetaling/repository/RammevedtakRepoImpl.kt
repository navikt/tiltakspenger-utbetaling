package no.nav.tiltakspenger.utbetaling.repository

import kotliquery.Row
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.tiltakspenger.utbetaling.db.DataSource
import no.nav.tiltakspenger.utbetaling.domene.BehandlingId
import no.nav.tiltakspenger.utbetaling.domene.Rammevedtak
import no.nav.tiltakspenger.utbetaling.domene.RammevedtakId
import no.nav.tiltakspenger.utbetaling.domene.SakId
import no.nav.tiltakspenger.utbetaling.domene.VedtakUtfall
import org.intellij.lang.annotations.Language

class RammevedtakRepoImpl() : RammevedtakRepo {
    override fun lagre(rammevedtak: Rammevedtak) {
        sessionOf(DataSource.hikariDataSource).use {
            it.transaction {
                it.run(
                    queryOf(
                        sqlLagre,
                        mapOf(
                            "id" to rammevedtak.id.toString(),
                            "sakId" to rammevedtak.sakId.toString(),
                            "behandlingId" to rammevedtak.behandlingId.toString(),
                            "personIdent" to rammevedtak.personIdent,
                            "fom" to rammevedtak.fom,
                            "tom" to rammevedtak.tom,
                            "vedtakUtfall" to rammevedtak.vedtakUtfall.name,
                            "vedtakstidspunkt" to rammevedtak.vedtakstidspunkt,
                            "saksbehandler" to rammevedtak.saksbehandler,
                            "beslutter" to rammevedtak.beslutter,
                        ),
                    ).asUpdate,
                )
            }
        }
    }

    override fun hent(id: RammevedtakId): Rammevedtak? {
        return sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                txSession.run(
                    queryOf(
                        sqlHent,
                        mapOf(
                            "id" to id.toString(),
                        ),
                    ).map { row ->
                        row.toRammevedtak()
                    }.asSingle,
                )
            }
        }
    }

    private fun Row.toRammevedtak(): Rammevedtak {
        return Rammevedtak(
            id = RammevedtakId.fromDb(string("id")),
            sakId = SakId.fromDb(string("sakId")),
            behandlingId = BehandlingId.fromDb((string("behandlingId"))),
            personIdent = string("personIdent"),
            fom = localDate("fom"),
            tom = localDate("tom"),
            vedtakUtfall = VedtakUtfall.valueOf(string("vedtakUtfall")),
            vedtakstidspunkt = localDateTime("vedtakstidspunkt"),
            saksbehandler = string("saksbehandler"),
            beslutter = string("beslutter"),
        )
    }

    @Language("PostgreSQL")
    private val sqlLagre = """
        insert into rammevedtak (
            id,              
            sakId,           
            behandlingId,    
            personIdent, 
            fom,
            tom,
            vedtakUtfall,        
            vedtakstidspunkt,
            saksbehandler,   
            beslutter       
        ) values (
            :id,              
            :sakId,                   
            :behandlingId,    
            :personIdent,   
            :fom,
            :tom,
            :vedtakUtfall,        
            :vedtakstidspunkt,
            :saksbehandler,   
            :beslutter
        )
    """.trimIndent()

    @Language("PostgreSQL")
    private val sqlHent = """
        select * from rammevedtak
        where id = :id
    """.trimIndent()
}
