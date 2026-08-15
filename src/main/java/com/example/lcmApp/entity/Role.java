package com.example.lcmApp.entity;

import org.hibernate.annotations.SQLInsert;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "roles")
@Data
@AllArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String name; // USER, ADMIN
    
    public Role() {}
    
    public Role(String name) {
        this.name = name;
    }
}
