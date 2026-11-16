package cat.copernic.nothinganpri.dto;

import java.io.Serializable;

/**
 * Fitxer ja llegit i validat en memòria. Es fa servir per conservar a la sessió
 * les imatges del registre fins que el client confirma la sol·licitud
 * (un MultipartFile no sobreviu a la petició que l'ha rebut).
 */
public class FitxerPujat implements Serializable {

    private final String nom;
    private final String contentType;
    private final byte[] dades;

    public FitxerPujat(String nom, String contentType, byte[] dades) {
        this.nom = nom;
        this.contentType = contentType;
        this.dades = dades;
    }

    public String getNom() {
        return nom;
    }

    public String getContentType() {
        return contentType;
    }

    public byte[] getDades() {
        return dades;
    }
}