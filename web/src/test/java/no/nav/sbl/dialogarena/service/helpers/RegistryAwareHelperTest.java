package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.internal.Files;
import com.github.jknack.handlebars.io.StringTemplateSource;
import com.github.jknack.handlebars.io.TemplateSource;
import no.nav.sbl.dialogarena.config.HandlebarsHelperConfig;
import no.nav.sbl.dialogarena.sendsoknad.domain.message.TekstHenter;
import no.nav.sbl.dialogarena.service.HandlebarRegistry;
import no.nav.sbl.dialogarena.soknadinnsending.business.BusinessTestConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.Miljovariabler;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.consumer.LandOgPostInfoFetcherService;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {RegistryAwareHelperTest.HandlebarsHelperTestConfig.class, HandlebarsHelperConfig.class})
public class RegistryAwareHelperTest {

    private static final Logger LOG = LoggerFactory.getLogger(RegistryAwareHelperTest.class);
    private static final String NAVN = "navn";

    @Inject
    List<RegistryAwareHelper> helpers;

    @Inject
    HandlebarRegistry registry;

    @Test
    public void listUtRegistrerteHelpers() {
        for (RegistryAwareHelper helper : helpers) {
            LOG.info("Helper: " + helper.getNavn());
        }
    }

    /*
    * Denne testen har ingen assertions, men genererer fila Handlebars-helpers.md.
    * Se beskrivelse i Handlebars-helpers.md for å få bakgrunn for testen.
    * */

    @Test
    public void skrivRegistrerteHelpersTilReadme() throws Exception {
        List<Map<String, String>> helpersListe = new ArrayList<>();

        for (RegistryAwareHelper helper : helpers) {
            HashMap<String, String> helperInformasjon = new HashMap<>();
            helperInformasjon.put(NAVN, helper.getNavn());
            helperInformasjon.put("beskrivelse", helper.getBeskrivelse());
            helperInformasjon.put("eksempel", hentEksempelfil(helper.getNavn()));
            helpersListe.add(helperInformasjon);
        }

        helpersListe.sort((o1, o2) -> o1.get(NAVN).compareTo(o2.get(NAVN)));

        Map<String, List> handlebarsObject = new HashMap<>();
        handlebarsObject.put("helpers", helpersListe);
        Handlebars handlebars = new Handlebars();
        TemplateSource utf8TemplateSource = new StringTemplateSource("Handlebars-helpers.hbs", Files.read("/readme/Handlebars-helpers.hbs"));
        String apply = handlebars.compile(utf8TemplateSource).apply(handlebarsObject);

        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream("Handlebars-helpers.md"), StandardCharsets.UTF_8);
        writer.write(apply);
        writer.close();
    }

    private String hentEksempelfil(String name) {
        URL url = this.getClass().getResource("/readme/" + name + ".hbs");
        try {
            return FileUtils.readFileToString(new File(url.toURI()), "UTF-8");
        } catch (Exception e) {
            fail("Helperen " + name + " har ingen eksempelfil under /readme. Det må finnes en hbs-fil med dette navnet her.");
        }
        return "";
    }

    @Test
    public void registryKaltMedHelper() {
        verify(registry, atLeastOnce()).registrerHelper(eq(VariabelHelper.NAVN), any(VariabelHelper.class));
        verify(registry, atLeastOnce()).registrerHelper(anyString(), any(RegistryAwareHelper.class));
    }

    @Import(BusinessTestConfig.class)
    public static class HandlebarsHelperTestConfig {

        @Bean
        public HandlebarRegistry register() {
            return mock(HandlebarRegistry.class);
        }

        @Bean
        public TekstHenter tekstHenter() {
          return mock(TekstHenter.class);
        }

        @Bean
        public LandOgPostInfoFetcherService landOgPostInfoFetcherService() {
            return mock(LandOgPostInfoFetcherService.class);
        }

        @Bean
        public Miljovariabler informasjonService() {
            return mock(Miljovariabler.class);
        }
    }
}
