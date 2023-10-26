package no.nav.tiltakspenger.utbetaling.routes.utbetaling

data class UtbetalingDTOUt (
    val sakId: String,
    val saksreferanse: String,
    val behandlingId: String,
    val personIdent: String,
    val vedtak: Vedtak,
    val forrigeIverksetting: ForrigeIverksetting
)

data class Vedtak (
    val vedtaksType: String = "RAMMEVEDTAK",
    val vedtakstidspunkt: String,
    val resultat: Resultat,
    val saksbehandlerId: String,
    val beslutterId: String,
    val utbetalinger: List<Utbetaling>,
)

data class ForrigeIverksetting (
    val behandlingId: String
)

enum class Resultat {
    INNVILGET, AVSLAG
}

enum class Stonadstype {
    TILTAKSPENGER
}

data class Utbetaling (
    val belopPerDag: Int,
    val fraOgMedDato: String,
    val tilOgMedDato: String,
    val stonadstype: Stonadstype,
)

/*
"vedtak": {
    "vedtakstype": "RAMMEVEDTAK",
    "vedtakstidspunkt": "2023-10-25T10:03:29.980Z",
    "resultat": "INNVILGET",
    "saksbehandlerId": "1233489712",
    "beslutterId": "beh_12039k1nmn1230194",
    "utbetalinger": [
    {
        "belopPerDag": 0,
        "fraOgMedDato": "2023-10-25",
        "tilOgMedDato": "2023-10-25",
        "stonadstype": "DAGPENGER_ARBEIDSSOKER_ORDINAER",
        "ferietillegg": "ORDINAER"
    }
    ]
},
"forrigeIverksetting": {
    "behandlingId": "3fa85f64-5717-4562-b3fc-2c963f66afa6"
}*/
