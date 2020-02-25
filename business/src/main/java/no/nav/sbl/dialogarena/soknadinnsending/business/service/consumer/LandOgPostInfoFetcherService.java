package no.nav.sbl.dialogarena.soknadinnsending.business.service.consumer;

import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.sendsoknad.domain.dto.Land;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.LandService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;

@Component
public class LandOgPostInfoFetcherService {

    @Inject
    private LandService landService;
    @Inject
    private Kodeverk kodeverk;

    public List<Land> hentLand(String filter) {
        return landService.hentLand(filter);
    }

    public String getLandnavn(String landkode) {
        return landService.getLandnavn(landkode);
    }

    public String getPoststed(String postnummer) {
        return kodeverk.getPoststed(postnummer);
    }
}
