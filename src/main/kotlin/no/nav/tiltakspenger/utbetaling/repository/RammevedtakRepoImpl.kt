package no.nav.tiltakspenger.utbetaling.repository

import kotliquery.Row
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.tiltakspenger.utbetaling.db.DataSource
import org.intellij.lang.annotations.Language
import java.util.*

class RammevedtakRepoImpl() : RammevedtakRepo {
    override fun lagre() {
        sessionOf(DataSource.hikariDataSource).use {
            val id = UUID.randomUUID()
            it.transaction {
                it.run(
                    queryOf(
                        sqlLagre,
                        mapOf(
                            "id" to id,
                        ),
                    ).asUpdate,
                )
            }
        }
    }

    override fun hent(id: UUID) {
        return sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                txSession.run(
                    queryOf(
                        sqlHent,
                        mapOf(
                            "id" to id.toString(),
                        ),
                    ).map { row ->
                        row.toVedtak()
                    }.asSingle,
                )
            }
        }
    }

    private fun Row.toVedtak() {
    }

    @Language("PostgreSQL")
    private val sqlLagre = """
        insert into rammevedtak (
            id
        ) values (
            :id
        )
    """.trimIndent()

    @Language("PostgreSQL")
    private val sqlHent = """
        select * from rammevedtak
        where id = :id
    """.trimIndent()
}
