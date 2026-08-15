package com.example.lcmApp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogEntry {
    private String operation;    // "ADDED" или "ISSUE"
    private String material;
    private Double quantity;
    private String unit;
    private String user;
    private String timestamp;    
}