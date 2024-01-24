package no.nav.tiltakspenger.utbetaling.repository

import kotliquery.Row
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.tiltakspenger.utbetaling.db.DataSource
import no.nav.tiltakspenger.utbetaling.domene.BehandlingId
import no.nav.tiltakspenger.utbetaling.domene.IverksettingResultat
import no.nav.tiltakspenger.utbetaling.domene.Rammevedtak
import no.nav.tiltakspenger.utbetaling.domene.RammevedtakId
import no.nav.tiltakspenger.utbetaling.domene.SakId
import org.intellij.lang.annotations.Language
import java.util.*

class RammevedtakRepoImpl() : RammevedtakRepo {
    override fun lagre(rammevedtak: Rammevedtak) {
        sessionOf(DataSource.hikariDataSource).use {
            it.transaction {
                it.run(
                    queryOf(
                        sqlLagre,
                        mapOf(
                            "id" to rammevedtak.id.toString(),
                            "sakId" to rammevedtak.sakId.id.toString(),
                            "behandlingId" to rammevedtak.behandlingId.toString(),
                            "personIdent" to rammevedtak.personIdent,
                            "iverksettingResultat" to rammevedtak.iverksettingResultat.name,
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
            id = RammevedtakId(id = UUID.fromString(string("id"))),
            sakId = SakId(id = UUID.fromString(string("sakId"))),
            behandlingId = BehandlingId(id = UUID.fromString(string("behandlingId"))),
            personIdent = string("personIdent"),
            iverksettingResultat = IverksettingResultat.valueOf(string("iverksettingResultat")),
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
            iverksettingResultat,        
            vedtakstidspunkt,
            saksbehandler,   
            beslutter       
        ) values (
            :id,              
            :sakId,           
            :behandlingId,    
            :personIdent,     
            :iverksettingResultat,        
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
