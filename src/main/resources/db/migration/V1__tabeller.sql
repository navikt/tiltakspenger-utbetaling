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
    utløsendeId          varchar not null,
    ident                varchar not null,
    antallBarn           int not null,
    brukerNavkontor      varchar not null,
    vedtakstidspunkt     timestamp not null,
    saksbehandler        varchar not null,
    beslutter            varchar not null,
    forrigeVedtakId      varchar null references vedtak(id)
);

create table meldekortPeriode
(
    meldekortId          varchar primary key,
    vedtakId             varchar not null references vedtak(id),
    løpenr               int not null
);

create table utbetalingdag
(
    meldekortId          varchar not null references meldekortPeriode(meldekortId),
    dato                 date not null,
    status               varchar not null,
    tiltaktype           varchar not null,
    primary key (meldekortId, dato)
);