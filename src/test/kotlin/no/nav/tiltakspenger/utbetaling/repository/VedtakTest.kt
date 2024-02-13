package no.nav.tiltakspenger.utbetaling.repository

import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.utbetaling.db.PostgresTestcontainer
import no.nav.tiltakspenger.utbetaling.db.flywayCleanAndMigrate
import no.nav.tiltakspenger.utbetaling.domene.SakId
import no.nav.tiltakspenger.utbetaling.domene.Vedtak
import no.nav.tiltakspenger.utbetaling.domene.VedtakId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Testcontainers
internal class VedtakTest {
    private val utbetalingRepo = UtbetalingRepo()
    private val vedtakRepo: VedtakRepo = VedtakRepoImpl(utbetalingRepo)

    init {
        PostgresTestcontainer.start()
    }

    @BeforeEach
    fun setup() {
        flywayCleanAndMigrate()
    }

    @Test
    fun `lagre og hente et rammevedtak`() {
        val vedtakId = VedtakId.random()
        val vedtak = Vedtak(
            id = vedtakId,
            sakId = SakId.random(),
            utløsendeId = "vedtakIdFraVedtak",
            ident = "12345678901",
            antallBarn = 0,
            brukerNavkontor = "0219",
            vedtakstidspunkt = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
            saksbehandler = "saksbehandler",
            beslutter = "beslutter",
            utbetalinger = emptyList(),
            forrigeVedtak = null,
        )
        vedtakRepo.lagre(vedtak)

        val hentetRammevedtak = vedtakRepo.hentVedtak(vedtakId)

        hentetRammevedtak shouldBe vedtak
    }
}
