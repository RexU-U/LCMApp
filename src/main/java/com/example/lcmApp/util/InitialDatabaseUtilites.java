package com.example.lcmApp.util;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import com.example.lcmApp.entity.Material;
import com.example.lcmApp.entity.Role;

public class InitialDatabaseUtilites {
    
    public static List<Material> createMaterials() {
        List<Material> materials = new ArrayList<>();
        
        // Абразивы
        materials.add(new Material("Круги P80", 0.36, "Абразивы", "шт", 50.0, 47.0));
        materials.add(new Material("Круги P120", 0.37, "Абразивы", "шт", 100.0, 83.0));
        materials.add(new Material("Круги P180", 0.38, "Абразивы", "шт", 150.0, 12.0));
        materials.add(new Material("Круги P220", 0.39, "Абразивы", "шт", 200.0, 95.0));
        materials.add(new Material("Круги P320", 0.40, "Абразивы", "шт", 120.0, 34.0));
        materials.add(new Material("Круги P400", 0.41, "Абразивы", "шт", 80.0, 61.0));
        materials.add(new Material("Круги P500", 0.42, "Абразивы", "шт", 60.0, 78.0));
        materials.add(new Material("Круги P800", 0.43, "Абразивы", "шт", 40.0, 23.0));
        materials.add(new Material("Круги P1000", 0.44, "Абразивы", "шт", 30.0, 56.0));
        materials.add(new Material("Круги P1500", 0.45, "Абразивы", "шт", 25.0, 89.0));
        materials.add(new Material("Круги P2000", 0.46, "Абразивы", "шт", 20.0, 41.0));
        materials.add(new Material("Круги P6000", 0.47, "Абразивы", "шт", 10.0, 73.0));
        
        // Очистка
        materials.add(new Material("Обезжириватель", 0.76, "Очистка", "л", 20.0, 14.0));
        materials.add(new Material("Антисиликон", 0.77, "Очистка", "л", 5.0, 92.0));
        materials.add(new Material("Растворитель 646", 0.79, "Очистка", "л", 30.0, 38.0));
        materials.add(new Material("Спирт", 0.80, "Очистка", "л", 10.0, 67.0));
        
        // Грунты
        materials.add(new Material("Грунт мокрый по мокрому белый", 0.20, "Грунты", "л", 20.0, 19.0));
        materials.add(new Material("Грунт мокрый по мокрому серый", 0.21, "Грунты", "л", 15.0, 84.0));
        materials.add(new Material("Грунт мокрый по мокрому чёрный", 0.22, "Грунты", "л", 10.0, 53.0));
        materials.add(new Material("Грунт акриловый белый", 0.23, "Грунты", "л", 25.0, 7.0));
        materials.add(new Material("Грунт акриловый серый", 0.24, "Грунты", "л", 20.0, 96.0));
        materials.add(new Material("Грунт акриловый чёрный", 0.25, "Грунты", "л", 15.0, 44.0));
        
        // ЛКМ
        materials.add(new Material("Лак", 0.86, "ЛКМ", "л", 30.0, 28.0));
        materials.add(new Material("Лак в баллоне", 0.87, "ЛКМ", "шт", 50.0, 71.0));
        materials.add(new Material("Добавка для пластика", 0.89, "ЛКМ", "л", 8.0, 59.0));
        
        // Полировка
        materials.add(new Material("Паста полировальная", 0.93, "Полировка", "кг", 3.0, 5.0));
        materials.add(new Material("Антиголограммная паста", 0.94, "Полировка", "кг", 2.0, 88.0));
        materials.add(new Material("Губка MicroFine", 0.99, "Полировка", "шт", 10.0, 32.0));
        
        // Шпатлёвки
        materials.add(new Material("Шпатлёвка Soft", 0.31, "Шпатлёвки", "кг", 8.0, 76.0));
        materials.add(new Material("Шпатлёвка волокнистая", 0.32, "Шпатлёвки", "кг", 6.0, 11.0));
        
        // Расходники
        materials.add(new Material("Маскировочная бумага", 0.99, "Расходники", "рул", 20.0, 62.0));
        materials.add(new Material("Маскировочная плёнка", 0.99, "Расходники", "рул", 5.0, 48.0));
        materials.add(new Material("Скотч 50х50", 0.99, "Расходники", "рул", 30.0, 99.0));
        materials.add(new Material("Валик поролоновый", 0.99, "Расходники", "шт", 3.0, 17.0));
        materials.add(new Material("Перчатки резиновые", 0.99, "Расходники", "пара", 20.0, 55.0));
        materials.add(new Material("Лейка", 0.99, "Расходники", "шт", 5.0, 81.0));
        materials.add(new Material("Отвердитель", 0.98, "Расходники", "шт", 50.0, 36.0));
        materials.add(new Material("Ёмкость 0,33", 0.99, "Расходники", "шт", 100.0, 3.0));
        materials.add(new Material("Крышка 0,33", 0.99, "Расходники", "шт", 100.0, 69.0));
        
        return materials;
    }
    
    public static Set<Role> createRoles() {
        
        Set<Role> roles = new HashSet<Role>();
        
        roles.add(new Role("USER"));
        roles.add(new Role("ADMIN"));
        
        return roles;
    }
}