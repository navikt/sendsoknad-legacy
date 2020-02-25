package no.nav.sbl.dialogarena.rest.ressurser;

import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.OpplastingException;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static java.util.Collections.singletonList;
import static no.nav.sbl.dialogarena.rest.ressurser.VedleggRessurs.MAKS_TOTAL_FILSTORRELSE;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class VedleggRessursTest {

    private static final long VEDLEGGSID = 1;
    private static final String BEHANDLINGSID = "123";
    @Mock
    SoknadService soknadService;
    @Mock
    VedleggService vedleggService;

    @InjectMocks
    VedleggRessurs ressurs;

    @Before
    public void setup() {
        Vedlegg vedlegg = new Vedlegg();
        vedlegg.setStorrelse(MAKS_TOTAL_FILSTORRELSE + 1L);

        when(vedleggService.hentVedlegg(VEDLEGGSID, false)).thenReturn(new Vedlegg());
        when(vedleggService.hentVedleggUnderBehandling(eq(BEHANDLINGSID), anyString())).thenReturn(singletonList(vedlegg));
    }

    @Test(expected = OpplastingException.class)
    public void opplastingSkalKasteExceptionHvisVedleggeneErForStore() {
        ressurs.lastOppFiler(VEDLEGGSID, BEHANDLINGSID, Collections.emptyList());
    }
}
