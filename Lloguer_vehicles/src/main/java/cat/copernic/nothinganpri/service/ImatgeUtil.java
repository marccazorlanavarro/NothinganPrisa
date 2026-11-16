package cat.copernic.nothinganpri.service;

import cat.copernic.nothinganpri.dto.FitxerPujat;
import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

/** Llegeix i valida imatges pujades. Llença IllegalArgumentException si no són vàlides. */
public final class ImatgeUtil {

    public static final long MIDA_MAXIMA = 5L * 1024 * 1024; // 5 MB

    private ImatgeUtil() {
    }

    public static FitxerPujat llegir(MultipartFile fitxer) {
        if (fitxer == null || fitxer.isEmpty()) {
            throw new IllegalArgumentException("Cal adjuntar una imatge");
        }
        if (fitxer.getSize() > MIDA_MAXIMA) {
            throw new IllegalArgumentException("La imatge no pot superar els 5 MB");
        }

        byte[] dades;
        try {
            dades = fitxer.getBytes();
        } catch (IOException e) {
            throw new IllegalArgumentException("No s'ha pogut llegir la imatge", e);
        }

        // El tipus es detecta pels primers bytes: no ens fiem del Content-Type del navegador
        String tipus = detectarTipus(dades);
        if (tipus == null) {
            throw new IllegalArgumentException("El fitxer ha de ser una imatge JPG, PNG, GIF o WEBP");
        }
        return new FitxerPujat(netejarNom(fitxer.getOriginalFilename()), tipus, dades);
    }

    private static String detectarTipus(byte[] d) {
        if (d.length >= 3 && (d[0] & 0xFF) == 0xFF && (d[1] & 0xFF) == 0xD8 && (d[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        if (d.length >= 8 && (d[0] & 0xFF) == 0x89 && d[1] == 'P' && d[2] == 'N' && d[3] == 'G') {
            return "image/png";
        }
        if (d.length >= 6 && d[0] == 'G' && d[1] == 'I' && d[2] == 'F' && d[3] == '8') {
            return "image/gif";
        }
        if (d.length >= 12 && d[0] == 'R' && d[1] == 'I' && d[2] == 'F' && d[3] == 'F'
                && d[8] == 'W' && d[9] == 'E' && d[10] == 'B' && d[11] == 'P') {
            return "image/webp";
        }
        return null;
    }

    private static String netejarNom(String original) {
        if (original == null || original.isBlank()) {
            return "imatge";
        }
        String nom = original.substring(Math.max(original.lastIndexOf('/'), original.lastIndexOf('\\')) + 1).trim();
        if (nom.isEmpty()) {
            return "imatge";
        }
        return nom.length() > 255 ? nom.substring(0, 255) : nom;
    }
}