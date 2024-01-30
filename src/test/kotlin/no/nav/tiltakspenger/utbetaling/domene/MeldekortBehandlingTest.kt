package no.nav.tiltakspenger.utbetaling.domene

import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class MeldekortBehandlingTest {

    @Test
    fun ssdf() {
        val meldekortBehandling = MeldekortBehandling(
            deltakerDager = listOf(
                DeltattDag(LocalDate.of(2020, 1, 1), DeltagerStatus.Deltatt),
                DeltattDag(LocalDate.of(2020, 1, 2), DeltagerStatus.Syk),
                DeltattDag(LocalDate.of(2020, 1, 3), DeltagerStatus.Syk),
                DeltattDag(LocalDate.of(2020, 1, 4), DeltagerStatus.IkkeDeltatt),
                DeltattDag(LocalDate.of(2020, 1, 5), DeltagerStatus.IkkeDeltatt),
                DeltattDag(LocalDate.of(2020, 1, 6), DeltagerStatus.Syk),
                DeltattDag(LocalDate.of(2020, 1, 7), DeltagerStatus.Syk),
                DeltattDag(LocalDate.of(2020, 1, 8), DeltagerStatus.Syk),
                DeltattDag(LocalDate.of(2020, 1, 9), DeltagerStatus.Syk),
                DeltattDag(LocalDate.of(2020, 1, 10), DeltagerStatus.Syk),
                DeltattDag(LocalDate.of(2020, 1, 11), DeltagerStatus.Syk),
                DeltattDag(LocalDate.of(2020, 1, 12), DeltagerStatus.Syk),
                DeltattDag(LocalDate.of(2020, 1, 13), DeltagerStatus.Syk),
                DeltattDag(LocalDate.of(2020, 1, 14), DeltagerStatus.Syk),
                DeltattDag(LocalDate.of(2020, 1, 15), DeltagerStatus.Syk),
                DeltattDag(LocalDate.of(2020, 1, 16), DeltagerStatus.Syk),
                DeltattDag(LocalDate.of(2020, 1, 17), DeltagerStatus.Syk),
                DeltattDag(LocalDate.of(2020, 1, 18), DeltagerStatus.Syk),
                DeltattDag(LocalDate.of(2020, 1, 19), DeltagerStatus.Syk),
                DeltattDag(LocalDate.of(2020, 1, 20), DeltagerStatus.Syk),
                DeltattDag(LocalDate.of(2020, 1, 21), DeltagerStatus.Deltatt),
                DeltattDag(LocalDate.of(2020, 1, 22), DeltagerStatus.Deltatt),
                DeltattDag(LocalDate.of(2020, 1, 23), DeltagerStatus.Deltatt),
                DeltattDag(LocalDate.of(2020, 1, 24), DeltagerStatus.Deltatt),
                DeltattDag(LocalDate.of(2020, 1, 25), DeltagerStatus.Deltatt),
                DeltattDag(LocalDate.of(2020, 1, 26), DeltagerStatus.Deltatt),
                DeltattDag(LocalDate.of(2020, 1, 27), DeltagerStatus.Deltatt),
                DeltattDag(LocalDate.of(2020, 1, 28), DeltagerStatus.Deltatt),
                DeltattDag(LocalDate.of(2020, 1, 29), DeltagerStatus.Deltatt),
                DeltattDag(LocalDate.of(2020, 1, 30), DeltagerStatus.Deltatt),
                DeltattDag(LocalDate.of(2020, 1, 31), DeltagerStatus.Deltatt),
                DeltattDag(LocalDate.of(2020, 2, 1), DeltagerStatus.Deltatt),
                DeltattDag(LocalDate.of(2020, 2, 2), DeltagerStatus.Deltatt),
                DeltattDag(LocalDate.of(2020, 2, 3), DeltagerStatus.Deltatt),
                DeltattDag(LocalDate.of(2020, 2, 4), DeltagerStatus.Deltatt),
                DeltattDag(LocalDate.of(2020, 2, 5), DeltagerStatus.Deltatt),
                DeltattDag(LocalDate.of(2020, 2, 6), DeltagerStatus.Syk),
            ),
        )
        val utbet = meldekortBehandling.beregn()
        for (dag in utbet) {
            println(dag)
        }
    }

    @Test
    fun eksempel1() {
        val meldekortBehandling = MeldekortBehandling(
            deltakerDager = listOf(
                DeltattDag(LocalDate.of(2024, 2, 1), DeltagerStatus.Syk),
                DeltattDag(LocalDate.of(2024, 2, 2), DeltagerStatus.Deltatt),
                DeltattDag(LocalDate.of(2024, 2, 3), DeltagerStatus.IkkeDeltatt),
                DeltattDag(LocalDate.of(2024, 2, 4), DeltagerStatus.IkkeDeltatt),
                DeltattDag(LocalDate.of(2024, 2, 5), DeltagerStatus.Deltatt),
                DeltattDag(LocalDate.of(2024, 2, 6), DeltagerStatus.Deltatt),
                DeltattDag(LocalDate.of(2024, 2, 7), DeltagerStatus.Deltatt),
                DeltattDag(LocalDate.of(2024, 2, 8), DeltagerStatus.Syk),
                DeltattDag(LocalDate.of(2024, 2, 9), DeltagerStatus.Deltatt),
                DeltattDag(LocalDate.of(2024, 2, 10), DeltagerStatus.IkkeDeltatt),
                DeltattDag(LocalDate.of(2024, 2, 11), DeltagerStatus.IkkeDeltatt),
                DeltattDag(LocalDate.of(2024, 2, 12), DeltagerStatus.Deltatt),
                DeltattDag(LocalDate.of(2024, 2, 13), DeltagerStatus.Deltatt),
                DeltattDag(LocalDate.of(2024, 2, 14), DeltagerStatus.Syk),
                DeltattDag(LocalDate.of(2024, 2, 15), DeltagerStatus.Syk),
                DeltattDag(LocalDate.of(2024, 2, 16), DeltagerStatus.Deltatt),
                DeltattDag(LocalDate.of(2024, 2, 17), DeltagerStatus.IkkeDeltatt),
                DeltattDag(LocalDate.of(2024, 2, 18), DeltagerStatus.IkkeDeltatt),
                DeltattDag(LocalDate.of(2024, 2, 19), DeltagerStatus.Deltatt),
                DeltattDag(LocalDate.of(2024, 2, 20), DeltagerStatus.Deltatt),
                DeltattDag(LocalDate.of(2024, 2, 21), DeltagerStatus.Deltatt),
                DeltattDag(LocalDate.of(2024, 2, 22), DeltagerStatus.Deltatt),
                DeltattDag(LocalDate.of(2024, 2, 23), DeltagerStatus.Deltatt),
                DeltattDag(LocalDate.of(2024, 2, 24), DeltagerStatus.IkkeDeltatt),
                DeltattDag(LocalDate.of(2024, 2, 25), DeltagerStatus.IkkeDeltatt),
                DeltattDag(LocalDate.of(2024, 2, 26), DeltagerStatus.Deltatt),
                DeltattDag(LocalDate.of(2024, 2, 27), DeltagerStatus.Deltatt),
                DeltattDag(LocalDate.of(2024, 2, 28), DeltagerStatus.Deltatt),
                DeltattDag(LocalDate.of(2024, 2, 29), DeltagerStatus.Deltatt),
                DeltattDag(LocalDate.of(2024, 3, 1), DeltagerStatus.Deltatt),
                DeltattDag(LocalDate.of(2024, 3, 2), DeltagerStatus.IkkeDeltatt),
                DeltattDag(LocalDate.of(2024, 3, 3), DeltagerStatus.IkkeDeltatt),
                DeltattDag(LocalDate.of(2024, 3, 4), DeltagerStatus.Syk),
            ),
        )
        val utbet = meldekortBehandling.beregn()
        for (dag in utbet) {
            println(dag)
        }
    }
}
