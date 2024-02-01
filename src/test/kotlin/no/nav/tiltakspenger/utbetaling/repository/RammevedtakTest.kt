package no.nav.tiltakspenger.utbetaling.repository

import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.utbetaling.db.PostgresTestcontainer
import no.nav.tiltakspenger.utbetaling.db.flywayCleanAndMigrate
import no.nav.tiltakspenger.utbetaling.domene.BehandlingId
import no.nav.tiltakspenger.utbetaling.domene.Rammevedtak
import no.nav.tiltakspenger.utbetaling.domene.RammevedtakId
import no.nav.tiltakspenger.utbetaling.domene.SakId
import no.nav.tiltakspenger.utbetaling.domene.VedtakUtfall
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Testcontainers
internal class RammevedtakTest {
    private val rammevedtakRepo: RammevedtakRepo = RammevedtakRepoImpl()

    init {
        PostgresTestcontainer.start()
    }

    @BeforeEach
    fun setup() {
        flywayCleanAndMigrate()
    }

    @Test
    fun `lagre og hente et rammevedtak`() {
        val rammevedtakId = RammevedtakId.random()
        val rammevedtak = Rammevedtak(
            id = rammevedtakId,
            sakId = SakId.random(),
            saksnummer = "saksnummer",
            behandlingId = BehandlingId.random(),
            personIdent = "12345678901",
            fom = LocalDate.of(2024, 1, 1),
            tom = LocalDate.of(2024, 3, 1),
            vedtakUtfall = VedtakUtfall.INNVILGET,
            vedtakstidspunkt = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
            saksbehandler = "saksbehandler",
            beslutter = "beslutter",
        )
        rammevedtakRepo.lagre(rammevedtak)

        val hentetRammevedtak = rammevedtakRepo.hent(rammevedtakId)

        hentetRammevedtak shouldBe rammevedtak
    }
}
