package no.nav.sbl.dialogarena.rest.ressurser;

import no.nav.metrics.aspects.Timed;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.OpplastingException;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.UgyldigOpplastingTypeException;
import no.nav.sbl.dialogarena.sikkerhet.SjekkTilgangTilSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.pdfutility.PdfUtilities;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg.Status.UnderBehandling;
import static no.nav.sbl.dialogarena.sikkerhet.SjekkTilgangTilSoknad.Type.Vedlegg;
import static org.slf4j.LoggerFactory.getLogger;

@Controller
@Path("/vedlegg/{vedleggId}")
@Produces(APPLICATION_JSON)
@Timed
public class VedleggRessurs {

    private static final Logger logger = getLogger(VedleggRessurs.class);

    protected static final Integer MAKS_TOTAL_FILSTORRELSE = 1024 * 1024 * 10;

    @Inject
    private VedleggService vedleggService;
    @Inject
    private SoknadService soknadService;

    @GET
    @SjekkTilgangTilSoknad(type = Vedlegg)
    public Vedlegg hentVedlegg(@PathParam("vedleggId") final Long vedleggId) {
        return vedleggService.hentVedlegg(vedleggId, false);
    }

    @PUT
    @SjekkTilgangTilSoknad(type = Vedlegg)
    public void lagreVedlegg(@PathParam("vedleggId") final Long vedleggId, Vedlegg vedlegg) {
        Map<String, Long> tidsbruk = new HashMap<>();
        tidsbruk.put("Start", System.currentTimeMillis());
        vedleggService.lagreVedlegg(vedleggId, vedlegg);
        tidsbruk.put("Slutt", System.currentTimeMillis());
        loggStatistikk(tidsbruk, "TIDSBRUK:lagreVedlegg, id=" + vedleggId);
    }

    @DELETE
    @SjekkTilgangTilSoknad(type = Vedlegg)
    public void slettVedlegg(@PathParam("vedleggId") final Long vedleggId) {
        vedleggService.slettVedlegg(vedleggId);
    }

    @GET
    @Path("/fil")
    @SjekkTilgangTilSoknad(type = Vedlegg)
    public List<Vedlegg> hentVedleggUnderBehandling(@PathParam("vedleggId") final Long vedleggId, @QueryParam("behandlingsId") final String behandlingsId) {
        Map<String, Long> tidsbruk = new HashMap<>();
        tidsbruk.put("Start", System.currentTimeMillis());
        Vedlegg forventning = vedleggService.hentVedlegg(vedleggId, false);
        List<Vedlegg> vedleggListe = vedleggService.hentVedleggUnderBehandling(behandlingsId, forventning.getFillagerReferanse());
        tidsbruk.put("Slutt", System.currentTimeMillis());
        loggStatistikk(tidsbruk, "TIDSBRUK:hentVedleggUnderBehandling, id=" + vedleggId);
        return vedleggListe;
    }

    @GET
    @Path("/fil.png")
    @Produces("image/png")
    @SjekkTilgangTilSoknad(type = Vedlegg)
    public byte[] lagForhandsvisningForVedlegg(@PathParam("vedleggId") final Long vedleggId, @QueryParam("side") final int side) {
        logger.info("LagForhandsvisningForVedlegg {} og side {}", vedleggId, side);
        Map<String, Long> tidsbruk = new HashMap<>();
        tidsbruk.put("Start", System.currentTimeMillis());
        byte[] sideData = vedleggService.lagForhandsvisning(vedleggId, side);
        tidsbruk.put("Slutt", System.currentTimeMillis());
        loggStatistikk(tidsbruk, "TIDSBRUK:lagForhandsvisningForVedlegg, id=" + vedleggId + ", side=" + side + ", størrelse=" +sideData.length);
        return sideData;
    }

    @POST
    @Path("/fil")
    @Consumes(MULTIPART_FORM_DATA)
    @SjekkTilgangTilSoknad(type = Vedlegg)
    public List<Vedlegg> lastOppFiler(@PathParam("vedleggId") final Long vedleggId, @QueryParam("behandlingsId") String behandlingsId, @FormDataParam("files[]") final List<FormDataBodyPart> files) {
        Map<String, Long> tidsbruk = new HashMap<>();
        tidsbruk.put("Start", System.currentTimeMillis());
        WebSoknad soknad = soknadService.hentSoknad(behandlingsId, true, false);
        tidsbruk.put("HentetSoknad", System.currentTimeMillis());
        Vedlegg forventning = vedleggService.hentVedlegg(vedleggId, false);
        tidsbruk.put("HentetVedlegg", System.currentTimeMillis());

        long totalStorrelse = estimerTotalVedleggsStorrelse(behandlingsId, files, forventning);
        if (totalStorrelse > MAKS_TOTAL_FILSTORRELSE) {
            logger.info("Totalstørrelse="+ totalStorrelse + " for vedleggId="+vedleggId + " forsøkt lastet opp");
            tidsbruk.put("SjekketTotalFilstorrelse", System.currentTimeMillis());
            throw new OpplastingException("Kunne ikke lagre fil fordi total filstørrelse er for stor", null, "vedlegg.opplasting.feil.forStor");
        }
        tidsbruk.put("SjekketTotalFilstorrelse", System.currentTimeMillis());

        List<Vedlegg> res = new ArrayList<>();
        for (FormDataBodyPart file : files) {
            Map<String, Long> tidsbruk2 = new HashMap<>();
            tidsbruk2.put("Start", System.currentTimeMillis());
            byte[] in = getByteArray(file);
            boolean erPdfa;
            boolean varImage;
            // Sjekk og konvertert til PDF dersom image
            if (PdfUtilities.isImage(in)) {
                in = PdfUtilities.createPDFFromImage(in);
                tidsbruk2.put("KonvertertFraImage", System.currentTimeMillis());
                erPdfa = true;
                varImage = true;
            } else if (PdfUtilities.isPDF(in)) {
                // Kontroller at PDF er lovlig, dvs. ikke encrypted og passordbeskyttet
                try {
                    PdfUtilities.erGyldig(in);
                    tidsbruk2.put("KontrollertLesbarPDF", System.currentTimeMillis());
                    erPdfa = PdfUtilities.erPDFA(in);
                    tidsbruk2.put("TestOmPDFA", System.currentTimeMillis());
                    varImage = false;
                } catch (Exception e) {
                    if (e.getMessage().contains("PDF er signert")) {
                        throw new UgyldigOpplastingTypeException(
                                e.getMessage(), null,
                                "opplasting.feilmelding.pdf.signert");
                    } else {
                        throw new UgyldigOpplastingTypeException(
                                e.getMessage(), null,
                                "opplasting.feilmelding.pdf.kryptert");
                    }

                }
            } else {
                throw new UgyldigOpplastingTypeException(
                        "Ugyldig filtype for opplasting", null,
                        "opplasting.feilmelding.feiltype");
            }

            if (file.getContentDisposition().getSize() != in.length)
                logger.warn("Lengths differ! ContentDisposition size: {}, byte length: {}", file.getContentDisposition().getSize(), in.length);
            else
                logger.info("Lengths do not differ. ContentDisposition size: {}", file.getContentDisposition().getSize());
            Vedlegg vedlegg = new Vedlegg()
                    .medVedleggId(null)
                    .medSoknadId(soknad.getSoknadId())
                    .medFaktumId(forventning.getFaktumId())
                    .medSkjemaNummer(forventning.getSkjemaNummer())
                    .medSkjemanummerTillegg(forventning.getSkjemanummerTillegg())
                    .medNavn(forventning.getNavn())
                    .medStorrelse(file.getContentDisposition().getSize())
                    .medFillagerReferanse(forventning.getFillagerReferanse())
                    .medData(in) // invariant: alltid PDF
                    .medOpprettetDato(forventning.getOpprettetDato())
                    .medInnsendingsvalg(UnderBehandling);

            vedlegg.setFilnavn(settFilensFiltype(vedlegg, erPdfa));
            vedlegg.setAntallSider(PdfUtilities.finnAntallSider(in));
            List<Long> ids = vedleggService.lagreVedlegg(vedlegg, in);

            if (ids.size() != 1)
                logger.warn("Unexpected number of ids returned! Size: {}", ids.size());

            for (Long id : ids) {
                res.add(vedleggService.hentVedlegg(id, false));
            }
            tidsbruk2.put("LagretPDF", System.currentTimeMillis());
            tidsbruk.put("Slutt", System.currentTimeMillis());
            loggStatistikk(tidsbruk2, "TIDSBRUK:lastOppFil, FILFORMAT=" + (varImage ? "Image" : (erPdfa ? "PDFA" : "PDF"))
                    + " id=" + ids.get(0)
                    + " for behandlingsid=" + behandlingsId + " og vedleggsid=" + vedleggId);
        }
        tidsbruk.put("Slutt", System.currentTimeMillis());
        loggStatistikk(tidsbruk, "TIDSBRUK:lastOppFiler for behandlingsid=" + behandlingsId + " og vedleggsid=" + vedleggId);
        return res;
    }

    private void loggStatistikk(Map<String, Long> tidsbruk, String context) {
        if (tidsbruk.get("Slutt") != null && tidsbruk.get("Start") != null) {
            logger.info("{} tidsbruk : {}", context, (tidsbruk.get("Slutt") - tidsbruk.get("Start")));
        }
        tidsbruk.keySet().stream()
                .filter(key -> !key.equalsIgnoreCase("Start") && !key.equalsIgnoreCase("Slutt"))
                .forEach(key -> logger.info("{}: {}", key, tidsbruk.get(key)));
    }

    private String settFilensFiltype(Vedlegg vedlegg, boolean erPdfa) {
        String filnavn = vedlegg.lagFilNavn();
        filnavn = StringUtils.removeEnd(filnavn, ".pdf");
        filnavn = StringUtils.removeEnd(filnavn, ".pdfa");
        return filnavn + (erPdfa ? ".pdfa" : ".pdf");
    }

    private byte[] getByteArray(FormDataBodyPart file) {
        try {
            return IOUtils.toByteArray(file.getValueAs(InputStream.class));
        } catch (IOException e) {
            throw new OpplastingException("Kunne ikke lagre fil", e, "vedlegg.opplasting.feil.generell");
        }
    }

    private Long estimerTotalVedleggsStorrelse(String behandlingsId, List<FormDataBodyPart> files, Vedlegg forventning) {
        Long totalStorrelse = 0L;
        List<Vedlegg> alleVedlegg = vedleggService.hentVedleggUnderBehandling(behandlingsId, forventning.getFillagerReferanse());
        for (Vedlegg vedlegg : alleVedlegg) {
            totalStorrelse += vedlegg.getStorrelse();
        }

        if (files != null) {
            for (FormDataBodyPart file : files) {
                totalStorrelse += file.getValueAs(File.class).length();
            }
        }
        return totalStorrelse;
    }
}
