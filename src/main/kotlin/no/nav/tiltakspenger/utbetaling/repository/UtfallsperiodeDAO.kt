package no.nav.tiltakspenger.utbetaling.repository

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import no.nav.tiltakspenger.utbetaling.domene.UlidBase.Companion.random
import no.nav.tiltakspenger.utbetaling.domene.UtfallForPeriode
import no.nav.tiltakspenger.utbetaling.domene.Utfallsperiode
import no.nav.tiltakspenger.utbetaling.domene.VedtakId
import org.intellij.lang.annotations.Language

class UtfallsperiodeDAO {

    fun hent(vedtakId: VedtakId, txSession: TransactionalSession): List<Utfallsperiode> {
        return txSession.run(
            queryOf(hentUtfallsperioderForVedtak, vedtakId.toString())
                .map { row -> row.toUtfallsperiode() }
                .asList,
        )
    }

    fun lagre(vedtakId: VedtakId, utfallsperioder: List<Utfallsperiode>, txSession: TransactionalSession) {
        utfallsperioder.forEach { utfallsperiode ->
            lagreUtfallsperiode(vedtakId, utfallsperiode, txSession)
        }
    }

    private fun lagreUtfallsperiode(
        vedtakId: VedtakId,
        utfallsperiode: Utfallsperiode,
        txSession: TransactionalSession,
    ) {
        txSession.run(
            queryOf(
                lagreUtfallsperiode,
                mapOf(
                    "id" to random(ULID_PREFIX_UTFALLSPERIODE).toString(),
                    "vedtakId" to vedtakId.toString(),
                    "fom" to utfallsperiode.fom,
                    "tom" to utfallsperiode.tom,
                    "antallBarn" to utfallsperiode.antallBarn,
                    "utfall" to utfallsperiode.utfall.name,
                ),
            ).asUpdate,
        )
    }

    private fun Row.toUtfallsperiode(): Utfallsperiode {
        return Utfallsperiode(
            fom = localDate("fom"),
            tom = localDate("tom"),
            antallBarn = int("antall_barn"),
            utfall = UtfallForPeriode.valueOf(string("utfall")),
        )
    }

    @Language("SQL")
    private val lagreUtfallsperiode = """
        insert into utfallsperiode (
            id,
            vedtak_id,
            fom,
            tom,
            antall_barn,
            utfall
        ) values (
            :id,
            :vedtakId,
            :fom,
            :tom,
            :antallBarn,
            :utfall
        )
    """.trimIndent()

    @Language("SQL")
    private val hentUtfallsperioderForVedtak = "select * from utfallsperiode where vedtak_id = ?"

    companion object {
        private const val ULID_PREFIX_UTFALLSPERIODE = "uperiode"
    }
}
