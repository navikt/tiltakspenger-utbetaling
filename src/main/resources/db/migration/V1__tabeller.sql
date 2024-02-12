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

create table vedtak
(
    id                   varchar primary key,
    sakId                varchar not null,
    gjeldendeVedtakId    varchar not null,
    ident                varchar not null,
    fom                  date not null,
    tom                  date not null,
    antallBarn           int not null,
    brukerNavkontor      varchar not null,
    vedtakstidspunkt     timestamp not null,
    saksbehandler        varchar not null,
    beslutter            varchar not null,
    forrigeVedtakId      varchar null references vedtak(id)
);

create table utbetalingsperioder
(
    id                   varchar primary key,
    vedtakId             varchar not null references vedtak(id),
    meldekortId          varchar not null,
    beløp                numeric not null,
    utfall               varchar not null,
    fom                  date not null,
    tom                  date not null,
    tiltakskode          varchar not null
);