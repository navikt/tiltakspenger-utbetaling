package no.nav.tiltakspenger.utbetaling.repository

import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.utbetaling.db.PostgresTestcontainer
import no.nav.tiltakspenger.utbetaling.db.flywayCleanAndMigrate
import no.nav.tiltakspenger.utbetaling.domene.SakId
import no.nav.tiltakspenger.utbetaling.domene.TiltakType
import no.nav.tiltakspenger.utbetaling.domene.UtbetalingDag
import no.nav.tiltakspenger.utbetaling.domene.UtbetalingDagStatus
import no.nav.tiltakspenger.utbetaling.domene.UtfallForPeriode
import no.nav.tiltakspenger.utbetaling.domene.Utfallsperiode
import no.nav.tiltakspenger.utbetaling.domene.Vedtak
import no.nav.tiltakspenger.utbetaling.domene.VedtakId
import no.nav.tiltakspenger.utbetaling.service.januar
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

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
    fun `lagre og hente rammevedtak og utbetlingvedtak`() {
        val vedtakId1 = VedtakId.random()
        val vedtakId2 = VedtakId.random()
        val meldekortId1 = UUID.randomUUID()
        val meldekortId2 = UUID.randomUUID()

        val utbetalinger = listOf(
            lagUtbetalingDag(1.januar(), meldekortId1 = meldekortId1, løpenr = 1),
            lagUtbetalingDag(2.januar(), meldekortId1 = meldekortId1, løpenr = 1),
            lagUtbetalingDag(3.januar(), meldekortId1 = meldekortId1, løpenr = 1),
            lagUtbetalingDag(4.januar(), meldekortId1 = meldekortId2, løpenr = 2),
            lagUtbetalingDag(5.januar(), meldekortId1 = meldekortId2, løpenr = 2),
            lagUtbetalingDag(6.januar(), meldekortId1 = meldekortId2, løpenr = 2),
        )
        val vedtak1 = lagVedtak(vedtakId = vedtakId1)
        val vedtak2 = lagVedtak(vedtakId = vedtakId2, utbetalinger = utbetalinger, forrigeVedtakId = vedtakId1)

        vedtakRepo.lagre(vedtak1)
        vedtakRepo.lagre(vedtak2)

        val hentetVedtak1 = vedtakRepo.hentVedtak(vedtakId1)
        val hentetVedtak2 = vedtakRepo.hentVedtak(vedtakId2)

        hentetVedtak1 shouldBe vedtak1
        hentetVedtak2 shouldBe vedtak2
    }
}

private fun lagUtbetalingDag(
    dato: LocalDate,
    tiltaktype: TiltakType = TiltakType.ENKELAMO,
    status: UtbetalingDagStatus = UtbetalingDagStatus.FullUtbetaling,
    meldekortId1: UUID = UUID.randomUUID(),
    løpenr: Int = 1,
) =
    UtbetalingDag(
        dato = dato,
        tiltaktype = tiltaktype,
        status = status,
        meldekortId = meldekortId1,
        løpenr = løpenr,
    )
private fun lagVedtak(
    vedtakId: VedtakId = VedtakId.random(),
    antallBarn: Int = 0,
    utbetalinger: List<UtbetalingDag> = emptyList(),
    forrigeVedtakId: VedtakId? = null,
) = Vedtak(
    id = vedtakId,
    sakId = SakId("SakId"),
    utløsendeId = "vedtakIdFraVedtak",
    ident = "12345678901",
    brukerNavkontor = "0219",
    vedtakstidspunkt = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
    saksbehandler = "saksbehandler",
    beslutter = "beslutter",
    utbetalinger = utbetalinger,
    utfallsperioder = listOf(
        Utfallsperiode(
            fom = utbetalinger.minByOrNull { it.dato }?.dato ?: 1.januar(),
            tom = utbetalinger.maxByOrNull { it.dato }?.dato ?: 11.januar(),
            antallBarn = antallBarn,
            utfall = UtfallForPeriode.GIR_RETT_TILTAKSPENGER,
        ),
    ),
    forrigeVedtak = forrigeVedtakId,
)
