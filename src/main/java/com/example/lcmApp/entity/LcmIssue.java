package com.example.lcmApp.entity;

import org.hibernate.annotations.SQLInsert;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.Data;


@Entity
@Table(name = "lcmIssue")
@Data
public class LcmIssue {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    
    @Column(nullable = false)
    private Double quantity; //Количество материала на выдаче
    
    @Column(name = "issue_data")
    private Instant issueDate;
    
    @ManyToOne
    @JoinColumn(name = "material_fk", referencedColumnName = "material_id")
    private Material material;
    
    
    @ManyToOne
    @JoinColumn(name = "painten_fc", referencedColumnName = "employee_id")
    private Employee employee;
    
    public LcmIssue(Double quantity, Instant issueDate, Material material, Employee employee) {
        this.quantity = quantity;
        this.issueDate = issueDate;
        this.material = material;
        this.employee = employee;
    }
}