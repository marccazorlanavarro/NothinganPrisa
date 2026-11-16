package cat.copernic.nothinganpri.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;

@Entity
public class ClientJpaEntity extends UsuariJpaEntity {

    private String DNI;
    private String cadDNI;
    private String Lliconduir;
    private String cdLLiconduir;
    private String numtarjetacredit;
    private String adreca;
    private String nacionalitat;
    private String fotoDniReverso;

    @Lob
    @Column(name = "foto_dni_anverso_dades", columnDefinition = "LONGBLOB")
    private byte[] fotoDniAnverso;

    @Column(length = 50)
    private String fotoDniAnversoTipus;

    @Lob
    @Column(name = "foto_permiso_dades", columnDefinition = "LONGBLOB")
    private byte[] fotoPermiso;

    @Column(length = 50)
    private String fotoPermisoTipus;

    @Column(name = "titular_targeta")
    private String titularTargeta;

    @Column(name = "caducitat_targeta")
    private String caducitatTargeta;

    public ClientJpaEntity() {
        super();
    }

    public ClientJpaEntity(
            String DNI,
            String cadDNI,
            String Lliconduir,
            String cdLLiconduir,
            String numtarjetacredit,
            String adreca,
            String nacionalitat,
            String fotoDniReverso,
            byte[] fotoDniAnverso,
            String fotoDniAnversoTipus,
            byte[] fotoPermiso,
            String fotoPermisoTipus,
            String titularTargeta,
            String caducitatTargeta,
            Long id,
            String nom,
            String cognoms,
            String telefon,
            String email,
            String password,
            String rol) {

        super(id, nom, cognoms, telefon, email, password, rol);

        this.DNI = DNI;
        this.cadDNI = cadDNI;
        this.Lliconduir = Lliconduir;
        this.cdLLiconduir = cdLLiconduir;
        this.numtarjetacredit = numtarjetacredit;
        this.adreca = adreca;
        this.nacionalitat = nacionalitat;
        this.fotoDniReverso = fotoDniReverso;
        this.fotoDniAnverso = fotoDniAnverso;
        this.fotoDniAnversoTipus = fotoDniAnversoTipus;
        this.fotoPermiso = fotoPermiso;
        this.fotoPermisoTipus = fotoPermisoTipus;
        this.titularTargeta = titularTargeta;
        this.caducitatTargeta = caducitatTargeta;
    }

    public ClientJpaEntity(
            String DNI,
            String cadDNI,
            String Lliconduir,
            String cdLLiconduir,
            String numtarjetacredit,
            String adreca,
            String nacionalitat,
            String fotoDniReverso,
            byte[] fotoDniAnverso,
            String fotoDniAnversoTipus,
            byte[] fotoPermiso,
            String fotoPermisoTipus,
            String titularTargeta,
            String caducitatTargeta,
            String nom,
            String cognoms,
            String telefon,
            String email,
            String password,
            String rol) {

        super(nom, cognoms, telefon, email, password, rol);

        this.DNI = DNI;
        this.cadDNI = cadDNI;
        this.Lliconduir = Lliconduir;
        this.cdLLiconduir = cdLLiconduir;
        this.numtarjetacredit = numtarjetacredit;
        this.adreca = adreca;
        this.nacionalitat = nacionalitat;
        this.fotoDniReverso = fotoDniReverso;
        this.fotoDniAnverso = fotoDniAnverso;
        this.fotoDniAnversoTipus = fotoDniAnversoTipus;
        this.fotoPermiso = fotoPermiso;
        this.fotoPermisoTipus = fotoPermisoTipus;
        this.titularTargeta = titularTargeta;
        this.caducitatTargeta = caducitatTargeta;
    }

    public String getDNI() {
        return DNI;
    }

    public void setDNI(String DNI) {
        this.DNI = DNI;
    }

    public String getCadDNI() {
        return cadDNI;
    }

    public void setCadDNI(String cadDNI) {
        this.cadDNI = cadDNI;
    }

    public String getLliconduir() {
        return Lliconduir;
    }

    public void setLliconduir(String Lliconduir) {
        this.Lliconduir = Lliconduir;
    }

    public String getCdLLiconduir() {
        return cdLLiconduir;
    }

    public void setCdLLiconduir(String cdLLiconduir) {
        this.cdLLiconduir = cdLLiconduir;
    }

    public String getNumtarjetacredit() {
        return numtarjetacredit;
    }

    public void setNumtarjetacredit(String numtarjetacredit) {
        this.numtarjetacredit = numtarjetacredit;
    }

    public String getAdreca() {
        return adreca;
    }

    public void setAdreca(String adreca) {
        this.adreca = adreca;
    }

    public String getNacionalitat() {
        return nacionalitat;
    }

    public void setNacionalitat(String nacionalitat) {
        this.nacionalitat = nacionalitat;
    }

    public byte[] getFotoDniAnverso() {
        return fotoDniAnverso;
    }

    public void setFotoDniAnverso(byte[] fotoDniAnverso) {
        this.fotoDniAnverso = fotoDniAnverso;
    }

    public String getFotoDniAnversoTipus() {
        return fotoDniAnversoTipus;
    }

    public void setFotoDniAnversoTipus(String fotoDniAnversoTipus) {
        this.fotoDniAnversoTipus = fotoDniAnversoTipus;
    }

    public String getFotoDniReverso() {
        return fotoDniReverso;
    }

    public void setFotoDniReverso(String fotoDniReverso) {
        this.fotoDniReverso = fotoDniReverso;
    }

    public byte[] getFotoPermiso() {
        return fotoPermiso;
    }

    public void setFotoPermiso(byte[] fotoPermiso) {
        this.fotoPermiso = fotoPermiso;
    }

    public String getFotoPermisoTipus() {
        return fotoPermisoTipus;
    }

    public void setFotoPermisoTipus(String fotoPermisoTipus) {
        this.fotoPermisoTipus = fotoPermisoTipus;
    }

    public String getTitularTargeta() {
        return titularTargeta;
    }

    public void setTitularTargeta(String titularTargeta) {
        this.titularTargeta = titularTargeta;
    }

    public String getCaducitatTargeta() {
        return caducitatTargeta;
    }

    public void setCaducitatTargeta(String caducitatTargeta) {
        this.caducitatTargeta = caducitatTargeta;
    }

    public void setId(long l) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
