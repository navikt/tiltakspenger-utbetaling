tiltakspenger-utbetaling
========================

tiltakspenger-utbetaling er en backend tjeneste som skal håndtere beregning og utbetaling til tiltakspenger.

Denne appen skal snakke med "dp-iversett" appen i utgangspunktet som igjen vil snakke med OS/UR (økonomi sin tjeneste) for å gjennomføre utbetalingen.

Appen skal innhente de dagene bruker har gått på et tiltak fra meldekortet og så sender info om utbetalingen/behandlingen til dp-iverksett. 

En del av satsningen ["Flere i arbeid – P4"](https://memu.no/artikler/stor-satsing-skal-fornye-navs-utdaterte-it-losninger-og-digitale-verktoy/)

# Komme i gang
## Forutsetninger
- [JDK](https://jdk.java.net/)
- [Kotlin](https://kotlinlang.org/)
- [Gradle](https://gradle.org/) brukes som byggeverktøy og er inkludert i oppsettet

For hvilke versjoner som brukes, [se byggefilen](build.gradle.kts)

## Bygging og denslags
For å bygge artifaktene:

```sh
./gradlew build
```

---

# Henvendelser

Spørsmål knyttet til koden eller prosjektet kan stilles som issues her på GitHub.

## For NAV-ansatte

Interne henvendelser kan sendes via Slack i kanalen #tpts-tech.
