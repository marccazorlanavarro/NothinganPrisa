package cat.copernic.nothinganpri.dto;

/**
 * Una línia de canvi d'una sol·licitud: valor actual del client
 * i valor que proposa, perquè l'agent pugui validar-lo o rebutjar-lo.
 */
public class CanviSolicitud {

    private final String camp;
    private final String actual;
    private final String proposat;

    public CanviSolicitud(String camp, String actual, String proposat) {
        this.camp = camp;
        this.actual = actual;
        this.proposat = proposat;
    }

    public String getCamp() {
        return camp;
    }

    public String getActual() {
        return actual;
    }

    public String getProposat() {
        return proposat;
    }
}