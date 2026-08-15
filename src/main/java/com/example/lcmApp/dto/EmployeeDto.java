package com.example.lcmApp.dto;

import java.util.Set;
import lombok.Data;

@Data
public class EmployeeDto {
    
    private Long id;
    private String fullName;
    private Set<String> roles;
    
}
