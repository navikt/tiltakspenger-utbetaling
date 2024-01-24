package no.nav.tiltakspenger.utbetaling.domene

import java.time.LocalDateTime
import java.util.UUID

data class RammevedtakId(val id: UUID) {
    override fun toString(): String {
        return id.toString()
    }
}

data class SakId(val id: UUID) {
    override fun toString(): String {
        return id.toString()
    }
}

data class BehandlingId(val id: UUID) {
    override fun toString(): String {
        return id.toString()
    }
}

data class Rammevedtak(
    val id: RammevedtakId,
    val sakId: SakId,
    val behandlingId: BehandlingId,
    val personIdent: String,
    val iverksettingResultat: IverksettingResultat,
    val vedtakstidspunkt: LocalDateTime,
    val saksbehandler: String,
    val beslutter: String,
)

enum class IverksettingResultat {
    INNVILGET,
    AVSLÅTT,
}
