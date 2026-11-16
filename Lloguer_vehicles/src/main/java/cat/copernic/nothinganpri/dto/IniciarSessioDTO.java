/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.nothinganpri.dto;

/**
 *
 * @author lopez
 */
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class IniciarSessioDTO {

    @NotBlank(message = "El correu és obligatori")
    @Email(message = "Introdueix un correu vàlid")
    private String email;

    @NotBlank(message = "La contrasenya és obligatòria")
    private String password;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
