package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.sbl.dialogarena.sendsoknad.domain.DelstegStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SoknadType;
import no.nav.sbl.dialogarena.soknadinnsending.business.SoknadDataFletterIntegrationTestContext;
import no.nav.sbl.dialogarena.soknadinnsending.business.WebSoknadConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.HendelseRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.MigrasjonHandterer;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fillager.FillagerService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.HenvendelseService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.skjemaoppslag.SkjemaOppslagService;
import no.nav.tjeneste.domene.brukerdialog.fillager.v1.meldinger.WSInnhold;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import static no.nav.sbl.dialogarena.sendsoknad.domain.HendelseType.AVBRUTT_AUTOMATISK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.joda.time.DateTime.now;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SoknadDataFletterIntegrationTestContext.class)
public class SoknadServiceIntegrasjonsTest {

    private final String BEHANDLINGSID = "EN_BEHANDLINGSID";
    private WebSoknad soknad;
    private String uuid = "uid";
    private String skjemaNummer = "";
    private long soknadId;

    private SoknadService soknadService;

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private SoknadRepository lokalDb;
    @Autowired
    private FaktaService faktaService;
    @Autowired
    private HendelseRepository hendelseRepository;
    @Autowired
    private MigrasjonHandterer migrasjonHandterer;
    @Autowired
    private VedleggService vedleggService;

    private FillagerService fillagerService;
    private HenvendelseService henvendelseService;

    @Before
    public void setup() {
        fillagerService = mock(FillagerService.class);
        SoknadMetricsService soknadMetricsService = mock(SoknadMetricsService.class);

        SkjemaOppslagService skjemaOppslagService = mock(SkjemaOppslagService.class);
        when(skjemaOppslagService.getTema(anyString())).thenReturn("TSO");
        when(skjemaOppslagService.getTittel(anyString())).thenReturn("Søknad om gravferdsstønad");

        henvendelseService = mock(HenvendelseService.class);
        when(henvendelseService.startSoknad(anyString(), anyString(), anyString(), anyString(), isA(SoknadType.class)))
                .thenReturn(BEHANDLINGSID);

        AlternativRepresentasjonService alternativRepresentasjonService = new AlternativRepresentasjonService();
        SoknadDataFletter soknadDataFletter = new SoknadDataFletter();
        soknadService = new SoknadService();
        WebSoknadConfig config = new WebSoknadConfig();

        ReflectionTestUtils.setField(soknadDataFletter, "alternativRepresentasjonService", alternativRepresentasjonService);
        ReflectionTestUtils.setField(soknadDataFletter, "skjemaOppslagService", skjemaOppslagService);
        ReflectionTestUtils.setField(soknadDataFletter, "soknadMetricsService", soknadMetricsService);
        ReflectionTestUtils.setField(soknadDataFletter, "henvendelseService", henvendelseService);
        ReflectionTestUtils.setField(soknadDataFletter, "hendelseRepository", hendelseRepository);
        ReflectionTestUtils.setField(soknadDataFletter, "applicationContext", applicationContext);
        ReflectionTestUtils.setField(soknadDataFletter, "migrasjonHandterer", migrasjonHandterer);
        ReflectionTestUtils.setField(soknadDataFletter, "fillagerService", fillagerService);
        ReflectionTestUtils.setField(soknadDataFletter, "vedleggService", vedleggService);
        ReflectionTestUtils.setField(soknadDataFletter, "faktaService", faktaService);
        ReflectionTestUtils.setField(soknadDataFletter, "lokalDb", lokalDb);
        ReflectionTestUtils.setField(soknadDataFletter, "config", config);
        ReflectionTestUtils.setField(soknadService, "soknadMetricsService", soknadMetricsService);
        ReflectionTestUtils.setField(soknadService, "henvendelseService", henvendelseService);
        ReflectionTestUtils.setField(soknadService, "soknadDataFletter", soknadDataFletter);
        ReflectionTestUtils.setField(soknadService, "fillagerService", fillagerService);
        ReflectionTestUtils.setField(soknadService, "lokalDb", lokalDb);
        ReflectionTestUtils.setField(alternativRepresentasjonService, "fillagerService", fillagerService);
        ReflectionTestUtils.setField(alternativRepresentasjonService, "config", config);
        ReflectionTestUtils.setField(config, "skjemaOppslagService", skjemaOppslagService);
        ReflectionTestUtils.setField(config, "repository", lokalDb);

        soknadDataFletter.initBolker();
    }

    @After
    public void teardown() {
        lokalDb.slettSoknad(soknad.medId(soknadId), AVBRUTT_AUTOMATISK);
    }

    @Test
    public void startSoknadHenterBehandlingsIdFraHenvendelse() {
        String behandlingsId = soknadService.startSoknad("NAV 11-12.12", "aktorId");
        soknad = soknadService.hentSoknad(behandlingsId, false, false);

        assertThat(behandlingsId).isEqualTo(BEHANDLINGSID);
    }

    @Test
    public void hentSoknadFraLokalDbReturnererPopulertSoknad() {
        skjemaNummer = "NAV 11-12.12";
        Long soknadId = opprettOgPersisterSoknad(BEHANDLINGSID, "aktor");

        WebSoknad webSoknad = soknadService.hentSoknadFraLokalDb(soknadId);

        assertThat(webSoknad.getBrukerBehandlingId()).isEqualTo(BEHANDLINGSID);
        assertThat(webSoknad.getAktoerId()).isEqualTo("aktor");
        assertThat(webSoknad.getUuid()).isEqualTo(uuid);
        assertThat(webSoknad.getDelstegStatus()).isEqualTo(DelstegStatus.OPPRETTET);
        assertThat(webSoknad.getskjemaNummer()).isEqualTo(skjemaNummer);
    }

    @Test
    public void settDelstegPersistererNyttDelstegTilDb() {
        Long soknadId = opprettOgPersisterSoknad(BEHANDLINGSID, "aktor");

        soknadService.settDelsteg(BEHANDLINGSID, DelstegStatus.SAMTYKKET);

        WebSoknad webSoknad = soknadService.hentSoknadFraLokalDb(soknadId);
        assertThat(webSoknad.getDelstegStatus()).isEqualTo(DelstegStatus.SAMTYKKET);
    }

    @Test
    public void settJournalforendeEnhetPersistererNyJournalforendeEnhetTilDb() {
        Long soknadId = opprettOgPersisterSoknad(BEHANDLINGSID, "aktor");

        soknadService.settJournalforendeEnhet(BEHANDLINGSID, "NAV UTLAND");

        WebSoknad webSoknad = soknadService.hentSoknadFraLokalDb(soknadId);
        assertThat(webSoknad.getJournalforendeEnhet()).isEqualTo("NAV UTLAND");
    }

    @Test
    public void avbrytSoknadSletterSoknadenFraLokalDb() {
        Long soknadId = opprettOgPersisterSoknad(BEHANDLINGSID, "aktor");

        soknadService.avbrytSoknad(BEHANDLINGSID);

        WebSoknad webSoknad = soknadService.hentSoknadFraLokalDb(soknadId);
        assertThat(webSoknad).isNull();
    }

    @Test
    public void avbrytSoknadSletterSoknadenFraHenvendelse() {
        opprettOgPersisterSoknad(BEHANDLINGSID, "aktor");

        soknadService.avbrytSoknad(BEHANDLINGSID);

        List<WSInnhold> filer = fillagerService.hentFiler(BEHANDLINGSID);
        assertThat(filer).isEmpty();
    }

    @Test
    public void avbrytSoknadAvbryterSoknadenIHenvendelse() {
        String behandlingsId = nyBehandlnigsId();
        opprettOgPersisterSoknad(behandlingsId, "aktor");

        soknadService.avbrytSoknad(behandlingsId);

        verify(henvendelseService).avbrytSoknad(eq(behandlingsId));
    }

    @Test
    public void sendSoknadSkalLagreEnFilTilHenvendelseHvisBilstonad() {
        skjemaNummer = "NAV 10-07.40";
        String behandlingsId = nyBehandlnigsId();
        opprettOgPersisterSoknad(behandlingsId, "aktor");

        soknadService.sendSoknad(behandlingsId, new byte[]{});

        verify(fillagerService, times(1)).lagreFil(eq(behandlingsId), anyString(), anyString(), isA(InputStream.class));
    }

    @Test
    public void sendSoknadSkalLagreToFilerTilHenvendelseHvisTilleggsstonader() {
        skjemaNummer = "NAV 11-12.12";
        String behandlingsId = nyBehandlnigsId();
        opprettOgPersisterSoknadMedData(behandlingsId, "aktor");
        lokalDb.opprettFaktum(soknadId, maalgruppeFaktum(), true);

        soknadService.sendSoknad(behandlingsId, new byte[]{});

        verify(fillagerService, times(2)).lagreFil(eq(behandlingsId), anyString(), anyString(), isA(InputStream.class));
    }


    private String nyBehandlnigsId() {
        return UUID.randomUUID().toString();
    }

    private Long opprettOgPersisterSoknad(String behId, String aktor) {
        soknad = WebSoknad.startSoknad()
                .medUuid(uuid)
                .medAktorId(aktor)
                .medBehandlingId(behId)
                .medDelstegStatus(DelstegStatus.OPPRETTET)
                .medskjemaNummer(skjemaNummer)
                .medOppretteDato(now());
        soknadId = lokalDb.opprettSoknad(soknad);
        soknad.setSoknadId(soknadId);
        return soknadId;
    }

    private Long opprettOgPersisterSoknadMedData(String behId, String aktor) {
        soknad = WebSoknad.startSoknad()
                .medUuid(uuid)
                .medAktorId(aktor)
                .medBehandlingId(behId)
                .medVersjon(0)
                .medDelstegStatus(DelstegStatus.OPPRETTET)
                .medskjemaNummer(skjemaNummer)
                .medOppretteDato(now());

        soknadId = lokalDb.opprettSoknad(soknad);
        faktaService.opprettBrukerFaktum(behId, new Faktum()
                .medKey("bostotte.aarsak")
                .medValue("fasteboutgifter"));
        faktaService.opprettBrukerFaktum(behId, new Faktum()
                .medKey("bostotte.periode")
                .medProperty("fom", "2015-07-22")
                .medProperty("tom", "2015-10-22"));
        faktaService.opprettBrukerFaktum(behId, new Faktum()
                .medKey("bostotte.kommunestotte"));
        faktaService.opprettBrukerFaktum(behId, new Faktum()
                .medKey("bostotte.adresseutgifter.aktivitetsadresse")
                .medProperty("utgift", "2000"));
        faktaService.opprettBrukerFaktum(behId, new Faktum()
                .medKey("bostotte.adresseutgifter.hjemstedsaddresse"));

        soknad.setSoknadId(soknadId);

        return soknadId;
    }

    private Faktum maalgruppeFaktum() {
        return new Faktum()
                .medType(Faktum.FaktumType.SYSTEMREGISTRERT)
                .medKey("maalgruppe")
                .medProperty("kodeverkVerdi", "ARBSOKER")
                .medProperty("fom", "2015-01-01");
    }
}
