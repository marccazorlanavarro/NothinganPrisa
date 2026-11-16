/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.nothinganpri.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 *
 * @author lopez
 */
public class RegistreForm {

    @NotBlank(message = "El nom és obligatori")
    private String nom;

    @NotBlank(message = "Els cognoms són obligatoris")
    private String cognoms;

    @NotBlank(message = "El correu electrònic és obligatori")
    @Email(message = "El correu electrònic no és vàlid")
    private String email;

    @NotBlank(message = "El telèfon és obligatori")
    private String telefon;

    @NotBlank(message = "La nacionalitat és obligatòria")
    private String nacionalitat;

    @NotBlank(message = "L'adreça és obligatòria")
    private String adreca;

    @NotBlank(message = "La contrasenya és obligatòria")
    @Size(min = 8,
          message = "La contrasenya ha de tenir almenys 8 caràcters")
    
    private String password;


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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefon() {
        return telefon;
    }

    public void setTelefon(String telefon) {
        this.telefon = telefon;
    }

    public String getNacionalitat() {
        return nacionalitat;
    }

    public void setNacionalitat(String nacionalitat) {
        this.nacionalitat = nacionalitat;
    }

    public String getAdreca() {
        return adreca;
    }

    public void setAdreca(String adreca) {
        this.adreca = adreca;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
