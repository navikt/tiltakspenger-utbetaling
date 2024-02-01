package no.nav.tiltakspenger.utbetaling.db

import org.testcontainers.containers.PostgreSQLContainer

object PostgresTestcontainer : PostgreSQLContainer<PostgresTestcontainer>("postgres:14.10") {

    private const val DB_USERNAME_KEY = "DB_USERNAME"
    private const val DB_PASSWORD_KEY = "DB_PASSWORD"
    private const val DB_DATABASE_KEY = "DB_DATABASE"
    private const val DB_HOST_KEY = "DB_HOST"
    private const val DB_PORT_KEY = "DB_PORT"

    override fun start() {
        super.start()
        System.setProperty(DB_HOST_KEY, host)
        System.setProperty(DB_PORT_KEY, getMappedPort(POSTGRESQL_PORT).toString())
        System.setProperty(DB_DATABASE_KEY, databaseName)
        System.setProperty(DB_USERNAME_KEY, username)
        System.setProperty(DB_PASSWORD_KEY, password)
    }

    override fun stop() {
        System.clearProperty(DB_HOST_KEY)
        System.clearProperty(DB_PORT_KEY)
        System.clearProperty(DB_DATABASE_KEY)
        System.clearProperty(DB_USERNAME_KEY)
        System.clearProperty(DB_PASSWORD_KEY)
    }
}
