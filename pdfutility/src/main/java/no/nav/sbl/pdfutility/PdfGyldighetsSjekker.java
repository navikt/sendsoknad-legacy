package no.nav.sbl.pdfutility;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;

public class PdfGyldighetsSjekker {

    private static final Logger logger = getLogger(PdfGyldighetsSjekker.class);

    public static void erGyldig(byte[] input) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(input);
            PDDocument document = PDDocument.load(bais)){
            erGyldig(document);
        } catch (IOException ioe) {
            logger.error("Klarte ikke å sjekke om vedlegget er signert eller lesebeskyttet {}", ioe.getMessage());
            throw new RuntimeException("Klarte ikke å sjekke om vedlegget er signert eller lesebeskyttet");
        }
    }

    private static void erGyldig(PDDocument document) throws IOException {
        if (!document.getSignatureDictionaries().isEmpty()) {
            logger.error("Opplasting av vedlegg feilet da PDF er signert");
            throw new RuntimeException("opplasting.feilmelding.pdf.signert");
        } else if (document.isEncrypted()) {
            logger.error("Opplasting av vedlegg feilet da PDF er kryptert");
            throw new RuntimeException("opplasting.feilmelding.pdf.kryptert");
        }
    }

}
