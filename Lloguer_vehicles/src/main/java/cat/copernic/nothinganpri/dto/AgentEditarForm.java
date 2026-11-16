package cat.copernic.nothinganpri.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AgentEditarForm {

    @NotBlank
    private String DNI;

    @NotBlank
    private String nom;

    @NotBlank
    private String cognoms;

    @NotBlank
    private String telefon;

    @NotBlank
    @Email
    private String email;

    private LocalDate dataContracte;

    public String getDNI() {
        return DNI;
    }

    public void setDNI(String DNI) {
        this.DNI = DNI;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getCognoms() {
        return cognoms;
    }

    public void setCognoms(String cognoms) {
        this.cognoms = cognoms;
    }

    public String getTelefon() {
        return telefon;
    }

    public void setTelefon(String telefon) {
        this.telefon = telefon;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getDataContracte() {
        return dataContracte;
    }

    public void setDataContracte(LocalDate dataContracte) {
        this.dataContracte = dataContracte;
    }
}