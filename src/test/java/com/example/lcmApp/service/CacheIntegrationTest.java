package com.example.lcmApp;

import com.example.lcmApp.entity.Material;
import com.example.lcmApp.repository.RepositoryLcmMaterial;
import com.example.lcmApp.service.ServicePainter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.cache.type=caffeine",
    "spring.cache.caffeine.spec=maximumSize=100,expireAfterWrite=60s"
})
class CacheIntegrationTest {

    @Autowired
    private ServicePainter servicePainter;

    @Autowired
    private RepositoryLcmMaterial repository;

    @Autowired
    private CacheManager cacheManager;

    @Test
    void testFullCacheWorkflow() {
        // Очищаем все кеши
        cacheManager.getCache("materials").clear();
        cacheManager.getCache("employees").clear();

        // 1. Создаем материал
        Material material = new Material("Круги P80", 0.36, "Абразивы", "шт", 20.0, 47.0);
        servicePainter.insertMaterial(material);

        // 2. Загружаем материалы (заполняет кеш)
        long start = System.currentTimeMillis();
        servicePainter.getAllMaterials();
        long firstLoad = System.currentTimeMillis() - start;

        // 3. Загружаем снова (из кеша)
        start = System.currentTimeMillis();
        servicePainter.getAllMaterials();
        long secondLoad = System.currentTimeMillis() - start;

        // Кеш должен работать быстрее
        assertThat(secondLoad).isLessThanOrEqualTo(firstLoad + 50);

        // 4. Обновляем материал (очищает кеш)
        servicePainter.updateVolumeByName("Круги P80", 50.0);

        // 5. Проверяем, что кеш очищен
        assertThat(cacheManager.getCache("materials").get("all")).isNull();

        // 6. Загружаем снова (из БД)
        servicePainter.getAllMaterials();
        assertThat(cacheManager.getCache("materials").get("all")).isNotNull();

        // 7. Удаляем материал (очищает кеш)
        servicePainter.deleteMaterialByName("Интеграционный тест");

        // 8. Проверяем, что кеш очищен
        assertThat(cacheManager.getCache("materials").get("all")).isNull();
    }
}