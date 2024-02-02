DO
$$
    BEGIN
        IF
            EXISTS
                (SELECT 1 from pg_roles where rolname = 'cloudsqliamuser')
        THEN
            GRANT USAGE ON SCHEMA public TO cloudsqliamuser;
            GRANT
                SELECT
                ON ALL TABLES IN SCHEMA public TO cloudsqliamuser;
            ALTER
                DEFAULT PRIVILEGES IN SCHEMA public GRANT
                SELECT
                ON TABLES TO cloudsqliamuser;
        END IF;
    END
$$;

create table rammevedtak
(
    id                   varchar primary key,
    sakId                varchar not null,
    behandlingId         varchar not null,
    personIdent          varchar not null,
    fom                  date not null,
    tom                  date not null,
    vedtakUtfall         varchar not null,
    vedtakstidspunkt     timestamp not null,
    saksbehandler        varchar not null,
    beslutter            varchar not null
);

create table utbetalingsvedtak
(
    id                   varchar primary key,
    rammevedtakId        varchar not null references rammevedtak(id),
    behandlingId         varchar not null,
    meldekortId          varchar not null,
    resultat             varchar not null,
    vedtakstidspunkt     timestamp not null,
    saksbehandler        varchar not null,
    beslutter            varchar not null
);

create table utbetalingslinje
(
    id                   varchar primary key,
    utbetalingsvedtak    varchar not null references utbetalingsvedtak(id),
    beløp                numeric not null,
    fom                  date not null,
    tom                  date not null,
    stønaddstype         varchar not null
);
