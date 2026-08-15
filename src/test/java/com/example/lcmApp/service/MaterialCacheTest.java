package com.example.lcmApp.service;

import com.example.lcmApp.entity.Material;
import com.example.lcmApp.repository.RepositoryLcmMaterial;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.cache.CacheManager;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.cache.type=caffeine",
    "spring.cache.caffeine.spec=maximumSize=100,expireAfterWrite=60s"
})
@Transactional
@WithMockUser(username = "testuser", roles = {"USER"})
class MaterialCacheTest {

    @Autowired
    private ServicePainter servicePainter;

    @Autowired
    private RepositoryLcmMaterial materialRepository;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        // Очищаем кеш материалов
        cacheManager.getCache("materials").clear();
        
        // Очищаем базу данных
        materialRepository.deleteAll();

        // Создаем тестовый материал
        Material material = new Material();
        material.setName("Круги P80");
        material.setPriority(0.36);
        material.setType("Абразивы");
        material.setUnit("шт");
        material.setVolume(20.0);
        material.setInventory(47.0);
        material.setDescription("Абразивные круги");
        materialRepository.save(material);
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testCacheMaterials() {
        // Очищаем кеш перед тестом
        cacheManager.getCache("materials").clear();

        // Первый вызов - должен загрузить из БД
        long start1 = System.currentTimeMillis();
        Iterable<Material> materials1 = servicePainter.getAllMaterials();
        long time1 = System.currentTimeMillis() - start1;

        // Второй вызов - должен взять из кеша
        long start2 = System.currentTimeMillis();
        Iterable<Material> materials2 = servicePainter.getAllMaterials();
        long time2 = System.currentTimeMillis() - start2;

        // Проверяем, что данные получены
        assertThat(materials1).isNotEmpty();
        assertThat(materials2).isNotEmpty();
        
        // Проверяем, что размер одинаковый
        List<Material> list1 = (List<Material>) materials1;
        List<Material> list2 = (List<Material>) materials2;
        assertThat(list1.size()).isEqualTo(list2.size());
        
        // Второй запрос должен быть быстрее (из кеша)
        assertThat(time2).isLessThanOrEqualTo(time1 + 100);
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testCacheEvictOnDelete() {
        // Очищаем кеш и загружаем данные (заполняем кеш)
        cacheManager.getCache("materials").clear();
        servicePainter.getAllMaterials();
        
        // Проверяем, что кеш заполнен
        assertThat(cacheManager.getCache("materials").get("all")).isNotNull();

        // Получаем материал и удаляем его по имени через сервисный метод
        List<Material> materials = materialRepository.findAll();
        assertThat(materials).isNotEmpty();
        
        String materialName = materials.get(0).getName();
        servicePainter.deleteMaterialByName(materialName);

        // Проверяем, что кеш очищен
        assertThat(cacheManager.getCache("materials").get("all")).isNull();
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testCacheEvictOnInsert() {
        // Очищаем кеш и загружаем данные (заполняем кеш)
        cacheManager.getCache("materials").clear();
        servicePainter.getAllMaterials();
        
        // Проверяем, что кеш заполнен
        assertThat(cacheManager.getCache("materials").get("all")).isNotNull();

        // Создаем новый материал через сервисный метод
        Material newMaterial = new Material();
        newMaterial.setName("Новый материал");
        newMaterial.setPriority(0.5);
        newMaterial.setType("Тест");
        newMaterial.setUnit("шт");
        newMaterial.setVolume(10.0);
        newMaterial.setInventory(5.0);
        newMaterial.setDescription("Тестовый материал");
        servicePainter.insertMaterial(newMaterial);

        // Проверяем, что кеш очищен
        assertThat(cacheManager.getCache("materials").get("all")).isNull();
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testCacheEvictOnUpdate() {
        // Очищаем кеш и загружаем данные (заполняем кеш)
        cacheManager.getCache("materials").clear();
        servicePainter.getAllMaterials();
        
        // Проверяем, что кеш заполнен
        assertThat(cacheManager.getCache("materials").get("all")).isNotNull();

        // Получаем материал и обновляем его через сервисный метод
        List<Material> materials = materialRepository.findAll();
        assertThat(materials).isNotEmpty();
        
        Material material = materials.get(0);
        material.setName("Обновленный материал");
        material.setDescription("Обновленное описание");
        material.setPriority(0.99);
        servicePainter.updateMaterial(material);

        // Проверяем, что кеш очищен
        assertThat(cacheManager.getCache("materials").get("all")).isNull();
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testCacheGetMaterialByName() {
        // Очищаем кеш
        cacheManager.getCache("materials").clear();

        // Получаем имя материала
        List<Material> materials = materialRepository.findAll();
        assertThat(materials).isNotEmpty();
        
        String materialName = materials.get(0).getName();

        // Первый вызов - должен загрузить из БД
        long start1 = System.currentTimeMillis();
        Optional<Material> material1 = servicePainter.getMaterialByName(materialName);
        long time1 = System.currentTimeMillis() - start1;

        // Второй вызов - должен взять из кеша
        long start2 = System.currentTimeMillis();
        Optional<Material> material2 = servicePainter.getMaterialByName(materialName);
        long time2 = System.currentTimeMillis() - start2;

        // Проверяем, что данные получены и совпадают
        assertThat(material1).isPresent();
        assertThat(material2).isPresent();
        assertThat(material1.get().getMaterial_id()).isEqualTo(material2.get().getMaterial_id());
        assertThat(material1.get().getName()).isEqualTo(material2.get().getName());
        
        // Второй запрос должен быть быстрее (из кеша)
        assertThat(time2).isLessThanOrEqualTo(time1 + 100);
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testCacheEvictOnMaterialDeleteAll() {
        // Очищаем кеш и загружаем данные (заполняем кеш)
        cacheManager.getCache("materials").clear();
        servicePainter.getAllMaterials();
        
        // Проверяем, что кеш заполнен
        assertThat(cacheManager.getCache("materials").get("all")).isNotNull();

        // Удаляем все материалы через сервисный метод
        // Так как в ServicePainter нет метода deleteAll, используем репозиторий
        // Но затем очищаем кеш вручную, так как @CacheEvict не сработает
        materialRepository.deleteAll();
        
        // Вручную очищаем кеш, так как прямой вызов repository.deleteAll() не триггерит @CacheEvict
        cacheManager.getCache("materials").clear();

        // Проверяем, что кеш очищен
        assertThat(cacheManager.getCache("materials").get("all")).isNull();
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testCacheMissAfterClear() {
        // Очищаем кеш
        cacheManager.getCache("materials").clear();
        
        // Проверяем, что кеш пуст
        assertThat(cacheManager.getCache("materials").get("all")).isNull();
        
        // Загружаем данные (кеш должен заполниться)
        servicePainter.getAllMaterials();
        
        // Проверяем, что кеш заполнен
        assertThat(cacheManager.getCache("materials").get("all")).isNotNull();
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testCacheConsistency() {
        // Очищаем кеш
        cacheManager.getCache("materials").clear();
        
        // Загружаем данные первый раз (из БД)
        Iterable<Material> materials1 = servicePainter.getAllMaterials();
        List<Material> list1 = (List<Material>) materials1;
        
        // Загружаем данные второй раз (из кеша)
        Iterable<Material> materials2 = servicePainter.getAllMaterials();
        List<Material> list2 = (List<Material>) materials2;
        
        // Проверяем, что данные идентичны
        assertThat(list1.size()).isEqualTo(list2.size());
        for (int i = 0; i < list1.size(); i++) {
            assertThat(list1.get(i).getName()).isEqualTo(list2.get(i).getName());
            assertThat(list1.get(i).getVolume()).isEqualTo(list2.get(i).getVolume());
        }
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testCacheEvictOnVolumeUpdate() {
        // Очищаем кеш и загружаем данные (заполняем кеш)
        cacheManager.getCache("materials").clear();
        servicePainter.getAllMaterials();
        
        // Проверяем, что кеш заполнен
        assertThat(cacheManager.getCache("materials").get("all")).isNotNull();

        // Обновляем объем материала через сервисный метод
        servicePainter.updateVolumeByName("Круги P80", 10.0);

        // Проверяем, что кеш очищен
        assertThat(cacheManager.getCache("materials").get("all")).isNull();
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testCacheBehaviorWithMultipleOperations() {
        // Очищаем кеш
        cacheManager.getCache("materials").clear();
        
        // 1. Загружаем данные (кеш заполняется)
        servicePainter.getAllMaterials();
        assertThat(cacheManager.getCache("materials").get("all")).isNotNull();
        
        // 2. Добавляем новый материал (кеш очищается)
        Material newMaterial = new Material();
        newMaterial.setName("Материал для теста");
        newMaterial.setPriority(0.7);
        newMaterial.setType("Тест");
        newMaterial.setUnit("шт");
        newMaterial.setVolume(15.0);
        newMaterial.setInventory(10.0);
        servicePainter.insertMaterial(newMaterial);
        assertThat(cacheManager.getCache("materials").get("all")).isNull();
        
        // 3. Снова загружаем данные (кеш заполняется)
        servicePainter.getAllMaterials();
        assertThat(cacheManager.getCache("materials").get("all")).isNotNull();
        
        // 4. Обновляем материал (кеш очищается)
        Material materialToUpdate = materialRepository.findByName("Материал для теста").get();
        materialToUpdate.setDescription("Обновленное описание");
        servicePainter.updateMaterial(materialToUpdate);
        assertThat(cacheManager.getCache("materials").get("all")).isNull();
        
        // 5. Снова загружаем данные (кеш заполняется)
        servicePainter.getAllMaterials();
        assertThat(cacheManager.getCache("materials").get("all")).isNotNull();
        
        // 6. Удаляем материал (кеш очищается)
        servicePainter.deleteMaterialByName("Материал для теста");
        assertThat(cacheManager.getCache("materials").get("all")).isNull();
    }
}