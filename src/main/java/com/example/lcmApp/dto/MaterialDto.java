package com.example.lcmApp.dto;

import lombok.*;

@Getter
@Setter
    
public class MaterialDto {
    
    private String name;
    private Double volume;
    private String unit;
    
    public MaterialDto() {}
    
    public MaterialDto(String name, Double volume, String unit) {
        this.name = name;
        this.volume = volume;
        this.unit = unit;
    }
  }