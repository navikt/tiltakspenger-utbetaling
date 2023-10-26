package no.nav.tiltakspenger.utbetaling.routes.iverksett

import io.ktor.client.HttpClient
import io.ktor.server.config.ApplicationConfig
import no.nav.tiltakspenger.utbetaling.auth.ClientConfig

class IverksettCredentialsClient(
    config: ApplicationConfig,
    httpClient: HttpClient = HttpClient(),
) {
    val iverksettScope = config.property("scope.iverksett").getString()
    private val oauth2CredentialsClient = checkNotNull(ClientConfig(config, httpClient).clients["azure"])

    suspend fun getToken(): String {
        val clientCredentialsGrant = oauth2CredentialsClient.clientCredentials(iverksettScope)
        return clientCredentialsGrant.accessToken
    }
}
