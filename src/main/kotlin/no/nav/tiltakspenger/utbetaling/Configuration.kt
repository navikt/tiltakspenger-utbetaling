package no.nav.tiltakspenger.utbetaling

import com.natpryce.konfig.ConfigurationMap
import com.natpryce.konfig.ConfigurationProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.Key
import com.natpryce.konfig.intType
import com.natpryce.konfig.overriding
import com.natpryce.konfig.stringType
import no.nav.tiltakspenger.utbetaling.auth.AzureTokenProvider

enum class Profile {
    LOCAL, DEV, PROD
}

object Configuration {
    fun applicationProfile() =
        when (System.getenv("NAIS_CLUSTER_NAME") ?: System.getProperty("NAIS_CLUSTER_NAME")) {
            "dev-gcp" -> Profile.DEV
            "prod-gcp" -> Profile.PROD
            else -> Profile.LOCAL
        }

    private val defaultProperties = ConfigurationMap(
        mapOf(
            "application.httpPort" to 8080.toString(),
            "AZURE_APP_CLIENT_ID" to System.getenv("AZURE_APP_CLIENT_ID"),
            "AZURE_APP_CLIENT_SECRET" to System.getenv("AZURE_APP_CLIENT_SECRET"),
            "AZURE_APP_WELL_KNOWN_URL" to System.getenv("AZURE_APP_WELL_KNOWN_URL"),
            "AZURE_OPENID_CONFIG_ISSUER" to System.getenv("AZURE_OPENID_CONFIG_ISSUER"),
            "AZURE_OPENID_CONFIG_JWKS_URI" to System.getenv("AZURE_OPENID_CONFIG_JWKS_URI"),
            "DB_DATABASE" to System.getenv("DB_DATABASE"),
            "DB_HOST" to System.getenv("DB_HOSTS"),
            "DB_PASSWORD" to System.getenv("DB_PASSWORD"),
            "DB_PORT" to System.getenv("DB_PORT"),
            "DB_USERNAME" to System.getenv("DB_USERNAME"),
            "logback.configurationFile" to "logback.xml",
        ),
    )

    private val localProperties = ConfigurationMap(
        mapOf(
            "application.httpPort" to 8083.toString(),
            "application.profile" to Profile.LOCAL.toString(),
            "DB_DATABASE" to "utbetaling",
            "DB_HOST" to "localhost",
            "DB_PASSWORD" to "test",
            "DB_PORT" to "5430",
            "DB_USERNAME" to "postgres",
            "logback.configurationFile" to "logback.local.xml",
            "AZURE_APP_CLIENT_ID" to "tiltakspenger-utbetaling",
            "AZURE_APP_CLIENT_SECRET" to "secret",
            "AZURE_APP_WELL_KNOWN_URL" to "http://host.docker.internal:6969/azure/.well-known/openid-configuration",
            "AZURE_OPENID_CONFIG_ISSUER" to "http://host.docker.internal:6969/azure",
            "AZURE_OPENID_CONFIG_JWKS_URI" to "http://host.docker.internal:6969/azure/jwks",
            "IVERKSETT_SCOPE" to "localhost",
            "IVERKSETT_URL" to "http://localhost:8091",
        ),
    )

    private val devProperties = ConfigurationMap(
        mapOf(
            "application.profile" to Profile.DEV.toString(),
            "IVERKSETT_SCOPE" to "api://dev-gcp.teamdagpenger.tiltakspenger-iverksett/.default",
            "IVERKSETT_URL" to "http://tiltakspenger-iverksett.teamdagpenger",
        ),
    )

    private val prodProperties = ConfigurationMap(
        mapOf(
            "application.profile" to Profile.PROD.toString(),
            "IVERKSETT_SCOPE" to "api://prod-gcp.teamdagpenger.tiltakspenger-iverksett/.default",
            "IVERKSETT_URL" to "https://tiltakspenger-iverksett.teamdagpenger",
        ),
    )

    private val composeProperties = ConfigurationMap(
        mapOf(
            "logback.configurationFile" to "logback.local.xml",
            "IVERKSETT_SCOPE" to System.getenv("IVERKSETT_SCOPE"),
            "IVERKSETT_URL" to System.getenv("IVERKSETT_URL"),
        ),
    )

    private fun config() = when (System.getenv("NAIS_CLUSTER_NAME") ?: System.getProperty("NAIS_CLUSTER_NAME")) {
        "dev-gcp" ->
            ConfigurationProperties.systemProperties() overriding EnvironmentVariables overriding devProperties overriding defaultProperties

        "prod-gcp" ->
            ConfigurationProperties.systemProperties() overriding EnvironmentVariables overriding prodProperties overriding defaultProperties

        "compose" ->
            ConfigurationProperties.systemProperties() overriding EnvironmentVariables overriding composeProperties overriding defaultProperties

        else -> {
            ConfigurationProperties.systemProperties() overriding EnvironmentVariables overriding localProperties overriding defaultProperties
        }
    }

    data class ClientConfig(
        val baseUrl: String,
    )

    fun iverksettClientConfig(baseUrl: String = config()[Key("IVERKSETT_URL", stringType)]) =
        ClientConfig(baseUrl = baseUrl)

    fun oauthConfigIverksett(
        scope: String = config()[Key("IVERKSETT_SCOPE", stringType)],
        clientId: String = config()[Key("AZURE_APP_CLIENT_ID", stringType)],
        clientSecret: String = config()[Key("AZURE_APP_CLIENT_SECRET", stringType)],
        wellknownUrl: String = config()[Key("AZURE_APP_WELL_KNOWN_URL", stringType)],
    ) = AzureTokenProvider.OauthConfig(
        scope = scope,
        clientId = clientId,
        clientSecret = clientSecret,
        wellknownUrl = wellknownUrl,
    )

    fun logbackConfigurationFile() = config()[Key("logback.configurationFile", stringType)]

    fun httpPort() = config()[Key("application.httpPort", intType)]

    data class DataBaseConf(
        val database: String,
        val host: String,
        val passord: String,
        val port: Int,
        val brukernavn: String,
    )
    fun database() = DataBaseConf(
        database = config()[Key("DB_DATABASE", stringType)],
        host = config()[Key("DB_HOST", stringType)],
        passord = config()[Key("DB_PASSWORD", stringType)],
        brukernavn = config()[Key("DB_USERNAME", stringType)],
        port = config()[Key("DB_PORT", intType)],
    )
}
