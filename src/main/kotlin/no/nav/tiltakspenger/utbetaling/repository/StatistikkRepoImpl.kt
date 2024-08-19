package no.nav.tiltakspenger.utbetaling.repository

import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.tiltakspenger.utbetaling.db.DataSource
import no.nav.tiltakspenger.utbetaling.domene.Statistikk
import org.intellij.lang.annotations.Language

class StatistikkRepoImpl() : StatistikkRepo {
    override fun lagre(
        statistikk: Statistikk,
    ) {
        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { tx ->
                tx.run(
                    queryOf(
                        sqlLagre,
                        mapOf(
                            "posteringId" to statistikk.posteringId,
                            "sakId" to statistikk.sakId,
                            "belop" to statistikk.beløp,
                            "belopBeskrivelse" to statistikk.beløpBeskrivelse,
                            "aarsak" to statistikk.årsak,
                            "posteringDato" to statistikk.posteringDato,
                            "gyldigFraDatoPostering" to statistikk.gyldigFraDatoPostering,
                            "gyldigTilDatoPostering" to statistikk.gyldigTilDatoPostering,
                        ),
                    ).asUpdate,
                )
            }
        }
    }

    @Language("PostgreSQL")
    private val sqlLagre = """
        insert into statistikk (
            postering_id,
            sakId,
            beløp,
            beløp_beskrivelse,
            aarsak,
            postering_dato,
            gyldig_fra_dato_postering,
            gyldig_til_dato_postering
        ) values (
            :posteringId,
            :sakId,
            :belop,
            :belopBeskrivelse,
            :aarsak,
            :posteringDato,
            :gyldigFraDatoPostering,
            :gyldigTilDatoPostering
        )
    """.trimIndent()
}
