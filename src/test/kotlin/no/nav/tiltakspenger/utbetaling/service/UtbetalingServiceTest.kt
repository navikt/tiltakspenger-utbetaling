package no.nav.tiltakspenger.utbetaling.service

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.libs.common.VedtakId
import no.nav.tiltakspenger.utbetaling.domene.SakId
import no.nav.tiltakspenger.utbetaling.domene.Satser
import no.nav.tiltakspenger.utbetaling.domene.TiltakType
import no.nav.tiltakspenger.utbetaling.domene.UtbetalingDag
import no.nav.tiltakspenger.utbetaling.domene.UtbetalingDagStatus
import no.nav.tiltakspenger.utbetaling.domene.UtfallForPeriode
import no.nav.tiltakspenger.utbetaling.domene.Utfallsperiode
import no.nav.tiltakspenger.utbetaling.domene.Vedtak
import no.nav.utsjekk.kontrakter.felles.Satstype
import no.nav.utsjekk.kontrakter.felles.StønadTypeTiltakspenger
import no.nav.utsjekk.kontrakter.iverksett.StønadsdataTiltakspengerV2Dto
import no.nav.utsjekk.kontrakter.iverksett.UtbetalingV2Dto
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

class UtbetalingServiceTest {

    val sats = Satser.sats(1.januar())

    @Test
    fun `map rammevedtak`() {
        val vedtakId = VedtakId.random()
        val sakId = SakId("SakId")
        val vedtak = Vedtak(
            id = vedtakId,
            sakId = sakId,
            utløsendeId = "vedtakIdFraVedtak",
            ident = "12828098533",
            brukerNavkontor = "0219",
            vedtakstidspunkt = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
            saksbehandler = "saksbehandler",
            beslutter = "beslutter",
            utbetalinger = emptyList(),
            utfallsperioder = emptyList(),
            forrigeVedtak = null,
        )
        val dto = mapIverksettDTO(vedtak)
        dto.sakId shouldBe "SakId"
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
            sakId = SakId("SakId"),
            utløsendeId = "vedtakIdFraVedtak",
            ident = "12828098533",
            brukerNavkontor = "0219",
            vedtakstidspunkt = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
            saksbehandler = "saksbehandler",
            beslutter = "beslutter",
            utbetalinger = listOf(
                UtbetalingDag(dato = 1.januar(), tiltaktype = TiltakType.ENKELAMO, status = UtbetalingDagStatus.FullUtbetaling, meldekortId = meldekortId, løpenr = løpenr),
                UtbetalingDag(dato = 2.januar(), tiltaktype = TiltakType.ENKELAMO, status = UtbetalingDagStatus.DelvisUtbetaling, meldekortId = meldekortId, løpenr = løpenr),
                UtbetalingDag(dato = 3.januar(), tiltaktype = TiltakType.ENKELAMO, status = UtbetalingDagStatus.DelvisUtbetaling, meldekortId = meldekortId, løpenr = løpenr),
                UtbetalingDag(dato = 4.januar(), tiltaktype = TiltakType.ENKELAMO, status = UtbetalingDagStatus.FullUtbetaling, meldekortId = meldekortId, løpenr = løpenr),
                UtbetalingDag(dato = 5.januar(), tiltaktype = TiltakType.ENKELAMO, status = UtbetalingDagStatus.FullUtbetaling, meldekortId = meldekortId, løpenr = løpenr),
                UtbetalingDag(dato = 6.januar(), tiltaktype = TiltakType.GRUPPEAMO, status = UtbetalingDagStatus.FullUtbetaling, meldekortId = meldekortId, løpenr = løpenr),
                UtbetalingDag(dato = 7.januar(), tiltaktype = TiltakType.ENKELAMO, status = UtbetalingDagStatus.FullUtbetaling, meldekortId = meldekortId, løpenr = løpenr),
                UtbetalingDag(dato = 8.januar(), tiltaktype = TiltakType.ENKELAMO, status = UtbetalingDagStatus.FullUtbetaling, meldekortId = meldekortId, løpenr = løpenr),
                UtbetalingDag(dato = 9.januar(), tiltaktype = TiltakType.ENKELAMO, status = UtbetalingDagStatus.IngenUtbetaling, meldekortId = meldekortId, løpenr = løpenr),
                UtbetalingDag(dato = 10.januar(), tiltaktype = TiltakType.ENKELAMO, status = UtbetalingDagStatus.IngenUtbetaling, meldekortId = meldekortId, løpenr = løpenr),
                UtbetalingDag(dato = 11.januar(), tiltaktype = TiltakType.ENKELAMO, status = UtbetalingDagStatus.FullUtbetaling, meldekortId = meldekortId, løpenr = løpenr),
            ),
            utfallsperioder = listOf(
                Utfallsperiode(
                    fom = 1.januar(),
                    tom = 11.januar(),
                    antallBarn = 0,
                    utfall = UtfallForPeriode.GIR_RETT_TILTAKSPENGER,
                ),
            ),
            forrigeVedtak = null,
        )
        val dto = mapIverksettDTO(vedtak)
        dto.vedtak.utbetalinger.forEach {
            println(it)
        }

        dto.vedtak.utbetalinger shouldContainExactlyInAnyOrder listOf(
            UtbetalingV2Dto(beløp = sats.sats.toUInt(), satstype = Satstype.DAGLIG, fraOgMedDato = 1.januar(), tilOgMedDato = 1.januar(), stønadsdata = enkeltAmoUtenBarn()),
            UtbetalingV2Dto(beløp = sats.satsDelvis.toUInt(), satstype = Satstype.DAGLIG, fraOgMedDato = 2.januar(), tilOgMedDato = 3.januar(), stønadsdata = enkeltAmoUtenBarn()),
            UtbetalingV2Dto(beløp = sats.sats.toUInt(), satstype = Satstype.DAGLIG, fraOgMedDato = 4.januar(), tilOgMedDato = 5.januar(), stønadsdata = enkeltAmoUtenBarn()),
            UtbetalingV2Dto(beløp = sats.sats.toUInt(), satstype = Satstype.DAGLIG, fraOgMedDato = 6.januar(), tilOgMedDato = 6.januar(), stønadsdata = gruppeAmoUtenBarn()),
            UtbetalingV2Dto(beløp = sats.sats.toUInt(), satstype = Satstype.DAGLIG, fraOgMedDato = 7.januar(), tilOgMedDato = 8.januar(), stønadsdata = enkeltAmoUtenBarn()),
            UtbetalingV2Dto(beløp = sats.sats.toUInt(), satstype = Satstype.DAGLIG, fraOgMedDato = 11.januar(), tilOgMedDato = 11.januar(), stønadsdata = enkeltAmoUtenBarn()),
        )
    }

    @Test
    fun `sjekk slå sammen perioder med 2 barn`() {
        val vedtakId = VedtakId.random()
        val meldekortId = UUID.randomUUID()
        val løpenr = 1
        val vedtak = Vedtak(
            id = vedtakId,
            sakId = SakId("SakId"),
            utløsendeId = "vedtakIdFraVedtak",
            ident = "12828098533",
            brukerNavkontor = "0219",
            vedtakstidspunkt = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
            saksbehandler = "saksbehandler",
            beslutter = "beslutter",
            utbetalinger = listOf(
                UtbetalingDag(dato = 1.januar(), tiltaktype = TiltakType.ENKELAMO, status = UtbetalingDagStatus.FullUtbetaling, meldekortId = meldekortId, løpenr = løpenr),
                UtbetalingDag(dato = 2.januar(), tiltaktype = TiltakType.ENKELAMO, status = UtbetalingDagStatus.DelvisUtbetaling, meldekortId = meldekortId, løpenr = løpenr),
                UtbetalingDag(dato = 3.januar(), tiltaktype = TiltakType.ENKELAMO, status = UtbetalingDagStatus.DelvisUtbetaling, meldekortId = meldekortId, løpenr = løpenr),
                UtbetalingDag(dato = 4.januar(), tiltaktype = TiltakType.ENKELAMO, status = UtbetalingDagStatus.FullUtbetaling, meldekortId = meldekortId, løpenr = løpenr),
                UtbetalingDag(dato = 5.januar(), tiltaktype = TiltakType.ENKELAMO, status = UtbetalingDagStatus.FullUtbetaling, meldekortId = meldekortId, løpenr = løpenr),
                UtbetalingDag(dato = 6.januar(), tiltaktype = TiltakType.GRUPPEAMO, status = UtbetalingDagStatus.FullUtbetaling, meldekortId = meldekortId, løpenr = løpenr),
                UtbetalingDag(dato = 7.januar(), tiltaktype = TiltakType.ENKELAMO, status = UtbetalingDagStatus.FullUtbetaling, meldekortId = meldekortId, løpenr = løpenr),
                UtbetalingDag(dato = 8.januar(), tiltaktype = TiltakType.ENKELAMO, status = UtbetalingDagStatus.FullUtbetaling, meldekortId = meldekortId, løpenr = løpenr),
                UtbetalingDag(dato = 9.januar(), tiltaktype = TiltakType.ENKELAMO, status = UtbetalingDagStatus.IngenUtbetaling, meldekortId = meldekortId, løpenr = løpenr),
                UtbetalingDag(dato = 10.januar(), tiltaktype = TiltakType.ENKELAMO, status = UtbetalingDagStatus.IngenUtbetaling, meldekortId = meldekortId, løpenr = løpenr),
                UtbetalingDag(dato = 11.januar(), tiltaktype = TiltakType.ENKELAMO, status = UtbetalingDagStatus.FullUtbetaling, meldekortId = meldekortId, løpenr = løpenr),
            ),
            utfallsperioder = listOf(
                Utfallsperiode(
                    fom = 1.januar(),
                    tom = 11.januar(),
                    antallBarn = 2,
                    utfall = UtfallForPeriode.GIR_RETT_TILTAKSPENGER,
                ),
            ),
            forrigeVedtak = null,
        )
        val dto = mapIverksettDTO(vedtak)
        dto.vedtak.utbetalinger.forEach {
            println(it)
        }

        dto.vedtak.utbetalinger shouldContainExactlyInAnyOrder listOf(
            UtbetalingV2Dto(beløp = sats.sats.toUInt(), satstype = Satstype.DAGLIG, fraOgMedDato = 1.januar(), tilOgMedDato = 1.januar(), stønadsdata = enkeltAmoUtenBarn()),
            UtbetalingV2Dto(beløp = sats.satsDelvis.toUInt(), satstype = Satstype.DAGLIG, fraOgMedDato = 2.januar(), tilOgMedDato = 3.januar(), stønadsdata = enkeltAmoUtenBarn()),
            UtbetalingV2Dto(beløp = sats.sats.toUInt(), satstype = Satstype.DAGLIG, fraOgMedDato = 4.januar(), tilOgMedDato = 5.januar(), stønadsdata = enkeltAmoUtenBarn()),
            UtbetalingV2Dto(beløp = sats.sats.toUInt(), satstype = Satstype.DAGLIG, fraOgMedDato = 6.januar(), tilOgMedDato = 6.januar(), stønadsdata = gruppeAmoUtenBarn()),
            UtbetalingV2Dto(beløp = sats.sats.toUInt(), satstype = Satstype.DAGLIG, fraOgMedDato = 7.januar(), tilOgMedDato = 8.januar(), stønadsdata = enkeltAmoUtenBarn()),
            UtbetalingV2Dto(beløp = sats.sats.toUInt(), satstype = Satstype.DAGLIG, fraOgMedDato = 11.januar(), tilOgMedDato = 11.januar(), stønadsdata = enkeltAmoUtenBarn()),

            UtbetalingV2Dto(beløp = sats.satsBarnetillegg.toUInt() * 2u, satstype = Satstype.DAGLIG, fraOgMedDato = 1.januar(), tilOgMedDato = 1.januar(), stønadsdata = enkeltAmoMedBarn()),
            UtbetalingV2Dto(beløp = sats.satsBarnetilleggDelvis.toUInt() * 2u, satstype = Satstype.DAGLIG, fraOgMedDato = 2.januar(), tilOgMedDato = 3.januar(), stønadsdata = enkeltAmoMedBarn()),
            UtbetalingV2Dto(beløp = sats.satsBarnetillegg.toUInt() * 2u, satstype = Satstype.DAGLIG, fraOgMedDato = 4.januar(), tilOgMedDato = 5.januar(), stønadsdata = enkeltAmoMedBarn()),
            UtbetalingV2Dto(beløp = sats.satsBarnetillegg.toUInt() * 2u, satstype = Satstype.DAGLIG, fraOgMedDato = 6.januar(), tilOgMedDato = 6.januar(), stønadsdata = gruppeAmoMedBarn()),
            UtbetalingV2Dto(beløp = sats.satsBarnetillegg.toUInt() * 2u, satstype = Satstype.DAGLIG, fraOgMedDato = 7.januar(), tilOgMedDato = 8.januar(), stønadsdata = enkeltAmoMedBarn()),
            UtbetalingV2Dto(beløp = sats.satsBarnetillegg.toUInt() * 2u, satstype = Satstype.DAGLIG, fraOgMedDato = 11.januar(), tilOgMedDato = 11.januar(), stønadsdata = enkeltAmoMedBarn()),
        )
    }

    @Test
    fun `sjekk at perioder ikke går på tvers av meldekort`() {
        val vedtakId = VedtakId.random()
        val meldekortId = UUID.randomUUID()
        val dato = DayFactory(1.januar())

        val utbetalinger = listOf(
            UtbetalingDag(dato = dato.hent(), tiltaktype = TiltakType.ENKELAMO, status = UtbetalingDagStatus.IngenUtbetaling, meldekortId = meldekortId, løpenr = 1),
            UtbetalingDag(dato = dato.hent(), tiltaktype = TiltakType.ENKELAMO, status = UtbetalingDagStatus.FullUtbetaling, meldekortId = meldekortId, løpenr = 1),
            UtbetalingDag(dato = dato.hent(), tiltaktype = TiltakType.ENKELAMO, status = UtbetalingDagStatus.FullUtbetaling, meldekortId = meldekortId, løpenr = 1),
            UtbetalingDag(dato = dato.hent(), tiltaktype = TiltakType.ENKELAMO, status = UtbetalingDagStatus.FullUtbetaling, meldekortId = meldekortId, løpenr = 1),
            UtbetalingDag(dato = dato.hent(), tiltaktype = TiltakType.ENKELAMO, status = UtbetalingDagStatus.FullUtbetaling, meldekortId = meldekortId, løpenr = 1),
            UtbetalingDag(dato = dato.hent(), tiltaktype = TiltakType.ENKELAMO, status = UtbetalingDagStatus.IngenUtbetaling, meldekortId = meldekortId, løpenr = 1),
            UtbetalingDag(dato = dato.hent(), tiltaktype = TiltakType.ENKELAMO, status = UtbetalingDagStatus.IngenUtbetaling, meldekortId = meldekortId, løpenr = 1),
            UtbetalingDag(dato = dato.hent(), tiltaktype = TiltakType.ENKELAMO, status = UtbetalingDagStatus.IngenUtbetaling, meldekortId = meldekortId, løpenr = 1),
            UtbetalingDag(dato = dato.hent(), tiltaktype = TiltakType.ENKELAMO, status = UtbetalingDagStatus.FullUtbetaling, meldekortId = meldekortId, løpenr = 1),
            UtbetalingDag(dato = dato.hent(), tiltaktype = TiltakType.ENKELAMO, status = UtbetalingDagStatus.FullUtbetaling, meldekortId = meldekortId, løpenr = 1),
            UtbetalingDag(dato = dato.hent(), tiltaktype = TiltakType.ENKELAMO, status = UtbetalingDagStatus.FullUtbetaling, meldekortId = meldekortId, løpenr = 1),
            UtbetalingDag(dato = dato.hent(), tiltaktype = TiltakType.ENKELAMO, status = UtbetalingDagStatus.FullUtbetaling, meldekortId = meldekortId, løpenr = 1),
            UtbetalingDag(dato = dato.hent(), tiltaktype = TiltakType.ENKELAMO, status = UtbetalingDagStatus.IngenUtbetaling, meldekortId = meldekortId, løpenr = 1),
            UtbetalingDag(dato = dato.hent(), tiltaktype = TiltakType.ENKELAMO, status = UtbetalingDagStatus.IngenUtbetaling, meldekortId = meldekortId, løpenr = 1),

            UtbetalingDag(dato = dato.hent(), tiltaktype = TiltakType.ENKELAMO, status = UtbetalingDagStatus.IngenUtbetaling, meldekortId = meldekortId, løpenr = 2),
            UtbetalingDag(dato = dato.hent(), tiltaktype = TiltakType.ENKELAMO, status = UtbetalingDagStatus.FullUtbetaling, meldekortId = meldekortId, løpenr = 2),
            UtbetalingDag(dato = dato.hent(), tiltaktype = TiltakType.ENKELAMO, status = UtbetalingDagStatus.FullUtbetaling, meldekortId = meldekortId, løpenr = 2),
            UtbetalingDag(dato = dato.hent(), tiltaktype = TiltakType.ENKELAMO, status = UtbetalingDagStatus.FullUtbetaling, meldekortId = meldekortId, løpenr = 2),
            UtbetalingDag(dato = dato.hent(), tiltaktype = TiltakType.ENKELAMO, status = UtbetalingDagStatus.FullUtbetaling, meldekortId = meldekortId, løpenr = 2),
            UtbetalingDag(dato = dato.hent(), tiltaktype = TiltakType.ENKELAMO, status = UtbetalingDagStatus.IngenUtbetaling, meldekortId = meldekortId, løpenr = 2),
            UtbetalingDag(dato = dato.hent(), tiltaktype = TiltakType.ENKELAMO, status = UtbetalingDagStatus.IngenUtbetaling, meldekortId = meldekortId, løpenr = 2),
            UtbetalingDag(dato = dato.hent(), tiltaktype = TiltakType.ENKELAMO, status = UtbetalingDagStatus.IngenUtbetaling, meldekortId = meldekortId, løpenr = 2),
            UtbetalingDag(dato = dato.hent(), tiltaktype = TiltakType.ENKELAMO, status = UtbetalingDagStatus.FullUtbetaling, meldekortId = meldekortId, løpenr = 2),
            UtbetalingDag(dato = dato.hent(), tiltaktype = TiltakType.ENKELAMO, status = UtbetalingDagStatus.FullUtbetaling, meldekortId = meldekortId, løpenr = 2),
            UtbetalingDag(dato = dato.hent(), tiltaktype = TiltakType.ENKELAMO, status = UtbetalingDagStatus.FullUtbetaling, meldekortId = meldekortId, løpenr = 2),
            UtbetalingDag(dato = dato.hent(), tiltaktype = TiltakType.ENKELAMO, status = UtbetalingDagStatus.FullUtbetaling, meldekortId = meldekortId, løpenr = 2),
            UtbetalingDag(dato = dato.hent(), tiltaktype = TiltakType.ENKELAMO, status = UtbetalingDagStatus.IngenUtbetaling, meldekortId = meldekortId, løpenr = 2),
            UtbetalingDag(dato = dato.hent(), tiltaktype = TiltakType.ENKELAMO, status = UtbetalingDagStatus.IngenUtbetaling, meldekortId = meldekortId, løpenr = 2),
        )

        val vedtak = Vedtak(
            id = vedtakId,
            sakId = SakId("SakId"),
            utløsendeId = "vedtakIdFraVedtak",
            ident = "12828098533",
            brukerNavkontor = "0219",
            vedtakstidspunkt = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
            saksbehandler = "saksbehandler",
            beslutter = "beslutter",
            utbetalinger = utbetalinger,
            utfallsperioder = listOf(
                Utfallsperiode(
                    fom = utbetalinger.minBy { it.dato }.dato,
                    tom = utbetalinger.maxBy { it.dato }.dato,
                    antallBarn = 0,
                    utfall = UtfallForPeriode.GIR_RETT_TILTAKSPENGER,
                ),
            ),
            forrigeVedtak = null,
        )
        val dto = mapIverksettDTO(vedtak)
        dto.vedtak.utbetalinger.forEach {
            println(it)
        }

        dto.vedtak.utbetalinger shouldContainExactlyInAnyOrder listOf(
            UtbetalingV2Dto(beløp = sats.sats.toUInt(), satstype = Satstype.DAGLIG, fraOgMedDato = 2.januar(), tilOgMedDato = 5.januar(), stønadsdata = enkeltAmoUtenBarn()),
            UtbetalingV2Dto(beløp = sats.sats.toUInt(), satstype = Satstype.DAGLIG, fraOgMedDato = 9.januar(), tilOgMedDato = 12.januar(), stønadsdata = enkeltAmoUtenBarn()),
            UtbetalingV2Dto(beløp = sats.sats.toUInt(), satstype = Satstype.DAGLIG, fraOgMedDato = 16.januar(), tilOgMedDato = 19.januar(), stønadsdata = enkeltAmoUtenBarn()),
            UtbetalingV2Dto(beløp = sats.sats.toUInt(), satstype = Satstype.DAGLIG, fraOgMedDato = 23.januar(), tilOgMedDato = 26.januar(), stønadsdata = enkeltAmoUtenBarn()),
        )
    }
}

class DayFactory(
    var date: LocalDate,
) {
    fun hent() = date.also { date = date.plusDays(1) }
}

fun LocalDate.hent() = this.plusDays(1)

private fun gruppeAmoUtenBarn(brukersNavKontor: String = "0219") =
    StønadsdataTiltakspengerV2Dto(
        stønadstype = StønadTypeTiltakspenger.GRUPPE_AMO,
        barnetillegg = false,
        brukersNavKontor = brukersNavKontor,
    )

private fun gruppeAmoMedBarn(brukersNavKontor: String = "0219") =
    StønadsdataTiltakspengerV2Dto(
        stønadstype = StønadTypeTiltakspenger.GRUPPE_AMO,
        barnetillegg = true,
        brukersNavKontor = brukersNavKontor,
    )

private fun enkeltAmoUtenBarn(brukersNavKontor: String = "0219") =
    StønadsdataTiltakspengerV2Dto(
        stønadstype = StønadTypeTiltakspenger.ENKELTPLASS_AMO,
        barnetillegg = false,
        brukersNavKontor = brukersNavKontor,
    )
private fun enkeltAmoMedBarn(brukersNavKontor: String = "0219") =
    StønadsdataTiltakspengerV2Dto(
        stønadstype = StønadTypeTiltakspenger.ENKELTPLASS_AMO,
        barnetillegg = true,
        brukersNavKontor = brukersNavKontor,
    )

fun Int.januar(year: Int = 2024) = LocalDate.of(year, 1, this)
