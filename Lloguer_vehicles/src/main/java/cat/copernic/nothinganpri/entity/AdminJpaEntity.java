package cat.copernic.nothinganpri.entity;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author lopez
 */

import cat.copernic.nothinganpri.entity.UsuariJpaEntity;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@DiscriminatorValue("AdminJpaEntity")
public class AdminJpaEntity extends UsuariJpaEntity {

    public AdminJpaEntity() {
        super();
    }

    public AdminJpaEntity(Long id, String nom, String cognoms, String telefon, String email, String password, String rol) {
        super(id, nom, cognoms, telefon, email, password, rol);
    }

    public AdminJpaEntity(String nom, String cognoms, String telefon, String email, String password, String rol) {
        super(nom, cognoms, telefon, email, password, rol);
    }

    
    
    
}
