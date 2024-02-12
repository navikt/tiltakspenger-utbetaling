package no.nav.tiltakspenger.utbetaling.repository

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import no.nav.dagpenger.kontrakter.felles.StønadTypeTiltakspenger
import no.nav.dagpenger.kontrakter.iverksett.StønadsdataTiltakspengerDto
import no.nav.dagpenger.kontrakter.iverksett.UtbetalingDto
import no.nav.tiltakspenger.utbetaling.db.PostgresTestcontainer
import no.nav.tiltakspenger.utbetaling.db.flywayCleanAndMigrate
import no.nav.tiltakspenger.utbetaling.domene.SakId
import no.nav.tiltakspenger.utbetaling.domene.TiltakType.ENKELTAMO
import no.nav.tiltakspenger.utbetaling.domene.TiltakType.GRUPPEAMO
import no.nav.tiltakspenger.utbetaling.domene.UtbetalingDag
import no.nav.tiltakspenger.utbetaling.domene.UtbetalingDagStatus.DelvisUtbetaling
import no.nav.tiltakspenger.utbetaling.domene.UtbetalingDagStatus.FullUtbetaling
import no.nav.tiltakspenger.utbetaling.domene.UtbetalingDagStatus.IngenUtbetaling
import no.nav.tiltakspenger.utbetaling.domene.Vedtak
import no.nav.tiltakspenger.utbetaling.domene.VedtakId
import no.nav.tiltakspenger.utbetaling.domene.toUtbetalingDto
import no.nav.tiltakspenger.utbetaling.service.BARNETILLEGG_SATS
import no.nav.tiltakspenger.utbetaling.service.REDUSERT_SATS
import no.nav.tiltakspenger.utbetaling.service.SATS
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Testcontainers
internal class VedtakTest {
    private val utbetalingDagerRepo = UtbetalingDagerRepo()
    private val vedtakRepo: VedtakRepo = VedtakRepoImpl(utbetalingDagerRepo)

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
            gjeldendeVedtakId = "vedtakIdFraVedtak",
            ident = "12345678901",
            fom = LocalDate.of(2024, 1, 1),
            tom = LocalDate.of(2024, 3, 1),
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

    @Test
    fun `sjekk slå sammen perioder`() {
        val vedtakId = VedtakId.random()
        val vedtak = Vedtak(
            id = vedtakId,
            sakId = SakId.random(),
            gjeldendeVedtakId = "vedtakIdFraVedtak",
            ident = "12345678901",
            fom = LocalDate.of(2024, 1, 1),
            tom = LocalDate.of(2024, 3, 1),
            antallBarn = 0,
            brukerNavkontor = "0219",
            vedtakstidspunkt = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
            saksbehandler = "saksbehandler",
            beslutter = "beslutter",
            utbetalinger = listOf(
                UtbetalingDag(dato = 1.januar(), tiltaktype = ENKELTAMO, status = FullUtbetaling),
                UtbetalingDag(dato = 2.januar(), tiltaktype = ENKELTAMO, status = DelvisUtbetaling),
                UtbetalingDag(dato = 3.januar(), tiltaktype = ENKELTAMO, status = DelvisUtbetaling),
                UtbetalingDag(dato = 4.januar(), tiltaktype = ENKELTAMO, status = FullUtbetaling),
                UtbetalingDag(dato = 5.januar(), tiltaktype = ENKELTAMO, status = FullUtbetaling),
                UtbetalingDag(dato = 6.januar(), tiltaktype = GRUPPEAMO, status = FullUtbetaling),
                UtbetalingDag(dato = 7.januar(), tiltaktype = ENKELTAMO, status = FullUtbetaling),
                UtbetalingDag(dato = 8.januar(), tiltaktype = ENKELTAMO, status = FullUtbetaling),
                UtbetalingDag(dato = 9.januar(), tiltaktype = ENKELTAMO, status = IngenUtbetaling),
                UtbetalingDag(dato = 10.januar(), tiltaktype = ENKELTAMO, status = IngenUtbetaling),
                UtbetalingDag(dato = 11.januar(), tiltaktype = ENKELTAMO, status = FullUtbetaling),
            ),
            forrigeVedtak = null,
        )
        val dto = vedtak.toUtbetalingDto()
        dto.forEach {
            println(it)
        }

        dto shouldContainExactlyInAnyOrder listOf(
            UtbetalingDto(beløpPerDag = SATS, fraOgMedDato = 1.januar(), tilOgMedDato = 1.januar(), stønadsdata = enkeltAmoUtenBarn()),
            UtbetalingDto(beløpPerDag = REDUSERT_SATS, fraOgMedDato = 2.januar(), tilOgMedDato = 3.januar(), stønadsdata = enkeltAmoUtenBarn()),
            UtbetalingDto(beløpPerDag = SATS, fraOgMedDato = 4.januar(), tilOgMedDato = 5.januar(), stønadsdata = enkeltAmoUtenBarn()),
            UtbetalingDto(beløpPerDag = SATS, fraOgMedDato = 6.januar(), tilOgMedDato = 6.januar(), stønadsdata = gruppeAmoUtenBarn()),
            UtbetalingDto(beløpPerDag = SATS, fraOgMedDato = 7.januar(), tilOgMedDato = 8.januar(), stønadsdata = enkeltAmoUtenBarn()),
            UtbetalingDto(beløpPerDag = 0, fraOgMedDato = 9.januar(), tilOgMedDato = 10.januar(), stønadsdata = enkeltAmoUtenBarn()),
            UtbetalingDto(beløpPerDag = SATS, fraOgMedDato = 11.januar(), tilOgMedDato = 11.januar(), stønadsdata = enkeltAmoUtenBarn()),
        )
    }

    @Test
    fun `sjekk slå sammen perioder med 2 barn`() {
        val vedtakId = VedtakId.random()
        val vedtak = Vedtak(
            id = vedtakId,
            sakId = SakId.random(),
            gjeldendeVedtakId = "vedtakIdFraVedtak",
            ident = "12345678901",
            fom = LocalDate.of(2024, 1, 1),
            tom = LocalDate.of(2024, 3, 1),
            antallBarn = 2,
            brukerNavkontor = "0219",
            vedtakstidspunkt = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
            saksbehandler = "saksbehandler",
            beslutter = "beslutter",
            utbetalinger = listOf(
                UtbetalingDag(dato = 1.januar(), tiltaktype = ENKELTAMO, status = FullUtbetaling),
                UtbetalingDag(dato = 2.januar(), tiltaktype = ENKELTAMO, status = DelvisUtbetaling),
                UtbetalingDag(dato = 3.januar(), tiltaktype = ENKELTAMO, status = DelvisUtbetaling),
                UtbetalingDag(dato = 4.januar(), tiltaktype = ENKELTAMO, status = FullUtbetaling),
                UtbetalingDag(dato = 5.januar(), tiltaktype = ENKELTAMO, status = FullUtbetaling),
                UtbetalingDag(dato = 6.januar(), tiltaktype = GRUPPEAMO, status = FullUtbetaling),
                UtbetalingDag(dato = 7.januar(), tiltaktype = ENKELTAMO, status = FullUtbetaling),
                UtbetalingDag(dato = 8.januar(), tiltaktype = ENKELTAMO, status = FullUtbetaling),
                UtbetalingDag(dato = 9.januar(), tiltaktype = ENKELTAMO, status = IngenUtbetaling),
                UtbetalingDag(dato = 10.januar(), tiltaktype = ENKELTAMO, status = IngenUtbetaling),
                UtbetalingDag(dato = 11.januar(), tiltaktype = ENKELTAMO, status = FullUtbetaling),
            ),
            forrigeVedtak = null,
        )
        val dto = vedtak.toUtbetalingDto()
        dto.forEach {
            println(it)
        }

        dto shouldContainExactlyInAnyOrder listOf(
            UtbetalingDto(beløpPerDag = SATS, fraOgMedDato = 1.januar(), tilOgMedDato = 1.januar(), stønadsdata = enkeltAmoUtenBarn()),
            UtbetalingDto(beløpPerDag = REDUSERT_SATS, fraOgMedDato = 2.januar(), tilOgMedDato = 3.januar(), stønadsdata = enkeltAmoUtenBarn()),
            UtbetalingDto(beløpPerDag = SATS, fraOgMedDato = 4.januar(), tilOgMedDato = 5.januar(), stønadsdata = enkeltAmoUtenBarn()),
            UtbetalingDto(beløpPerDag = SATS, fraOgMedDato = 6.januar(), tilOgMedDato = 6.januar(), stønadsdata = gruppeAmoUtenBarn()),
            UtbetalingDto(beløpPerDag = SATS, fraOgMedDato = 7.januar(), tilOgMedDato = 8.januar(), stønadsdata = enkeltAmoUtenBarn()),
            UtbetalingDto(beløpPerDag = 0, fraOgMedDato = 9.januar(), tilOgMedDato = 10.januar(), stønadsdata = enkeltAmoUtenBarn()),
            UtbetalingDto(beløpPerDag = SATS, fraOgMedDato = 11.januar(), tilOgMedDato = 11.januar(), stønadsdata = enkeltAmoUtenBarn()),

            UtbetalingDto(beløpPerDag = BARNETILLEGG_SATS * 2, fraOgMedDato = 1.januar(), tilOgMedDato = 1.januar(), stønadsdata = enkeltAmoMedBarn()),
            UtbetalingDto(beløpPerDag = REDUSERT_SATS * 2, fraOgMedDato = 2.januar(), tilOgMedDato = 3.januar(), stønadsdata = enkeltAmoMedBarn()),
            UtbetalingDto(beløpPerDag = BARNETILLEGG_SATS * 2, fraOgMedDato = 4.januar(), tilOgMedDato = 5.januar(), stønadsdata = enkeltAmoMedBarn()),
            UtbetalingDto(beløpPerDag = BARNETILLEGG_SATS * 2, fraOgMedDato = 6.januar(), tilOgMedDato = 6.januar(), stønadsdata = gruppeAmoMedBarn()),
            UtbetalingDto(beløpPerDag = BARNETILLEGG_SATS * 2, fraOgMedDato = 7.januar(), tilOgMedDato = 8.januar(), stønadsdata = enkeltAmoMedBarn()),
            UtbetalingDto(beløpPerDag = 0, fraOgMedDato = 9.januar(), tilOgMedDato = 10.januar(), stønadsdata = enkeltAmoMedBarn()),
            UtbetalingDto(beløpPerDag = BARNETILLEGG_SATS * 2, fraOgMedDato = 11.januar(), tilOgMedDato = 11.januar(), stønadsdata = enkeltAmoMedBarn()),
        )
    }
}

private fun gruppeAmoUtenBarn() =
    StønadsdataTiltakspengerDto(
        stønadstype = StønadTypeTiltakspenger.GRUPPE_AMO,
        barnetillegg = false,
    )

private fun gruppeAmoMedBarn() =
    StønadsdataTiltakspengerDto(
        stønadstype = StønadTypeTiltakspenger.GRUPPE_AMO,
        barnetillegg = true,
    )

private fun enkeltAmoUtenBarn() =
    StønadsdataTiltakspengerDto(
        stønadstype = StønadTypeTiltakspenger.ENKELTPLASS_AMO,
        barnetillegg = false,
    )
private fun enkeltAmoMedBarn() =
    StønadsdataTiltakspengerDto(
        stønadstype = StønadTypeTiltakspenger.ENKELTPLASS_AMO,
        barnetillegg = true,
    )

fun Int.januar(year: Int = 2024) = LocalDate.of(year, 1, this)
