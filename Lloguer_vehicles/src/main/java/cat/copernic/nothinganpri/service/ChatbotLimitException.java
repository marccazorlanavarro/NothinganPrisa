/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.nothinganpri.service;

/** L'usuari ha fet massa preguntes en poc temps. */
public class ChatbotLimitException extends RuntimeException {

    public ChatbotLimitException(String missatge) {
        super(missatge);
    }
}