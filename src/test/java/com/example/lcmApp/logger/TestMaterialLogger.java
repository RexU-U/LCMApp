package com.example.lcmApp.logger;

import org.springframework.stereotype.Component;

@Component
public class TestMaterialLogger extends MaterialLogger {
    
    @Override
    public void logMaterialAddition(String name, double volume, String unit, String user) {
        // Пустая реализация для тестов
        System.out.println("LOG: Added " + volume + " " + unit + " of " + name + " by " + user);
    }
    
    @Override
    public void logMaterialWriteOff(String name, double volume, String unit, String user) {
        // Пустая реализация для тестов
        System.out.println("LOG: Write off " + volume + " " + unit + " of " + name + " by " + user);
    }
}
