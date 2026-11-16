package cat.copernic.nothinganpri.dto;

public class DocumentClient {

    private final byte[] dades;
    private final String tipus;

    public DocumentClient(byte[] dades, String tipus) {
        this.dades = dades;
        this.tipus = tipus;
    }

    public byte[] getDades() {
        return dades;
    }

    public String getTipus() {
        return tipus;
    }
}