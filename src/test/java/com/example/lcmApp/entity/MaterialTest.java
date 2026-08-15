package com.example.lcmApp.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MaterialTest {

    @Test
    void testMaterialConstructorAndGetters() {
        Material material = new Material("Круги P80", 0.36, "Абразивы", "шт", 20.0, 47.0);
        material.setDescription("Абразивные круги");
        material.setMaterial_id(1L);
        
        assertAll("Material properties",
            () -> assertEquals(1L, material.getMaterial_id()),
            () -> assertEquals("Круги P80", material.getName()),
            () -> assertEquals(0.36, material.getPriority()),
            () -> assertEquals("Абразивы", material.getType()),
            () -> assertEquals("шт", material.getUnit()),
            () -> assertEquals(20.0, material.getVolume()),
            () -> assertEquals(47.0, material.getInventory()),
            () -> assertEquals("Абразивные круги", material.getDescription())
        );
    }

    @Test
    void testMaterialConstructorGeneratesNullId() {
        Material material = new Material("Круги P80", 0.36, "Абразивы", "шт", 20.0, 47.0);
        
        // ID должен быть null до сохранения в БД
        assertNull(material.getMaterial_id());
    }

    @Test
    void testDefaultConstructorGeneratesNullId() {
        Material material = new Material();
        
        assertNull(material.getMaterial_id());
    }

    @Test
    void testMaterialSetters() {
        Material material = new Material();
        material.setMaterial_id(42L);
        material.setName("Лак");
        material.setPriority(0.86);
        material.setType("ЛКМ");
        material.setUnit("л");
        material.setVolume(30.0);
        material.setInventory(28.0);
        material.setDescription("Прозрачный лак");
        
        assertAll("Setters",
            () -> assertEquals(42L, material.getMaterial_id()),
            () -> assertEquals("Лак", material.getName()),
            () -> assertEquals(0.86, material.getPriority()),
            () -> assertEquals("ЛКМ", material.getType()),
            () -> assertEquals("л", material.getUnit()),
            () -> assertEquals(30.0, material.getVolume()),
            () -> assertEquals(28.0, material.getInventory()),
            () -> assertEquals("Прозрачный лак", material.getDescription())
        );
    }

    @Test
    void testMaterialSetId() {
        Material material = new Material();
        material.setMaterial_id(100L);
        
        assertEquals(100L, material.getMaterial_id());
    }

    @Test
    void testMaterialSetNullId() {
        Material material = new Material();
        material.setMaterial_id(1L);
        material.setMaterial_id(null);
        
        assertNull(material.getMaterial_id());
    }

    @Test
    void testMaterialHasValidId() {
        Material material = new Material();
        // В классе Material нет метода hasValidId, но мы можем проверить вручную
        assertNull(material.getMaterial_id()); // null -> невалидный
        
        material.setMaterial_id(1L);
        assertNotNull(material.getMaterial_id()); // валидный
        
        material.setMaterial_id(null);
        assertNull(material.getMaterial_id()); // снова невалидный
    }

    @Test
    void testMaterialEqualsAndHashCode() {
        Long sameId = 1L;
        
        Material m1 = new Material("Круги P80", 0.36, "Абразивы", "шт", 20.0, 47.0);
        m1.setMaterial_id(sameId);
        
        Material m2 = new Material("Круги P80", 0.36, "Абразивы", "шт", 20.0, 47.0);
        m2.setMaterial_id(sameId);
        
        Material m3 = new Material("Лак", 0.86, "ЛКМ", "л", 30.0, 28.0);
        m3.setMaterial_id(2L);
        
        assertEquals(m1, m2);
        assertNotEquals(m1, m3);
        assertEquals(m1.hashCode(), m2.hashCode());
    }

    @Test
    void testMaterialEqualsWithDifferentIds() {
        Material m1 = new Material("Круги P80", 0.36, "Абразивы", "шт", 20.0, 47.0);
        m1.setMaterial_id(1L);
        
        Material m2 = new Material("Круги P80", 0.36, "Абразивы", "шт", 20.0, 47.0);
        m2.setMaterial_id(2L);
        
        assertNotEquals(m1, m2);
    }

    @Test
    void testMaterialEqualsWhenBothIdsNull() {
        Material m1 = new Material("Круги P80", 0.36, "Абразивы", "шт", 20.0, 47.0);
        Material m2 = new Material("Круги P80", 0.36, "Абразивы", "шт", 20.0, 47.0);
        
        // Оба ID = null, equals должен возвращать false (так как это разные объекты)
        assertNotEquals(m1, m2);
    }

    @Test
    void testMaterialNullFields() {
        Material material = new Material();
        material.setMaterial_id(null);
        
        assertNull(material.getMaterial_id());
        assertNull(material.getName());
        assertNull(material.getDescription());
        assertNull(material.getVolume());
        assertNull(material.getUnit());
        assertNull(material.getType());
        assertNull(material.getPriority());
        assertNull(material.getInventory());
    }

    @Test
    void testMaterialToString() {
        Material material = new Material("Круги P80", 0.36, "Абразивы", "шт", 20.0, 47.0);
        material.setDescription("Абразивные круги");
        material.setMaterial_id(1L);
        
        String str = material.toString();
        
        assertNotNull(str);
        assertTrue(str.contains("Круги P80"));
        assertTrue(str.contains("Абразивы"));
        assertTrue(str.contains("Абразивные круги"));
        assertTrue(str.contains("material_id='1'"));
    }

    @Test
    void testMaterialToStringWithNullId() {
        Material material = new Material("Круги P80", 0.36, "Абразивы", "шт", 20.0, 47.0);
        material.setDescription("Абразивные круги");
        
        String str = material.toString();
        
        assertNotNull(str);
        assertTrue(str.contains("Круги P80"));
        assertTrue(str.contains("Абразивы"));
        assertTrue(str.contains("material_id='null'"));
    }

    @Test
    void testMaterialGenerateSequentialIds() {
        // В реальном приложении ID генерируются БД, здесь мы симулируем
        Material m1 = new Material();
        m1.setMaterial_id(1L);
        
        Material m2 = new Material();
        m2.setMaterial_id(2L);
        
        Material m3 = new Material();
        m3.setMaterial_id(3L);
        
        assertEquals(1L, m1.getMaterial_id());
        assertEquals(2L, m2.getMaterial_id());
        assertEquals(3L, m3.getMaterial_id());
        
        // Проверяем, что ID последовательные
        assertEquals(m2.getMaterial_id(), m1.getMaterial_id() + 1);
        assertEquals(m3.getMaterial_id(), m2.getMaterial_id() + 1);
    }

    @Test
    void testMaterialWithNegativeId() {
        Material material = new Material();
        material.setMaterial_id(-1L);
        
        assertEquals(-1L, material.getMaterial_id());
        assertNotNull(material.getMaterial_id()); // -1 считается валидным (не null)
    }

    @Test
    void testMaterialIdBounds() {
        Material material = new Material();
        Long maxLong = Long.MAX_VALUE;
        Long minLong = Long.MIN_VALUE;
        
        material.setMaterial_id(maxLong);
        assertEquals(maxLong, material.getMaterial_id());
        
        material.setMaterial_id(minLong);
        assertEquals(minLong, material.getMaterial_id());
    }
}