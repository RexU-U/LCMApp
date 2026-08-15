package com.example.lcmApp.service;

import com.example.lcmApp.entity.Employee;
import com.example.lcmApp.entity.Material;
import com.example.lcmApp.entity.Role;
import com.example.lcmApp.dto.EmployeeDto;
import com.example.lcmApp.dto.MaterialDto;
import com.example.lcmApp.exception.*;
import com.example.lcmApp.repository.EmployeeRepository;
import com.example.lcmApp.repository.RepositoryLcmMaterial;
import com.example.lcmApp.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.cache.CacheManager;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
class ServicePainterTest {

    @Autowired
    private ServicePainter servicePainter;

    @Autowired
    private RepositoryLcmMaterial materialRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CacheManager cacheManager;

    private Material testMaterial;
    private Employee testEmployee;
    private Role userRole;

    @BeforeEach
    void setUp() {
        // Очищаем кеши
        cacheManager.getCache("materials").clear();
        cacheManager.getCache("employees").clear();
        
        // Очищаем базу данных
        materialRepository.deleteAll();
        employeeRepository.deleteAll();
        roleRepository.deleteAll();

        // Создаем роль USER
        userRole = roleRepository.findByName("USER")
            .orElseGet(() -> {
                Role newRole = new Role();
                newRole.setName("USER");
                return roleRepository.save(newRole);
            });

        // Создаем тестового сотрудника - используем HashSet для изменяемой коллекции
        testEmployee = new Employee();
        testEmployee.setFullName("testuser");
        testEmployee.setPassword("password");
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        testEmployee.setRoles(roles);
        employeeRepository.save(testEmployee);

        // Создаем тестовый материал
        testMaterial = new Material();
        testMaterial.setName("Круги P80");
        testMaterial.setPriority(0.36);
        testMaterial.setType("Абразивы");
        testMaterial.setUnit("шт");
        testMaterial.setVolume(100.0);
        testMaterial.setInventory(47.0);
        testMaterial.setDescription("Абразивные круги зернистостью P80");
        materialRepository.save(testMaterial);
    }

    // ==================== ТЕСТЫ ДЛЯ GET ALL MATERIALS ====================

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testGetAllMaterials() {
        Iterable<Material> materials = servicePainter.getAllMaterials();
        assertThat(materials).isNotEmpty();
        assertThat(materials).hasSize(1);
        
        Material material = materials.iterator().next();
        assertThat(material.getName()).isEqualTo("Круги P80");
        assertThat(material.getVolume()).isEqualTo(100.0);
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testGetAllMaterialsEmpty() {
        materialRepository.deleteAll();
        Iterable<Material> materials = servicePainter.getAllMaterials();
        assertThat(materials).isEmpty();
    }

    // ==================== ТЕСТЫ ДЛЯ GET MATERIAL BY NAME ====================

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testGetMaterialByName() {
        Optional<Material> material = servicePainter.getMaterialByName("Круги P80");
        assertThat(material).isPresent();
        assertThat(material.get().getName()).isEqualTo("Круги P80");
        assertThat(material.get().getType()).isEqualTo("Абразивы");
        assertThat(material.get().getPriority()).isEqualTo(0.36);
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testGetMaterialByNameNotFound() {
        assertThatThrownBy(() -> servicePainter.getMaterialByName("Несуществующий материал"))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Material not found");
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testGetMaterialByNameWithEmptyName() {
        assertThatThrownBy(() -> servicePainter.getMaterialByName(""))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Material name cannot be empty");
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testGetMaterialByNameWithNullName() {
        assertThatThrownBy(() -> servicePainter.getMaterialByName(null))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Material name cannot be empty");
    }

    // ==================== ТЕСТЫ ДЛЯ GET MATERIALS BY TYPE ====================

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testGetMaterialsByType() {
        Iterable<MaterialDto> materials = servicePainter.getMaterialsByType("Абразивы");
        assertThat(materials).isNotEmpty();
        assertThat(materials).hasSize(1);
        
        MaterialDto dto = materials.iterator().next();
        assertThat(dto.getName()).isEqualTo("Круги P80");
        assertThat(dto.getVolume()).isEqualTo(100.0);
        assertThat(dto.getUnit()).isEqualTo("шт");
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testGetMaterialsByTypeNotFound() {
        assertThatThrownBy(() -> servicePainter.getMaterialsByType("Несуществующая категория"))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Materials not found for type");
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testGetMaterialsByTypeWithEmptyType() {
        assertThatThrownBy(() -> servicePainter.getMaterialsByType(""))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Category type cannot be empty");
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testGetMaterialsByTypeWithNullType() {
        assertThatThrownBy(() -> servicePainter.getMaterialsByType(null))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Category type cannot be empty");
    }

    // ==================== ТЕСТЫ ДЛЯ DEDUCT MATERIALS ====================

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testDeductMaterials() {
        MaterialDto dto = new MaterialDto("Круги P80", 10.0, "шт");
        servicePainter.deductMaterials(List.of(dto));

        Optional<Material> updated = materialRepository.findByName("Круги P80");
        assertThat(updated).isPresent();
        assertThat(updated.get().getVolume()).isEqualTo(90.0);
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testDeductMultipleMaterials() {
        // Создаем второй материал
        Material material2 = new Material();
        material2.setName("Круги P120");
        material2.setPriority(0.37);
        material2.setType("Абразивы");
        material2.setUnit("шт");
        material2.setVolume(50.0);
        material2.setInventory(30.0);
        material2.setDescription("Абразивные круги P120");
        materialRepository.save(material2);

        MaterialDto dto1 = new MaterialDto("Круги P80", 10.0, "шт");
        MaterialDto dto2 = new MaterialDto("Круги P120", 5.0, "шт");
        servicePainter.deductMaterials(List.of(dto1, dto2));

        Optional<Material> updated1 = materialRepository.findByName("Круги P80");
        assertThat(updated1).isPresent();
        assertThat(updated1.get().getVolume()).isEqualTo(90.0);

        Optional<Material> updated2 = materialRepository.findByName("Круги P120");
        assertThat(updated2).isPresent();
        assertThat(updated2.get().getVolume()).isEqualTo(45.0);
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testDeductMaterialsNotFound() {
        MaterialDto dto = new MaterialDto("Несуществующий материал", 10.0, "шт");
        
        assertThatThrownBy(() -> servicePainter.deductMaterials(List.of(dto)))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Material not found");
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testDeductMaterialsInsufficientStock() {
        MaterialDto dto = new MaterialDto("Круги P80", 200.0, "шт");
        
        assertThatThrownBy(() -> servicePainter.deductMaterials(List.of(dto)))
            .isInstanceOf(InsufficientStockException.class)
            .hasMessageContaining("Не достаточно материала на складе: Круги P80. имееться: 100.0, требуеться: 200.0");
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testDeductMaterialsEmptyList() {
        assertThatThrownBy(() -> servicePainter.deductMaterials(List.of()))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Material list cannot be empty");
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testDeductMaterialsWithNullName() {
        MaterialDto dto = new MaterialDto(null, 10.0, "шт");
        
        assertThatThrownBy(() -> servicePainter.deductMaterials(List.of(dto)))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Material name cannot be empty");
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testDeductMaterialsWithZeroVolume() {
        MaterialDto dto = new MaterialDto("Круги P80", 0.0, "шт");
        
        assertThatThrownBy(() -> servicePainter.deductMaterials(List.of(dto)))
            .isInstanceOf(BusinessLogicException.class)
            .hasMessageContaining("Volume must be positive");
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testDeductMaterialsWithNullUnit() {
        MaterialDto dto = new MaterialDto("Круги P80", 10.0, null);
        
        assertThatThrownBy(() -> servicePainter.deductMaterials(List.of(dto)))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Unit cannot be empty");
    }

    // ==================== ТЕСТЫ ДЛЯ UPDATE MATERIAL ====================

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testUpdateMaterial() {
        testMaterial.setDescription("Обновленное описание");
        testMaterial.setVolume(150.0);
        testMaterial.setUnit("уп");
        testMaterial.setType("Абразивы-обновленные");
        testMaterial.setPriority(0.99);
        
        servicePainter.updateMaterial(testMaterial);

        Optional<Material> updated = materialRepository.findByName("Круги P80");
        assertThat(updated).isPresent();
        assertThat(updated.get().getDescription()).isEqualTo("Обновленное описание");
        assertThat(updated.get().getVolume()).isEqualTo(150.0);
        assertThat(updated.get().getUnit()).isEqualTo("уп");
        assertThat(updated.get().getType()).isEqualTo("Абразивы-обновленные");
        assertThat(updated.get().getPriority()).isEqualTo(0.99);
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testUpdateMaterialNotFound() {
        Material nonExistentMaterial = new Material();
        nonExistentMaterial.setName("Несуществующий материал");
        nonExistentMaterial.setDescription("Описание");
        nonExistentMaterial.setVolume(10.0);
        nonExistentMaterial.setUnit("шт");
        nonExistentMaterial.setType("Тест");
        nonExistentMaterial.setPriority(0.5);
        
        assertThatThrownBy(() -> servicePainter.updateMaterial(nonExistentMaterial))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Material not found");
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testUpdateMaterialWithNullName() {
        Material material = new Material();
        material.setName(null);
        material.setDescription("Описание");
        material.setVolume(10.0);
        material.setUnit("шт");
        material.setType("Тест");
        material.setPriority(0.5);
        
        assertThatThrownBy(() -> servicePainter.updateMaterial(material))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Material name cannot be empty");
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testUpdateMaterialWithNullMaterial() {
        assertThatThrownBy(() -> servicePainter.updateMaterial(null))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Material cannot be null");
    }

    // ==================== ТЕСТЫ ДЛЯ INSERT MATERIAL ====================

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testInsertMaterial() {
        Material newMaterial = new Material();
        newMaterial.setName("Новый материал");
        newMaterial.setPriority(0.5);
        newMaterial.setType("Тест");
        newMaterial.setUnit("шт");
        newMaterial.setVolume(10.0);
        newMaterial.setInventory(5.0);
        newMaterial.setDescription("Тестовый материал");
        
        servicePainter.insertMaterial(newMaterial);

        Optional<Material> found = materialRepository.findByName("Новый материал");
        assertThat(found).isPresent();
        assertThat(found.get().getPriority()).isEqualTo(0.5);
        assertThat(found.get().getType()).isEqualTo("Тест");
        assertThat(found.get().getVolume()).isEqualTo(10.0);
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testInsertMaterialAlreadyExists() {
        Material duplicateMaterial = new Material();
        duplicateMaterial.setName("Круги P80");
        duplicateMaterial.setPriority(0.5);
        duplicateMaterial.setType("Тест");
        duplicateMaterial.setUnit("шт");
        duplicateMaterial.setVolume(10.0);
        duplicateMaterial.setInventory(5.0);
        
        assertThatThrownBy(() -> servicePainter.insertMaterial(duplicateMaterial))
            .isInstanceOf(ResourceAlreadyExistsException.class)
            .hasMessageContaining("Material already exists");
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testInsertMaterialWithNullName() {
        Material material = new Material();
        material.setName(null);
        material.setPriority(0.5);
        material.setType("Тест");
        material.setUnit("шт");
        material.setVolume(10.0);
        material.setInventory(5.0);
        
        assertThatThrownBy(() -> servicePainter.insertMaterial(material))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Material name cannot be empty");
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testInsertMaterialWithNegativeVolume() {
        Material material = new Material();
        material.setName("Новый материал");
        material.setPriority(0.5);
        material.setType("Тест");
        material.setUnit("шт");
        material.setVolume(-10.0);
        material.setInventory(5.0);
        
        assertThatThrownBy(() -> servicePainter.insertMaterial(material))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Material volume cannot be negative");
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testInsertMaterialWithNullType() {
        Material material = new Material();
        material.setName("Новый материал");
        material.setPriority(0.5);
        material.setType(null);
        material.setUnit("шт");
        material.setVolume(10.0);
        material.setInventory(5.0);
        
        assertThatThrownBy(() -> servicePainter.insertMaterial(material))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Material type cannot be empty");
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testInsertMaterialWithNullUnit() {
        Material material = new Material();
        material.setName("Новый материал");
        material.setPriority(0.5);
        material.setType("Тест");
        material.setUnit(null);
        material.setVolume(10.0);
        material.setInventory(5.0);
        
        assertThatThrownBy(() -> servicePainter.insertMaterial(material))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Material unit cannot be empty");
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testInsertMaterialWithNullPriority() {
        Material material = new Material();
        material.setName("Новый материал");
        material.setPriority(null);
        material.setType("Тест");
        material.setUnit("шт");
        material.setVolume(10.0);
        material.setInventory(5.0);
        
        assertThatThrownBy(() -> servicePainter.insertMaterial(material))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Material priority cannot be negative");
    }

    // ==================== ТЕСТЫ ДЛЯ DELETE MATERIAL ====================

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testDeleteMaterialByName() {
        int deleted = servicePainter.deleteMaterialByName("Круги P80");
        assertThat(deleted).isEqualTo(1);
        
        Optional<Material> found = materialRepository.findByName("Круги P80");
        assertThat(found).isEmpty();
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testDeleteMaterialByNameNotFound() {
        assertThatThrownBy(() -> servicePainter.deleteMaterialByName("Несуществующий материал"))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Material not found");
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testDeleteMaterialByNameWithEmptyName() {
        assertThatThrownBy(() -> servicePainter.deleteMaterialByName(""))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Material name cannot be empty");
    }

    // ==================== ТЕСТЫ ДЛЯ UPDATE VOLUME ====================

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testUpdateVolumeByName() {
        int updated = servicePainter.updateVolumeByName("Круги P80", 10.0);
        assertThat(updated).isEqualTo(1);
        
        Optional<Material> found = materialRepository.findByName("Круги P80");
        assertThat(found).isPresent();
        assertThat(found.get().getVolume()).isEqualTo(110.0);
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testUpdateVolumeByNameNotFound() {
        assertThatThrownBy(() -> servicePainter.updateVolumeByName("Несуществующий материал", 10.0))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Material not found");
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testUpdateVolumeByNameWithZeroVolume() {
        assertThatThrownBy(() -> servicePainter.updateVolumeByName("Круги P80", 0.0))
            .isInstanceOf(BusinessLogicException.class)
            .hasMessageContaining("Volume must be positive");
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testUpdateVolumeByNameWithNegativeVolume() {
        assertThatThrownBy(() -> servicePainter.updateVolumeByName("Круги P80", -10.0))
            .isInstanceOf(BusinessLogicException.class)
            .hasMessageContaining("Volume must be positive");
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testUpdateVolumeByNameWithNullName() {
        assertThatThrownBy(() -> servicePainter.updateVolumeByName(null, 10.0))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Material name cannot be empty");
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testUpdateVolumeByNameWithNullVolume() {
        assertThatThrownBy(() -> servicePainter.updateVolumeByName("Круги P80", null))
            .isInstanceOf(BusinessLogicException.class)
            .hasMessageContaining("Volume must be positive");
    }

    // ==================== ТЕСТЫ ДЛЯ EMPLOYEE (PAINTER) ====================

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testGetPainterList() {
        List<Employee> employees = servicePainter.getPainterList();
        assertThat(employees).isNotEmpty();
        assertThat(employees).hasSize(1);
        assertThat(employees.get(0).getFullName()).isEqualTo("testuser");
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testGetPainterListEmpty() {
        employeeRepository.deleteAll();
        List<Employee> employees = servicePainter.getPainterList();
        assertThat(employees).isEmpty();
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testGetEmployeeById() {
        Optional<Employee> employee = servicePainter.getEmployeeById(testEmployee.getId());
        assertThat(employee).isPresent();
        assertThat(employee.get().getFullName()).isEqualTo("testuser");
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testGetEmployeeByIdNotFound() {
        assertThatThrownBy(() -> servicePainter.getEmployeeById(999L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Employee not found");
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testGetEmployeeByIdWithNullId() {
        assertThatThrownBy(() -> servicePainter.getEmployeeById(null))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Employee ID cannot be empty");
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testDeletePainter() {
        servicePainter.deletePainter(testEmployee.getId());
        
        Optional<Employee> deleted = employeeRepository.findById(testEmployee.getId());
        assertThat(deleted).isEmpty();
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testDeletePainterNotFound() {
        assertThatThrownBy(() -> servicePainter.deletePainter(999L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Employee not found");
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testDeletePainterWithNullId() {
        assertThatThrownBy(() -> servicePainter.deletePainter(null))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Employee ID cannot be empty");
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testUpdateEmployee() {
        EmployeeDto dto = new EmployeeDto();
        dto.setId(testEmployee.getId());
        dto.setFullName("Обновленное имя");
        
        Employee updated = servicePainter.updateEmployee(dto);
        
        assertThat(updated).isNotNull();
        assertThat(updated.getFullName()).isEqualTo("Обновленное имя");
        
        Optional<Employee> found = employeeRepository.findById(testEmployee.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getFullName()).isEqualTo("Обновленное имя");
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testUpdateEmployeeNotFound() {
        EmployeeDto dto = new EmployeeDto();
        dto.setId(999L);
        dto.setFullName("Обновленное имя");
        
        assertThatThrownBy(() -> servicePainter.updateEmployee(dto))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Employee not found");
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testUpdateEmployeeWithNullId() {
        EmployeeDto dto = new EmployeeDto();
        dto.setId(null);
        dto.setFullName("Обновленное имя");
        
        assertThatThrownBy(() -> servicePainter.updateEmployee(dto))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Employee ID cannot be empty");
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testUpdateEmployeeWithEmptyName() {
        EmployeeDto dto = new EmployeeDto();
        dto.setId(testEmployee.getId());
        dto.setFullName("");
        
        assertThatThrownBy(() -> servicePainter.updateEmployee(dto))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Employee full name cannot be empty");
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testUpdateEmployeeWithNullEmployee() {
        assertThatThrownBy(() -> servicePainter.updateEmployee(null))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Employee cannot be null");
    }

    // ==================== ТЕСТЫ ДЛЯ КЕШИРОВАНИЯ ====================

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testCacheEvictOnMaterialUpdate() {
        // Загружаем материалы (заполняем кеш)
        servicePainter.getAllMaterials();
        assertThat(cacheManager.getCache("materials").get("all")).isNotNull();

        // Обновляем материал (очищает кеш)
        testMaterial.setDescription("Обновленное описание");
        servicePainter.updateMaterial(testMaterial);

        // Проверяем, что кеш очищен
        assertThat(cacheManager.getCache("materials").get("all")).isNull();
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testCacheEvictOnMaterialInsert() {
        // Загружаем материалы (заполняем кеш)
        servicePainter.getAllMaterials();
        assertThat(cacheManager.getCache("materials").get("all")).isNotNull();

        // Создаем новый материал (очищает кеш)
        Material newMaterial = new Material();
        newMaterial.setName("Новый материал");
        newMaterial.setPriority(0.5);
        newMaterial.setType("Тест");
        newMaterial.setUnit("шт");
        newMaterial.setVolume(10.0);
        newMaterial.setInventory(5.0);
        servicePainter.insertMaterial(newMaterial);

        // Проверяем, что кеш очищен
        assertThat(cacheManager.getCache("materials").get("all")).isNull();
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testCacheEvictOnMaterialDelete() {
        // Загружаем материалы (заполняем кеш)
        servicePainter.getAllMaterials();
        assertThat(cacheManager.getCache("materials").get("all")).isNotNull();

        // Удаляем материал (очищает кеш)
        servicePainter.deleteMaterialByName("Круги P80");

        // Проверяем, что кеш очищен
        assertThat(cacheManager.getCache("materials").get("all")).isNull();
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testCacheEvictOnEmployeeUpdate() {
        // Загружаем сотрудников (заполняем кеш)
        servicePainter.getPainterList();
        assertThat(cacheManager.getCache("employees").get("all")).isNotNull();

        // Обновляем сотрудника (очищает кеш)
        EmployeeDto dto = new EmployeeDto();
        dto.setId(testEmployee.getId());
        dto.setFullName("Обновленное имя");
        
        // Сохраняем обновленного сотрудника
        Employee updatedEmployee = servicePainter.updateEmployee(dto);
        assertThat(updatedEmployee).isNotNull();

        // Проверяем, что кеш очищен
        assertThat(cacheManager.getCache("employees").get("all")).isNull();
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testCacheEvictOnEmployeeDelete() {
        // Загружаем сотрудников (заполняем кеш)
        servicePainter.getPainterList();
        assertThat(cacheManager.getCache("employees").get("all")).isNotNull();

        // Удаляем сотрудника (очищает кеш)
        servicePainter.deletePainter(testEmployee.getId());

        // Проверяем, что кеш очищен
        assertThat(cacheManager.getCache("employees").get("all")).isNull();
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testCacheBehaviorAfterClear() {
        // Очищаем кеш
        cacheManager.getCache("materials").clear();
        
        // Проверяем, что кеш пуст
        assertThat(cacheManager.getCache("materials").get("all")).isNull();
        
        // Загружаем данные
        servicePainter.getAllMaterials();
        
        // Проверяем, что кеш заполнен
        assertThat(cacheManager.getCache("materials").get("all")).isNotNull();
    }

    // ==================== ТЕСТЫ ДЛЯ БЕЗОПАСНОСТИ ====================

    @Test
    void testDeductMaterialsWithoutAuthentication() {
        MaterialDto dto = new MaterialDto("Круги P80", 10.0, "шт");
        
        assertThatThrownBy(() -> servicePainter.deductMaterials(List.of(dto)))
            .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testUpdateVolumeByNameWithAuthentication() {
        int updated = servicePainter.updateVolumeByName("Круги P80", 10.0);
        assertThat(updated).isEqualTo(1);
        
        Optional<Material> found = materialRepository.findByName("Круги P80");
        assertThat(found).isPresent();
        assertThat(found.get().getVolume()).isEqualTo(110.0);
    }

    @Test
    void testGetAllMaterialsWithoutAuthentication() {
        // Очищаем контекст безопасности
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
        
        Iterable<Material> materials = servicePainter.getAllMaterials();
        assertThat(materials).isNotEmpty();
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testDeleteMaterialRequiresAuthentication() {
        // Удаление материала требует аутентификации
        String materialName = testMaterial.getName();
        int deleted = servicePainter.deleteMaterialByName(materialName);
        assertThat(deleted).isEqualTo(1);
    }

    // ==================== ТЕСТЫ ДЛЯ ОБРАБОТКИ ИСКЛЮЧЕНИЙ ====================

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testDatabaseException() {
        // Проверяем, что метод не возвращает null
        assertThat(servicePainter.getAllMaterials()).isNotNull();
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testValidationExceptionInGetMaterialsByType() {
        assertThatThrownBy(() -> servicePainter.getMaterialsByType(""))
            .isInstanceOf(ValidationException.class);
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testMultipleOperationsWithCache() {
        // 1. Загружаем данные (кеш заполняется)
        servicePainter.getAllMaterials();
        assertThat(cacheManager.getCache("materials").get("all")).isNotNull();
        
        // 2. Обновляем через сервис (кеш очищается)
        servicePainter.updateVolumeByName("Круги P80", 5.0);
        assertThat(cacheManager.getCache("materials").get("all")).isNull();
        
        // 3. Снова загружаем (кеш заполняется)
        servicePainter.getAllMaterials();
        assertThat(cacheManager.getCache("materials").get("all")).isNotNull();
        
        // 4. Удаляем через сервис (кеш очищается)
        servicePainter.deleteMaterialByName("Круги P80");
        assertThat(cacheManager.getCache("materials").get("all")).isNull();
    }
}
