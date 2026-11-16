package cat.copernic.nothinganpri.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;

public class DocumentacioForm implements Serializable {

    @NotBlank(message = "El DNI és obligatori")
    private String numeroDocument;

    @NotNull(message = "La data de caducitat del DNI és obligatòria")
    @Future(message = "La data de caducitat del DNI ha de ser futura")
    private LocalDate dataCaducitat;

    @NotBlank(message = "El tipus de llicència és obligatori")
    private String tipusLlicencia;

    @NotNull(message = "La data de caducitat de la llicència és obligatòria")
    @Future(message = "La data de caducitat de la llicència ha de ser futura")
    private LocalDate dataCaducitatLlicencia;

    private String nomDocumentIdentitat;
    private String nomLlicenciaConduir;
    private String caducitatDocument;
    private String caducitatLlicencia;

    // Imatges ja llegides i validades (ImatgeUtil.llegir); es guarden a la BD en registrar el client
    private FitxerPujat documentIdentitat;
    private FitxerPujat llicenciaConduir;

    public String getCaducitatDocument() {
        return caducitatDocument;
    }

    public void setCaducitatDocument(String caducitatDocument) {
        this.caducitatDocument = caducitatDocument;
    }

    public String getCaducitatLlicencia() {
        return caducitatLlicencia;
    }

    public void setCaducitatLlicencia(String caducitatLlicencia) {
        this.caducitatLlicencia = caducitatLlicencia;
    }

    public String getNumeroDocument() {
        return numeroDocument;
    }

    public void setNumeroDocument(String numeroDocument) {
        this.numeroDocument = numeroDocument;
    }

    public LocalDate getDataCaducitat() {
        return dataCaducitat;
    }

    public void setDataCaducitat(LocalDate dataCaducitat) {
        this.dataCaducitat = dataCaducitat;
    }

    public String getTipusLlicencia() {
        return tipusLlicencia;
    }

    public void setTipusLlicencia(String tipusLlicencia) {
        this.tipusLlicencia = tipusLlicencia;
    }

    public LocalDate getDataCaducitatLlicencia() {
        return dataCaducitatLlicencia;
    }

    public void setDataCaducitatLlicencia(LocalDate dataCaducitatLlicencia) {
        this.dataCaducitatLlicencia = dataCaducitatLlicencia;
    }

    public String getNomDocumentIdentitat() {
        return nomDocumentIdentitat;
    }

    public void setNomDocumentIdentitat(String nomDocumentIdentitat) {
        this.nomDocumentIdentitat = nomDocumentIdentitat;
    }

    public String getNomLlicenciaConduir() {
        return nomLlicenciaConduir;
    }

    public void setNomLlicenciaConduir(String nomLlicenciaConduir) {
        this.nomLlicenciaConduir = nomLlicenciaConduir;
    }

    public FitxerPujat getDocumentIdentitat() {
        return documentIdentitat;
    }

    public void setDocumentIdentitat(FitxerPujat documentIdentitat) {
        this.documentIdentitat = documentIdentitat;
    }

    public FitxerPujat getLlicenciaConduir() {
        return llicenciaConduir;
    }

    public void setLlicenciaConduir(FitxerPujat llicenciaConduir) {
        this.llicenciaConduir = llicenciaConduir;
    }
}