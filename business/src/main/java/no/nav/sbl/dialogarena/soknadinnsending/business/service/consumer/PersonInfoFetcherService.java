package no.nav.sbl.dialogarena.soknadinnsending.business.service.consumer;

import no.nav.sbl.dialogarena.soknadinnsending.consumer.personinfo.PersonInfoService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class PersonInfoFetcherService {

    @Inject
    private PersonInfoService personInfoService;

    public String hentYtelseStatus(String fnr) {
        return personInfoService.hentYtelseStatus(fnr);
    }
}
