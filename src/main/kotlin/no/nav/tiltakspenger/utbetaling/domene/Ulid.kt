package no.nav.tiltakspenger.utbetaling.domene

import com.github.guepardoapps.kulid.ULID

interface Ulid : Comparable<Ulid> {

    fun prefixPart(): String
    fun ulidPart(): String
    override fun toString(): String
}

data class UlidBase(private val stringValue: String) : Ulid {

    companion object {
        fun random(prefix: String): UlidBase {
            require(prefix.isNotEmpty()) { "Prefiks er tom" }
            return UlidBase("${prefix}_${ULID.random()}")
        }

        fun fromDb(stringValue: String) = UlidBase(stringValue)
    }

    init {
        require(stringValue.contains("_")) { "Ikke gyldig Id, skal bestå av to deler skilt med _" }
        require(stringValue.split("_").size == 2) { "Ikke gyldig Id, skal bestå av prefiks + ulid" }
        require(stringValue.split("_").first().isNotEmpty()) { "Ikke gyldig Id, prefiks er tom" }
        require(ULID.isValid(stringValue.split("_").last())) { "Ikke gyldig Id, ulid er ugyldig" }
    }

    override fun prefixPart(): String = stringValue.split("_").first()
    override fun ulidPart(): String = ULID.fromString(stringValue.split("_").last())
    override fun toString() = stringValue
    override fun compareTo(other: Ulid): Int {
        // todo be noen voksne om å skrive denne litt finere
        val o = other.toString()
        val me = this.toString()
        return when {
            me == o -> 0
            me > o -> 1
            else -> -1
        }
    }
}

data class RammevedtakId private constructor(private val ulid: UlidBase) : Ulid by ulid {
    companion object {
        private const val PREFIX = "ins"
        fun random() = RammevedtakId(ulid = UlidBase("${PREFIX}_${ULID.random()}"))

        fun fromDb(stringValue: String) = RammevedtakId(ulid = UlidBase(stringValue))
    }
}

data class SakId private constructor(private val ulid: UlidBase) : Ulid by ulid {
    companion object {
        private const val PREFIX = "sak"
        fun random() = SakId(ulid = UlidBase("${PREFIX}_${ULID.random()}"))

        fun fromDb(stringValue: String) = SakId(ulid = UlidBase(stringValue))
    }
}

data class BehandlingId private constructor(private val ulid: UlidBase) : Ulid by ulid {
    companion object {
        private const val PREFIX = "beh"
        fun random() = BehandlingId(ulid = UlidBase("${PREFIX}_${ULID.random()}"))

        fun fromDb(stringValue: String) = BehandlingId(ulid = UlidBase(stringValue))
    }
}
