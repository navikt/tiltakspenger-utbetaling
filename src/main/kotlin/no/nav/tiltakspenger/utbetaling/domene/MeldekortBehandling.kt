package no.nav.tiltakspenger.utbetaling.domene

import java.time.LocalDate

data class UtbetalingDag(
    val deltagerStatus: DeltagerStatus,
    val dag: LocalDate,
    val status: UtbetalingStatus,
    val kvote100: Int,
    val kvote75: Int,
    val karantene: Int,
    val iKarantene: Boolean,
) {
    override fun toString(): String {
        return "${deltagerStatus.toString().padEnd(15)} $dag  ${dag.dayOfWeek.toString().padEnd(12)} \t $status \t kvote100=$kvote100 \t kvote75=${kvote75.toString().padEnd(12)} \t karantene=$karantene \t iKarantene=$iKarantene"
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
    private var iKarantene = false
    private var kvote100 = 3
    private var kvote75 = 13
    private var karantene = 0
//    private var antDagerSidenSisteSykedagMedUtbetaling = 0
    private var forrigeTilstand = Tilstand.Ukjent
    private val utbetalingDager: List<UtbetalingDag> = mutableListOf()
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

    private fun deltatt(d: LocalDate) {
        if (iKarantene) karantene++
        if (karantene > 15) {
            resetKvote()
        }
        utbetalingDager.addLast(UtbetalingDag(DeltagerStatus.Deltatt, d, UtbetalingStatus.FullUtbetaling, kvote100, kvote75, karantene, iKarantene))
        forrigeTilstand = Tilstand.Deltatt
    }

    private fun ikkeDeltatt(d: LocalDate) {
//        if (kvote == 0) karantene++
        if (iKarantene) karantene++
        if (karantene > 15) {
//            kvote = 16
            if (iKarantene) karantene++
            karantene = 0
        }
        utbetalingDager.addLast(UtbetalingDag(DeltagerStatus.IkkeDeltatt, d, UtbetalingStatus.IngenUtbetaling, kvote100, kvote75, karantene, iKarantene))
        forrigeTilstand = Tilstand.IkkeDeltatt
    }

    private fun syk(d: LocalDate) {
        if (iKarantene) {
            utbetalingDager.addLast(UtbetalingDag(DeltagerStatus.Syk, d, UtbetalingStatus.IngenUtbetaling, kvote100, kvote75, karantene, iKarantene))
        } else {
            if (kvote75 == 13) {
                if (kvote100 > 0) kvote100-- else kvote75--
                if (kvote75 == 0) iKarantene = true
                utbetalingDager.addLast(UtbetalingDag(DeltagerStatus.Syk, d, UtbetalingStatus.FullUtbetaling, kvote100, kvote75, karantene, iKarantene))
            } else {
                if (kvote100 > 0) kvote100-- else kvote75--
                if (kvote75 == 0) iKarantene = true
                utbetalingDager.addLast(UtbetalingDag(DeltagerStatus.Syk, d, UtbetalingStatus.DelvisUtbetaling, kvote100, kvote75, karantene, iKarantene))
            }
        }



//        antDagerSyk++
//        if (kvote > 0) {
//            kvote--
//            antDagerSidenSisteSykedagMedUtbetaling = 0
//            if (antDagerSyk > 3) {
//                utbetalingDager.addLast(UtbetalingDag(DeltagerStatus.Syk, d, UtbetalingStatus.DelvisUtbetaling, kvote100, kvote75, karantene, iKarantene))
//            } else {
//                utbetalingDager.addLast(UtbetalingDag(DeltagerStatus.Syk, d, UtbetalingStatus.FullUtbetaling, kvote100, kvote75, karantene, iKarantene))
//            }
//        } else {
//            antDagerSidenSisteSykedagMedUtbetaling++
//            utbetalingDager.addLast(UtbetalingDag(DeltagerStatus.Syk, d, UtbetalingStatus.IngenUtbetaling, kvote100, kvote75, karantene, iKarantene))
//        }
        forrigeTilstand = Tilstand.Syk
    }

    private fun syktBarn(d: LocalDate) {
        // tilsvarende som syk ??
        utbetalingDager.addLast(UtbetalingDag(DeltagerStatus.SyktBarn, d, UtbetalingStatus.FullUtbetaling, kvote100, kvote75, karantene, iKarantene))
        forrigeTilstand = Tilstand.SyktBarn
    }

    private fun resetKvote() {
        kvote75 = 3
        kvote100 = 16
        iKarantene = false
        karantene = 0
    }
 }
