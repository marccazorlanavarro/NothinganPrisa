package cat.copernic.nothinganpri.enums;

public enum EstatReserva {
    PENDENT("Pendent"),
    ACTIVA("Activa"),
    EN_CURS("En curs"),
    FINALITZADA("Finalitzada"),
    ANUL_LADA("Anul·lada");

    private final String nomFormatejat;

    EstatReserva(String nomFormatejat) {
        this.nomFormatejat = nomFormatejat;
    }

    public String getNomFormatejat() {
        return nomFormatejat;
    }
}