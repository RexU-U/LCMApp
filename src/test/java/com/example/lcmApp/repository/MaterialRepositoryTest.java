package com.example.lcmApp.repository;

import com.example.lcmApp.entity.Material;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class MaterialRepositoryTest {

    @Autowired
    private RepositoryLcmMaterial repository;

    private Material material;
    private Long materialId;

    @BeforeEach
    void setUp() {
        repository.deleteAll();

        material = new Material(
            "Круги P80", 
            0.36, 
            "Абразивы", 
            "шт", 
            20.0, 
            10.0
        );
        material.setDescription("Абразивные круги зернистостью P80");
        material = repository.save(material);
        materialId = material.getMaterial_id();
    }
    
@Test
void testUpdateMaterialWithNullValues() {
    int updatedCount = repository.updateMaterial(
        materialId,
        material.getName(), // Передаем существующее имя
        "Абразивные круги зернистостью P80", // Описание можно не обновлять
        material.getVolume(), // Передаем текущее значение
        material.getUnit(), // Передаем текущее значение
        material.getType(), // Передаем текущее значение
        material.getPriority(), // Обязательное значение
        material.getInventory() // Обязательное значение
    );

    assertThat(updatedCount).isEqualTo(1);

    Optional<Material> found = repository.findByName(material.getName());
    assertThat(found).isPresent();
    // Проверяем, что описание не изменилось
    assertThat(found.get().getDescription()).isEqualTo("Абразивные круги зернистостью P80");
    assertThat(found.get().getVolume()).isEqualTo(20.0);
}
    
    @Test
void testUpdateMaterialOnlyDescription() {
    int updatedCount = repository.updateMaterial(
        materialId,
        material.getName(), // Передаем существующее имя
        "Только новое описание", // Новое описание
        material.getVolume(), // Передаем текущее значение
        material.getUnit(), // Передаем текущее значение
        material.getType(), // Передаем текущее значение
        material.getPriority(), // Обязательное значение
        material.getInventory() // Обязательное значение
    );

    assertThat(updatedCount).isEqualTo(1);

    Optional<Material> found = repository.findByName(material.getName());
    assertThat(found).isPresent();
    assertThat(found.get().getDescription()).isEqualTo("Только новое описание");
    assertThat(found.get().getVolume()).isEqualTo(20.0);
    assertThat(found.get().getType()).isEqualTo("Абразивы");
}

    @Test
    void testSave() {
        Material newMaterial = new Material(
            "Лак", 
            0.86, 
            "ЛКМ", 
            "л", 
            30.0, 
            5.0
        );
        newMaterial.setDescription("Прозрачный лак для авто");
        Material saved = repository.save(newMaterial);

        assertThat(saved.getMaterial_id()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Лак");
        assertThat(saved.getPriority()).isEqualTo(0.86);
        assertThat(saved.getType()).isEqualTo("ЛКМ");
        assertThat(saved.getUnit()).isEqualTo("л");
        assertThat(saved.getVolume()).isEqualTo(30.0);
        assertThat(saved.getDescription()).isEqualTo("Прозрачный лак для авто");
        assertThat(saved.getInventory()).isEqualTo(5.0);
    }
}
