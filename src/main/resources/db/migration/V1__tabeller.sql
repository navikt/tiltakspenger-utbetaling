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
    brukerNavkontor      varchar not null,
    vedtakstidspunkt     timestamp not null,
    saksbehandler        varchar not null,
    beslutter            varchar not null,
    forrigeVedtakId      varchar null references vedtak(id)
);

CREATE TABLE utfallsperiode
(
    id                  VARCHAR PRIMARY KEY,
    vedtak_id           VARCHAR                  NOT NULL REFERENCES vedtak (id),
    fom                 DATE                     NOT NULL,
    tom                 DATE                     NOT NULL,
    antall_barn         INT                      NOT NULL,
    utfall              VARCHAR                  NOT NULL
);

create table meldekortPeriode
(
    meldekortId          varchar not null,
    vedtakId             varchar not null references vedtak(id),
    løpenr               int not null,
    primary key (meldekortId, vedtakId)
);

create table utbetalingdag
(
    vedtakId             varchar not null,
    meldekortId          varchar not null,
    dato                 date not null,
    status               varchar not null,
    tiltaktype           varchar not null,
    primary key (vedtakId, dato),
    foreign key (vedtakId, meldekortId) references meldekortPeriode (vedtakId, meldekortId)
);

create table statistikk
(
    postering_id              varchar                  null,
    sakId                     varchar                  null,
    beløp                     decimal                  null,
    beløp_beskrivelse         varchar                  null,
    aarsak                    varchar                  null,
    postering_dato            date                     null,
    gyldig_fra_dato_postering date                     null,
    gyldig_til_dato_postering date                     null
);
