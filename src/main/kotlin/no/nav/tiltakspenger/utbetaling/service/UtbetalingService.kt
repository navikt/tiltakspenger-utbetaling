package no.nav.tiltakspenger.utbetaling.service

import no.nav.tiltakspenger.utbetaling.client.iverksett.IverksettKlient.Response
import no.nav.tiltakspenger.utbetaling.domene.Rammevedtak

interface UtbetalingService {
    suspend fun mottaRammevedtakOgSendTilIverksett(rammevedtak: Rammevedtak): Response
}
