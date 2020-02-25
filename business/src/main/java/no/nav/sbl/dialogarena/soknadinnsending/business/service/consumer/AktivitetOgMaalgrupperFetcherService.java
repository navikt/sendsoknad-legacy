package no.nav.sbl.dialogarena.soknadinnsending.business.service.consumer;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.AktivitetService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.MaalgrupperService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;

@Component
public class AktivitetOgMaalgrupperFetcherService {

    @Inject
    private AktivitetService aktivitetService;
    @Inject
    private MaalgrupperService maalgrupperService;

    public List<Faktum> hentAktiviteter(String fodselnummer) {
        return aktivitetService.hentAktiviteter(fodselnummer);
    }

    public List<Faktum> hentVedtak(String fodselsnummer) {
        return aktivitetService.hentVedtak(fodselsnummer);
    }

    public List<Faktum> hentMaalgrupper(String fodselsnummer) {
        return maalgrupperService.hentMaalgrupper(fodselsnummer);
    }
}
