package no.nav.tiltakspenger.utbetaling.service

import no.nav.tiltakspenger.utbetaling.client.iverksett.IverksettKlient.Response
import no.nav.tiltakspenger.utbetaling.domene.Utbetaling
import no.nav.tiltakspenger.utbetaling.domene.Vedtak

interface UtbetalingService {
    suspend fun mottaRammevedtakOgSendTilIverksett(vedtak: Vedtak): Response

    suspend fun mottaUtbetalingOgSendTilIverksett(utbetaling: Utbetaling): Response
}
