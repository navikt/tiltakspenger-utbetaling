package no.nav.tiltakspenger.utbetaling.service

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import no.nav.dagpenger.kontrakter.felles.GeneriskIdSomUUID
import no.nav.dagpenger.kontrakter.felles.StønadTypeTiltakspenger
import no.nav.dagpenger.kontrakter.iverksett.StønadsdataTiltakspengerDto
import no.nav.dagpenger.kontrakter.iverksett.UtbetalingDto
import no.nav.tiltakspenger.utbetaling.domene.SakId
import no.nav.tiltakspenger.utbetaling.domene.TiltakType
import no.nav.tiltakspenger.utbetaling.domene.UtbetalingDag
import no.nav.tiltakspenger.utbetaling.domene.UtbetalingDagStatus
import no.nav.tiltakspenger.utbetaling.domene.Vedtak
import no.nav.tiltakspenger.utbetaling.domene.VedtakId
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

class UtbetalingServiceTest {
    @Test
    fun `map rammevedtak`() {
        val vedtakId = VedtakId.random()
        val sakId = SakId.random()
        val vedtak = Vedtak(
            id = vedtakId,
            sakId = sakId,
            utløsendeId = "vedtakIdFraVedtak",
            ident = "12828098533",
            antallBarn = 0,
            brukerNavkontor = "0219",
            vedtakstidspunkt = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
            saksbehandler = "saksbehandler",
            beslutter = "beslutter",
            utbetalinger = emptyList(),
            forrigeVedtak = null,
        )
        val dto = mapIverksettDTO(vedtak)
        dto.sakId shouldBe GeneriskIdSomUUID(sakId.uuid())
        dto.forrigeIverksetting shouldBe null
        dto.vedtak.utbetalinger shouldBe emptyList()
    }

    @Test
    fun `sjekk slå sammen perioder`() {
        val vedtakId = VedtakId.random()
        val meldekortId = UUID.randomUUID()
        val løpenr = 1
        val vedtak = Vedtak(
            id = vedtakId,
            sakId = SakId.random(),
            utløsendeId = "vedtakIdFraVedtak",
            ident = "12828098533",
            antallBarn = 0,
            brukerNavkontor = "0219",
            vedtakstidspunkt = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
            saksbehandler = "saksbehandler",
            beslutter = "beslutter",
            utbetalinger = listOf(
                UtbetalingDag(dato = 1.januar(), tiltaktype = TiltakType.ENKELTAMO, status = UtbetalingDagStatus.FullUtbetaling, meldekortId = meldekortId, løpenr = løpenr),
                UtbetalingDag(dato = 2.januar(), tiltaktype = TiltakType.ENKELTAMO, status = UtbetalingDagStatus.DelvisUtbetaling, meldekortId = meldekortId, løpenr = løpenr),
                UtbetalingDag(dato = 3.januar(), tiltaktype = TiltakType.ENKELTAMO, status = UtbetalingDagStatus.DelvisUtbetaling, meldekortId = meldekortId, løpenr = løpenr),
                UtbetalingDag(dato = 4.januar(), tiltaktype = TiltakType.ENKELTAMO, status = UtbetalingDagStatus.FullUtbetaling, meldekortId = meldekortId, løpenr = løpenr),
                UtbetalingDag(dato = 5.januar(), tiltaktype = TiltakType.ENKELTAMO, status = UtbetalingDagStatus.FullUtbetaling, meldekortId = meldekortId, løpenr = løpenr),
                UtbetalingDag(dato = 6.januar(), tiltaktype = TiltakType.GRUPPEAMO, status = UtbetalingDagStatus.FullUtbetaling, meldekortId = meldekortId, løpenr = løpenr),
                UtbetalingDag(dato = 7.januar(), tiltaktype = TiltakType.ENKELTAMO, status = UtbetalingDagStatus.FullUtbetaling, meldekortId = meldekortId, løpenr = løpenr),
                UtbetalingDag(dato = 8.januar(), tiltaktype = TiltakType.ENKELTAMO, status = UtbetalingDagStatus.FullUtbetaling, meldekortId = meldekortId, løpenr = løpenr),
                UtbetalingDag(dato = 9.januar(), tiltaktype = TiltakType.ENKELTAMO, status = UtbetalingDagStatus.IngenUtbetaling, meldekortId = meldekortId, løpenr = løpenr),
                UtbetalingDag(dato = 10.januar(), tiltaktype = TiltakType.ENKELTAMO, status = UtbetalingDagStatus.IngenUtbetaling, meldekortId = meldekortId, løpenr = løpenr),
                UtbetalingDag(dato = 11.januar(), tiltaktype = TiltakType.ENKELTAMO, status = UtbetalingDagStatus.FullUtbetaling, meldekortId = meldekortId, løpenr = løpenr),
            ),
            forrigeVedtak = null,
        )
        val dto = mapIverksettDTO(vedtak)
        dto.vedtak.utbetalinger.forEach {
            println(it)
        }

        dto.vedtak.utbetalinger shouldContainExactlyInAnyOrder listOf(
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
        val meldekortId = UUID.randomUUID()
        val løpenr = 1
        val vedtak = Vedtak(
            id = vedtakId,
            sakId = SakId.random(),
            utløsendeId = "vedtakIdFraVedtak",
            ident = "12828098533",
            antallBarn = 2,
            brukerNavkontor = "0219",
            vedtakstidspunkt = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
            saksbehandler = "saksbehandler",
            beslutter = "beslutter",
            utbetalinger = listOf(
                UtbetalingDag(dato = 1.januar(), tiltaktype = TiltakType.ENKELTAMO, status = UtbetalingDagStatus.FullUtbetaling, meldekortId = meldekortId, løpenr = løpenr),
                UtbetalingDag(dato = 2.januar(), tiltaktype = TiltakType.ENKELTAMO, status = UtbetalingDagStatus.DelvisUtbetaling, meldekortId = meldekortId, løpenr = løpenr),
                UtbetalingDag(dato = 3.januar(), tiltaktype = TiltakType.ENKELTAMO, status = UtbetalingDagStatus.DelvisUtbetaling, meldekortId = meldekortId, løpenr = løpenr),
                UtbetalingDag(dato = 4.januar(), tiltaktype = TiltakType.ENKELTAMO, status = UtbetalingDagStatus.FullUtbetaling, meldekortId = meldekortId, løpenr = løpenr),
                UtbetalingDag(dato = 5.januar(), tiltaktype = TiltakType.ENKELTAMO, status = UtbetalingDagStatus.FullUtbetaling, meldekortId = meldekortId, løpenr = løpenr),
                UtbetalingDag(dato = 6.januar(), tiltaktype = TiltakType.GRUPPEAMO, status = UtbetalingDagStatus.FullUtbetaling, meldekortId = meldekortId, løpenr = løpenr),
                UtbetalingDag(dato = 7.januar(), tiltaktype = TiltakType.ENKELTAMO, status = UtbetalingDagStatus.FullUtbetaling, meldekortId = meldekortId, løpenr = løpenr),
                UtbetalingDag(dato = 8.januar(), tiltaktype = TiltakType.ENKELTAMO, status = UtbetalingDagStatus.FullUtbetaling, meldekortId = meldekortId, løpenr = løpenr),
                UtbetalingDag(dato = 9.januar(), tiltaktype = TiltakType.ENKELTAMO, status = UtbetalingDagStatus.IngenUtbetaling, meldekortId = meldekortId, løpenr = løpenr),
                UtbetalingDag(dato = 10.januar(), tiltaktype = TiltakType.ENKELTAMO, status = UtbetalingDagStatus.IngenUtbetaling, meldekortId = meldekortId, løpenr = løpenr),
                UtbetalingDag(dato = 11.januar(), tiltaktype = TiltakType.ENKELTAMO, status = UtbetalingDagStatus.FullUtbetaling, meldekortId = meldekortId, løpenr = løpenr),
            ),
            forrigeVedtak = null,
        )
        val dto = mapIverksettDTO(vedtak)
        dto.vedtak.utbetalinger.forEach {
            println(it)
        }

        dto.vedtak.utbetalinger shouldContainExactlyInAnyOrder listOf(
            UtbetalingDto(beløpPerDag = SATS, fraOgMedDato = 1.januar(), tilOgMedDato = 1.januar(), stønadsdata = enkeltAmoUtenBarn()),
            UtbetalingDto(beløpPerDag = REDUSERT_SATS, fraOgMedDato = 2.januar(), tilOgMedDato = 3.januar(), stønadsdata = enkeltAmoUtenBarn()),
            UtbetalingDto(beløpPerDag = SATS, fraOgMedDato = 4.januar(), tilOgMedDato = 5.januar(), stønadsdata = enkeltAmoUtenBarn()),
            UtbetalingDto(beløpPerDag = SATS, fraOgMedDato = 6.januar(), tilOgMedDato = 6.januar(), stønadsdata = gruppeAmoUtenBarn()),
            UtbetalingDto(beløpPerDag = SATS, fraOgMedDato = 7.januar(), tilOgMedDato = 8.januar(), stønadsdata = enkeltAmoUtenBarn()),
            UtbetalingDto(beløpPerDag = 0, fraOgMedDato = 9.januar(), tilOgMedDato = 10.januar(), stønadsdata = enkeltAmoUtenBarn()),
            UtbetalingDto(beløpPerDag = SATS, fraOgMedDato = 11.januar(), tilOgMedDato = 11.januar(), stønadsdata = enkeltAmoUtenBarn()),

            UtbetalingDto(beløpPerDag = BARNETILLEGG_SATS * 2, fraOgMedDato = 1.januar(), tilOgMedDato = 1.januar(), stønadsdata = enkeltAmoMedBarn()),
            UtbetalingDto(beløpPerDag = REDUSERT_BARNETILLEGG_SATS * 2, fraOgMedDato = 2.januar(), tilOgMedDato = 3.januar(), stønadsdata = enkeltAmoMedBarn()),
            UtbetalingDto(beløpPerDag = BARNETILLEGG_SATS * 2, fraOgMedDato = 4.januar(), tilOgMedDato = 5.januar(), stønadsdata = enkeltAmoMedBarn()),
            UtbetalingDto(beløpPerDag = BARNETILLEGG_SATS * 2, fraOgMedDato = 6.januar(), tilOgMedDato = 6.januar(), stønadsdata = gruppeAmoMedBarn()),
            UtbetalingDto(beløpPerDag = BARNETILLEGG_SATS * 2, fraOgMedDato = 7.januar(), tilOgMedDato = 8.januar(), stønadsdata = enkeltAmoMedBarn()),
            UtbetalingDto(beløpPerDag = 0, fraOgMedDato = 9.januar(), tilOgMedDato = 10.januar(), stønadsdata = enkeltAmoMedBarn()),
            UtbetalingDto(beløpPerDag = BARNETILLEGG_SATS * 2, fraOgMedDato = 11.januar(), tilOgMedDato = 11.januar(), stønadsdata = enkeltAmoMedBarn()),
        )
    }

    @Test
    fun `sjekk at perioder ikke går på tvers av meldekort`() {
        val vedtakId = VedtakId.random()
        val meldekortId = UUID.randomUUID()
        val dato = DayFactory(1.januar())

        val vedtak = Vedtak(
            id = vedtakId,
            sakId = SakId.random(),
            utløsendeId = "vedtakIdFraVedtak",
            ident = "12828098533",
            antallBarn = 0,
            brukerNavkontor = "0219",
            vedtakstidspunkt = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
            saksbehandler = "saksbehandler",
            beslutter = "beslutter",
            utbetalinger = listOf(
                UtbetalingDag(dato = dato.hent(), tiltaktype = TiltakType.ENKELTAMO, status = UtbetalingDagStatus.IngenUtbetaling, meldekortId = meldekortId, løpenr = 1),
                UtbetalingDag(dato = dato.hent(), tiltaktype = TiltakType.ENKELTAMO, status = UtbetalingDagStatus.FullUtbetaling, meldekortId = meldekortId, løpenr = 1),
                UtbetalingDag(dato = dato.hent(), tiltaktype = TiltakType.ENKELTAMO, status = UtbetalingDagStatus.FullUtbetaling, meldekortId = meldekortId, løpenr = 1),
                UtbetalingDag(dato = dato.hent(), tiltaktype = TiltakType.ENKELTAMO, status = UtbetalingDagStatus.FullUtbetaling, meldekortId = meldekortId, løpenr = 1),
                UtbetalingDag(dato = dato.hent(), tiltaktype = TiltakType.ENKELTAMO, status = UtbetalingDagStatus.FullUtbetaling, meldekortId = meldekortId, løpenr = 1),
                UtbetalingDag(dato = dato.hent(), tiltaktype = TiltakType.ENKELTAMO, status = UtbetalingDagStatus.IngenUtbetaling, meldekortId = meldekortId, løpenr = 1),
                UtbetalingDag(dato = dato.hent(), tiltaktype = TiltakType.ENKELTAMO, status = UtbetalingDagStatus.IngenUtbetaling, meldekortId = meldekortId, løpenr = 1),
                UtbetalingDag(dato = dato.hent(), tiltaktype = TiltakType.ENKELTAMO, status = UtbetalingDagStatus.IngenUtbetaling, meldekortId = meldekortId, løpenr = 1),
                UtbetalingDag(dato = dato.hent(), tiltaktype = TiltakType.ENKELTAMO, status = UtbetalingDagStatus.FullUtbetaling, meldekortId = meldekortId, løpenr = 1),
                UtbetalingDag(dato = dato.hent(), tiltaktype = TiltakType.ENKELTAMO, status = UtbetalingDagStatus.FullUtbetaling, meldekortId = meldekortId, løpenr = 1),
                UtbetalingDag(dato = dato.hent(), tiltaktype = TiltakType.ENKELTAMO, status = UtbetalingDagStatus.FullUtbetaling, meldekortId = meldekortId, løpenr = 1),
                UtbetalingDag(dato = dato.hent(), tiltaktype = TiltakType.ENKELTAMO, status = UtbetalingDagStatus.FullUtbetaling, meldekortId = meldekortId, løpenr = 1),
                UtbetalingDag(dato = dato.hent(), tiltaktype = TiltakType.ENKELTAMO, status = UtbetalingDagStatus.IngenUtbetaling, meldekortId = meldekortId, løpenr = 1),
                UtbetalingDag(dato = dato.hent(), tiltaktype = TiltakType.ENKELTAMO, status = UtbetalingDagStatus.IngenUtbetaling, meldekortId = meldekortId, løpenr = 1),

                UtbetalingDag(dato = dato.hent(), tiltaktype = TiltakType.ENKELTAMO, status = UtbetalingDagStatus.IngenUtbetaling, meldekortId = meldekortId, løpenr = 2),
                UtbetalingDag(dato = dato.hent(), tiltaktype = TiltakType.ENKELTAMO, status = UtbetalingDagStatus.FullUtbetaling, meldekortId = meldekortId, løpenr = 2),
                UtbetalingDag(dato = dato.hent(), tiltaktype = TiltakType.ENKELTAMO, status = UtbetalingDagStatus.FullUtbetaling, meldekortId = meldekortId, løpenr = 2),
                UtbetalingDag(dato = dato.hent(), tiltaktype = TiltakType.ENKELTAMO, status = UtbetalingDagStatus.FullUtbetaling, meldekortId = meldekortId, løpenr = 2),
                UtbetalingDag(dato = dato.hent(), tiltaktype = TiltakType.ENKELTAMO, status = UtbetalingDagStatus.FullUtbetaling, meldekortId = meldekortId, løpenr = 2),
                UtbetalingDag(dato = dato.hent(), tiltaktype = TiltakType.ENKELTAMO, status = UtbetalingDagStatus.IngenUtbetaling, meldekortId = meldekortId, løpenr = 2),
                UtbetalingDag(dato = dato.hent(), tiltaktype = TiltakType.ENKELTAMO, status = UtbetalingDagStatus.IngenUtbetaling, meldekortId = meldekortId, løpenr = 2),
                UtbetalingDag(dato = dato.hent(), tiltaktype = TiltakType.ENKELTAMO, status = UtbetalingDagStatus.IngenUtbetaling, meldekortId = meldekortId, løpenr = 2),
                UtbetalingDag(dato = dato.hent(), tiltaktype = TiltakType.ENKELTAMO, status = UtbetalingDagStatus.FullUtbetaling, meldekortId = meldekortId, løpenr = 2),
                UtbetalingDag(dato = dato.hent(), tiltaktype = TiltakType.ENKELTAMO, status = UtbetalingDagStatus.FullUtbetaling, meldekortId = meldekortId, løpenr = 2),
                UtbetalingDag(dato = dato.hent(), tiltaktype = TiltakType.ENKELTAMO, status = UtbetalingDagStatus.FullUtbetaling, meldekortId = meldekortId, løpenr = 2),
                UtbetalingDag(dato = dato.hent(), tiltaktype = TiltakType.ENKELTAMO, status = UtbetalingDagStatus.FullUtbetaling, meldekortId = meldekortId, løpenr = 2),
                UtbetalingDag(dato = dato.hent(), tiltaktype = TiltakType.ENKELTAMO, status = UtbetalingDagStatus.IngenUtbetaling, meldekortId = meldekortId, løpenr = 2),
                UtbetalingDag(dato = dato.hent(), tiltaktype = TiltakType.ENKELTAMO, status = UtbetalingDagStatus.IngenUtbetaling, meldekortId = meldekortId, løpenr = 2),
            ),
            forrigeVedtak = null,
        )
        val dto = mapIverksettDTO(vedtak)
        dto.vedtak.utbetalinger.forEach {
            println(it)
        }

        dto.vedtak.utbetalinger shouldContainExactlyInAnyOrder listOf(
            UtbetalingDto(beløpPerDag = 0, fraOgMedDato = 1.januar(), tilOgMedDato = 1.januar(), stønadsdata = enkeltAmoUtenBarn()),
            UtbetalingDto(beløpPerDag = SATS, fraOgMedDato = 2.januar(), tilOgMedDato = 5.januar(), stønadsdata = enkeltAmoUtenBarn()),
            UtbetalingDto(beløpPerDag = 0, fraOgMedDato = 6.januar(), tilOgMedDato = 8.januar(), stønadsdata = enkeltAmoUtenBarn()),
            UtbetalingDto(beløpPerDag = SATS, fraOgMedDato = 9.januar(), tilOgMedDato = 12.januar(), stønadsdata = enkeltAmoUtenBarn()),
            UtbetalingDto(beløpPerDag = 0, fraOgMedDato = 13.januar(), tilOgMedDato = 14.januar(), stønadsdata = enkeltAmoUtenBarn()),

            UtbetalingDto(beløpPerDag = 0, fraOgMedDato = 15.januar(), tilOgMedDato = 15.januar(), stønadsdata = enkeltAmoUtenBarn()),
            UtbetalingDto(beløpPerDag = SATS, fraOgMedDato = 16.januar(), tilOgMedDato = 19.januar(), stønadsdata = enkeltAmoUtenBarn()),
            UtbetalingDto(beløpPerDag = 0, fraOgMedDato = 20.januar(), tilOgMedDato = 22.januar(), stønadsdata = enkeltAmoUtenBarn()),
            UtbetalingDto(beløpPerDag = SATS, fraOgMedDato = 23.januar(), tilOgMedDato = 26.januar(), stønadsdata = enkeltAmoUtenBarn()),
            UtbetalingDto(beløpPerDag = 0, fraOgMedDato = 27.januar(), tilOgMedDato = 28.januar(), stønadsdata = enkeltAmoUtenBarn()),
        )
    }
}

class DayFactory(
    var date: LocalDate,
) {
    fun hent() = date.also { date = date.plusDays(1) }
}

fun LocalDate.hent() = this.plusDays(1)

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
