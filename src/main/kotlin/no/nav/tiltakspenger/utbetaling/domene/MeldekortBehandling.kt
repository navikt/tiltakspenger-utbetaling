package no.nav.tiltakspenger.utbetaling.domene

import java.time.LocalDate

data class UtbetalingDag(
    val deltagerStatus: DeltagerStatus,
    val dag: LocalDate,
    val status: UtbetalingStatus,
    val kvote: Int,
    val karantene: Int,
    val antDagerSyk: Int,
) {
    override fun toString(): String {
        return "${deltagerStatus.toString().padEnd(15)} $dag  \t $status \t kvote=$kvote \t karantene=$karantene \t antDagerSyk=$antDagerSyk"
    }
}

enum class UtbetalingStatus {
    IngenUtbetaling,
    FullUtbetaling,
    DelvisUtbetaling,
}

enum class DeltagerStatus {
    Deltatt,
    IkkeDeltatt,
    Syk,
    SyktBarn,
}

data class DeltattDag(
    val dag: LocalDate,
    val status: DeltagerStatus,
)

enum class Tilstand {
    Ukjent,
    Deltatt,
    IkkeDeltatt,
    Syk,
    SyktBarn,
}

data class MeldekortBehandling(
    val deltakerDager: List<DeltattDag>,
) {
    var kvote = 16
    var karantene = 0
    var antDagerSyk = 0
    var forrigeTilstand = Tilstand.Ukjent
    val utbetalingDager: List<UtbetalingDag> = mutableListOf()
    fun beregn(): List<UtbetalingDag> {
        for (dag in deltakerDager) {
            when (dag.status) {
                DeltagerStatus.Deltatt -> deltatt(dag.dag)
                DeltagerStatus.IkkeDeltatt -> ikkeDeltatt(dag.dag)
                DeltagerStatus.Syk -> syk(dag.dag)
                DeltagerStatus.SyktBarn -> syktBarn(dag.dag)
            }
        }

        return utbetalingDager
    }

    fun deltatt(d: LocalDate) {
        if (kvote == 0) karantene++
        if (karantene > 15) {
            kvote = 16
            karantene = 0
            antDagerSyk = 0
        }
        utbetalingDager.addLast(UtbetalingDag(DeltagerStatus.Deltatt, d, UtbetalingStatus.FullUtbetaling, kvote, karantene, antDagerSyk))
        forrigeTilstand = Tilstand.Deltatt
    }

    fun ikkeDeltatt(d: LocalDate) {
        utbetalingDager.addLast(UtbetalingDag(DeltagerStatus.IkkeDeltatt, d, UtbetalingStatus.IngenUtbetaling, kvote, karantene, antDagerSyk))
        forrigeTilstand = Tilstand.IkkeDeltatt
    }

    fun syk(d: LocalDate) {
        antDagerSyk++
        if (kvote > 0) {
            kvote--
            if (antDagerSyk > 3) {
                utbetalingDager.addLast(UtbetalingDag(DeltagerStatus.Syk, d, UtbetalingStatus.DelvisUtbetaling, kvote, karantene, antDagerSyk))
            } else {
                utbetalingDager.addLast(UtbetalingDag(DeltagerStatus.Syk, d, UtbetalingStatus.FullUtbetaling, kvote, karantene, antDagerSyk))
            }
        } else {
            utbetalingDager.addLast(UtbetalingDag(DeltagerStatus.Syk, d, UtbetalingStatus.IngenUtbetaling, kvote, karantene, antDagerSyk))
        }
        forrigeTilstand = Tilstand.Syk
    }

    fun syktBarn(d: LocalDate) {
        // tilsvarende som syk ??
        utbetalingDager.addLast(UtbetalingDag(DeltagerStatus.SyktBarn, d, UtbetalingStatus.FullUtbetaling, kvote, karantene, antDagerSyk))
        forrigeTilstand = Tilstand.SyktBarn
    }
}
