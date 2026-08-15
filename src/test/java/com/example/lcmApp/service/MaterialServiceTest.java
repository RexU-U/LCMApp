package com.example.lcmApp.service;

import com.example.lcmApp.entity.Material;
import com.example.lcmApp.repository.RepositoryLcmMaterial;
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
class MaterialServiceTest {

    @Autowired
    private RepositoryLcmMaterial repository;

    private Material testMaterial;
    private Long testMaterialId;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        testMaterial = new Material("Круги P80", 0.36, "Абразивы", "шт", 50.0, 47.0);
        testMaterial.setDescription("Абразивные круги");
        testMaterial = repository.save(testMaterial);
        testMaterialId = testMaterial.getMaterial_id();
    }

    @Test
    void testSaveMaterial() {
        Material newMaterial = new Material("Лак", 0.86, "ЛКМ", "л", 30.0, 28.0);
        Material saved = repository.save(newMaterial);
        
        assertThat(saved.getMaterial_id()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Лак");
        assertThat(saved.getPriority()).isEqualTo(0.86);
        assertThat(saved.getType()).isEqualTo("ЛКМ");
        assertThat(saved.getUnit()).isEqualTo("л");
        assertThat(saved.getVolume()).isEqualTo(30.0);
        assertThat(repository.findAll()).hasSize(2);
    }

    @Test
    void testFindAllMaterials() {
        List<Material> materials = repository.findAll();
        assertThat(materials).hasSize(1);
        assertThat(materials.get(0).getName()).isEqualTo("Круги P80");
    }

    @Test
    void testFindByName() {
        Optional<Material> found = repository.findByName("Круги P80");
        assertThat(found).isPresent();
        assertThat(found.get().getType()).isEqualTo("Абразивы");
        assertThat(found.get().getPriority()).isEqualTo(0.36);
    }

    @Test
    void testUpdateVolume() {
        int updated = repository.updateVolumeByName("Круги P80", 10.0);
        assertThat(updated).isEqualTo(1);
        
        Optional<Material> found = repository.findByName("Круги P80");
        assertThat(found).isPresent();
        assertThat(found.get().getVolume()).isEqualTo(60.0);
    }

    @Test
    void testDeleteByName() {
        int deleted = repository.deleteByName("Круги P80");
        assertThat(deleted).isEqualTo(1);
        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    void testUpdateMaterial() {
        int updated = repository.updateMaterial(
            testMaterialId,
            "Круги P80",
            "Новое описание",
            100.0,
            "уп",
            "Абразивы-новые",
            0.99,
            1.0
        );
        
        assertThat(updated).isEqualTo(1);
        
        Optional<Material> found = repository.findByName("Круги P80");
        assertThat(found).isPresent();
        assertThat(found.get().getDescription()).isEqualTo("Новое описание");
        assertThat(found.get().getVolume()).isEqualTo(100.0);
        assertThat(found.get().getUnit()).isEqualTo("уп");
        assertThat(found.get().getType()).isEqualTo("Абразивы-новые");
        assertThat(found.get().getPriority()).isEqualTo(0.99);
    }

    @Test
    void testGetNamesByCategory() {
        Material m2 = new Material("Круги P120", 0.37, "Абразивы", "шт", 30.0, 83.0);
        repository.save(m2);
        
        List<String> names = repository.getMaterialsNameByCategory("Абразивы");
        assertThat(names).hasSize(2);
        assertThat(names).contains("Круги P80", "Круги P120");
    }

    @Test
    void testMultipleOperations() {
        Material newMaterial = new Material("Шпатлёвка", 0.31, "Шпатлёвки", "кг", 5.0, 4.0);
        repository.save(newMaterial);
        
        repository.updateVolumeByName("Шпатлёвка", 3.0);
        
        Optional<Material> found = repository.findByName("Шпатлёвка");
        assertThat(found).isPresent();
        assertThat(found.get().getVolume()).isEqualTo(8.0);
        
        repository.deleteByName("Шпатлёвка");
        assertThat(repository.findByName("Шпатлёвка")).isEmpty();
    }
}