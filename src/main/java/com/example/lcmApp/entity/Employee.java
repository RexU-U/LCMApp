package com.example.lcmApp.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "employee")
public class Employee {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long employee_id;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;
    
    @Column(nullable = false)
    private String password;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "employee_roles",
        joinColumns = @JoinColumn(name = "employee_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();
    
    // Конструктор для создания нового сотрудника
    public Employee(String fullName, String password) {
        this.fullName = fullName;
        this.password = password;
    }

    // Геттер для совместимости с существующим кодом
    public Long getId() {
        return employee_id;
    }

    public void setId(Long id) {
        this.employee_id = id;
    }
}
