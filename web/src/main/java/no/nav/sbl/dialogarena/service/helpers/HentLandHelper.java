package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Options;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.consumer.LandOgPostInfoFetcherService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class HentLandHelper extends RegistryAwareHelper<String> {

    @Inject
    private LandOgPostInfoFetcherService landOgPostInfoFetcherService;

    @Override
    public String getNavn() {
        return "hentLand";
    }

    @Override
    public String getBeskrivelse() {
        return "Henter land fra Kodeverk basert på landkode.";
    }

    @Override
    public CharSequence apply(String landkode, Options options) {
        return landOgPostInfoFetcherService.getLandnavn(landkode);
    }
}
