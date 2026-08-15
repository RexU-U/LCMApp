package com.example.lcmApp.logger;

/**
 * Заглушка для MaterialLogger, используемая в тестах.
 * Не выполняет никаких действий, только логирует в консоль для отладки.
 */
public class MaterialLoggerStub extends MaterialLogger {

    @Override
    public void logMaterialAddition(String name, double volume, String unit, String user) {
        // Пустая реализация для тестов
        System.out.println("[TEST LOG] Добавление материала: " + name + ", объем: " + volume + " " + unit + ", пользователь: " + user);
    }

    @Override
    public void logMaterialWriteOff(String name, double volume, String unit, String user) {
        // Пустая реализация для тестов
        System.out.println("[TEST LOG] Списание материала: " + name + ", объем: " + volume + " " + unit + ", пользователь: " + user);
    }
}
