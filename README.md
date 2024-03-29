## Sendsoknad
Applikasjonen er backend løsning for brukertjenesten Søknadsdialog.<br />
Søknadsdialog tilbyr funksjonalitet for utfylling av en søknad via et dynamisk skjema, samt opplasting av påkrevde og frivillig opplastede vedlegg. <br />
I tilegg er det bygget inn en ettersendingsløsning for å kunne ettersende dokumenter på en allerede innsendt søknad, innenfor de tidsrammene og reglene som er fastsatt.<br />

Etter bruker har lagt inn alle påkrevde data, lages det en pdf av innsendte data som sammen med vedleggene brukeren har lastet opp, legges i joark og håndteres av normal dokumentløp der.
* Benyttes for søknadene for AAP, Tillegstonader, tiltaksstønader, bilsøknad, aap-utland

### Funksjonell dokumentasjon
Se [Søknadsdialog](https://confluence.adeo.no/pages/viewpage.action?pageId=124944618)
#### Tekster i søknadene
* ligger samlet i domain/main/resources/tekster


## For lokal utviklling
* installer java 8
* git clone repo
* mvn clean install

### Henvendelser
Spørsmål tilknyttet kode eller prosjektet kan rettes mot:
* [team-soknad@nav.no](mailto:team-soknad@nav.no)

### For Navansatte
Interne henvendelser kan sendes via Slack i kanalen #teamsoknad

### Teknisk dokumentasjon
* kjører på JBOSS EAP 6 med 4 noder i produksjon, skal over på kubernetes basert Nais-plattform
* Java 8
* javabatcher
* tradisjonell modulær inndeling av funksjoner. (Se avhengigheter for bygg)


#### Større planlagte og pågående større jobber 
Arbeid som er planlagt og pågår eller planlagt oppstart av<br />
-[ ] Flytte [appen over til Github](https://github.com/navikt/sendsoknad)
-[ ] Ta i bruk ny arkiveringstjeneste
-[ ] Avslutte mellomlagring i henvendelse, IE mellomlagring i Sendsoknad.
-[ ] Redusere sirkulære avhengigheter mellom modulene
-[ ] Dele modulene opp i egne apper og tilby tjenester
-[ ] flytte over på nais
-[ ] Introdusere OIDC og fjerne openAm og SAML
-[ ] Erstatte gamle tjenester med nye -> forutsetter oidc støtte
-[ ] Ta i bruk ny tjeneste for arkivering og slutte å bruke henvendelse
-[x] Oppgardere spring
-[x] Fjerne interne biblioteker
-[x] Støtte bygging lokalt

#### Avhengigheter til andre systemer
* Tilgang på liste av søknadsmetadata fra [Søknadsveiviser](https://tjenester.nav.no/soknadsveiviserproxy/skjemautlisting) 
    * Har lagret listen lokalt om tjeneesten er nede. se Rutine oppgaver
* Tilgang til fagsystemtjenester skjer gjennom serviceGateway
* Tilgang til AAP tjenester for å avklare om personen har aktiv sak ++ for å søke om tillegsstønader og tiltakspenger
    * arbeidsforhold_v3
    * SakOgAktivitet_v1
    * maalgruppe_v1
-[ ] Fjerne tjenster det ikke lengre er bruk for
* Tilgang på persondatatjeneste for å hente persondata
    * person_v1
    * brukerprofil_v1
    * DigitalKontaktinformasjon_v1
- [ ] Erstatte med personopplysninger-tjeneste, forutsetter OIDC
* tilgang til ereg
    * Organisasjon_v4
-[ ] Avklare om denne brukes fortsatt
* tilgang til Henvendelse for innsending og mellomlagring av søknader
    * domene.Brukerdialog/Henvendelse_v2
    * domene.Brukerdialog:fillagerservice_v1
    * domene.Brukerdialog/SendSoknadService_v1
* tilgang til kodeverk som er brukt internt i NAV
    * Kodeverk_v2
##### Fasit & Vault
[Fasit](https://fasit.adeo.no/instances/333523) er intert vault for propperties, rettigheter for appen, brukernavn mm <br  />
[Vault](https://vault.adeo.no/ui/vault/secrets) er det nye sikre lageret for data <br />
* applicationpropperties
    * soknad.feature.toggles
    * soknad.propperties
    * soknad.ettersending.propperties (setter antall dager en kan ettersende på en søknad)
* andre appdata
    * base urler for navigasjon og tjenestekall
    * credentials
    * smtp
    * minneforbruk
    * openad
#### Rutine oppgaver
* Kopiere over endringer til init på github (avsluttes når vi har flyttet over til githubrepo med nais)
* Oppdattere backup fil for Soknadsveiviser<br />
Hent ut sanity.json ved å copy fra [soknadsveileder tjenestsen](https://tjenester.nav.no/soknadsveiviserproxy/skjemautlisting)
    * sendsoknad/consumer/src/resources/sanity.json<br />
    Backupfil hvis tjenesten er utilgjengelig.
    * sendsoknad/web/src/test/resources/sanity.json<br />
    Benyttes for tester mot annen modul for konsumering av tjenester.
    
* Slette mellomlagrede søknader ved endring av faktummodellen (egen rutine)

#### Aksessloggene
Aksesslogger til sendsoknad kan finnes i [Kibana](https://logs.adeo.no) ved å søke med:
```
+type:accesslog +referer: https\:\/\/tjenester.nav.no\/soknad<navn>*
```
evt med:
```
+type:accesslog +path:\/sendsoknad*
```

#### Tekster
Sjekk ut det aktuelle tekstprosjektet og se README der. 


