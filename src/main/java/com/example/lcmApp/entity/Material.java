package com.example.lcmApp.entity;

import jakarta.persistence.*;
import java.util.Objects;
import lombok.Data;

@Data
@Entity
@Table(name = "material")
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long material_id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = true)
    private String description;

    @Column(nullable = true)
    private Double volume;

    @Column(nullable = false)
    private String unit;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private Double priority;
    
    @Column(nullable = false)
    private Double inventory;
    
    public Material() {}
    
    public Material(String name, Double priority, String type, String unit, Double volume, Double inventory) {
        this.name = name;
        this.type = type;
        this.priority = priority;
        this.unit = unit;
        this.volume = volume;
        this.inventory = inventory;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Material material = (Material) o;
        
        if (material_id == null || material.material_id == null) {
            return false;
        }
        return Objects.equals(material_id, material.material_id) &&
               Objects.equals(name, material.name) &&
               Objects.equals(type, material.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(material_id, name, type);
    }
    
    @Override
    public String toString() {
        return "Material{" +
                "material_id='" + material_id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", volume=" + volume +
                ", unit='" + unit + '\'' +
                ", type='" + type + '\'' +
                ", priority=" + priority +
                ", inventory=" + inventory +
                '}';
    }
}
