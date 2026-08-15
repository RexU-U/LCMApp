package com.example.lcmApp.service;

import com.example.lcmApp.entity.Employee;
import com.example.lcmApp.entity.Role;
import com.example.lcmApp.dto.EmployeeDto;
import com.example.lcmApp.repository.EmployeeRepository;
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
import java.util.Set;

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
class EmployeeCacheTest {

    @Autowired
    private ServicePainter servicePainter;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CacheManager cacheManager;

    private Employee testEmployee;
    private Role userRole;

    @BeforeEach
    void setUp() {
        cacheManager.getCache("employees").clear();
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
        testEmployee.setFullName("Тестовый сотрудник");
        testEmployee.setPassword("password");
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        testEmployee.setRoles(roles);
        employeeRepository.save(testEmployee);
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testCacheEmployees() {
        cacheManager.getCache("employees").clear();

        long start1 = System.currentTimeMillis();
        List<Employee> employees1 = servicePainter.getPainterList();
        long time1 = System.currentTimeMillis() - start1;

        long start2 = System.currentTimeMillis();
        List<Employee> employees2 = servicePainter.getPainterList();
        long time2 = System.currentTimeMillis() - start2;

        assertThat(employees1).isNotEmpty();
        assertThat(employees2).isNotEmpty();
        assertThat(employees1.size()).isEqualTo(employees2.size());
        assertThat(time2).isLessThanOrEqualTo(time1 + 100);
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testCacheEvictOnEmployeeUpdate() {
        cacheManager.getCache("employees").clear();
        servicePainter.getPainterList();
        assertThat(cacheManager.getCache("employees").get("all")).isNotNull();

        List<Employee> employees = employeeRepository.findAll();
        assertThat(employees).isNotEmpty();
        
        Employee employee = employees.get(0);
        EmployeeDto dto = new EmployeeDto();
        dto.setId(employee.getId());
        dto.setFullName("Обновленное имя");
        
        // Обновляем сотрудника
        Employee updated = servicePainter.updateEmployee(dto);
        assertThat(updated).isNotNull();
        assertThat(updated.getFullName()).isEqualTo("Обновленное имя");

        assertThat(cacheManager.getCache("employees").get("all")).isNull();
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testCacheEvictOnEmployeeDelete() {
        cacheManager.getCache("employees").clear();
        servicePainter.getPainterList();
        assertThat(cacheManager.getCache("employees").get("all")).isNotNull();

        List<Employee> employees = employeeRepository.findAll();
        assertThat(employees).isNotEmpty();
        
        Employee employee = employees.get(0);
        servicePainter.deletePainter(employee.getId());

        assertThat(cacheManager.getCache("employees").get("all")).isNull();
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testCacheMissAfterClear() {
        cacheManager.getCache("employees").clear();
        
        assertThat(cacheManager.getCache("employees").get("all")).isNull();
        
        servicePainter.getPainterList();
        
        assertThat(cacheManager.getCache("employees").get("all")).isNotNull();
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testCacheConsistency() {
        cacheManager.getCache("employees").clear();
        
        List<Employee> employees1 = servicePainter.getPainterList();
        List<Employee> employees2 = servicePainter.getPainterList();
        
        assertThat(employees1.size()).isEqualTo(employees2.size());
        for (int i = 0; i < employees1.size(); i++) {
            assertThat(employees1.get(i).getFullName()).isEqualTo(employees2.get(i).getFullName());
        }
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testCacheBehaviorWithMultipleOperations() {
        cacheManager.getCache("employees").clear();
        
        // 1. Загружаем данные (кеш заполняется)
        servicePainter.getPainterList();
        assertThat(cacheManager.getCache("employees").get("all")).isNotNull();
        
        // 2. Обновляем сотрудника (кеш очищается)
        EmployeeDto dto = new EmployeeDto();
        dto.setId(testEmployee.getId());
        dto.setFullName("Новое имя");
        servicePainter.updateEmployee(dto);
        assertThat(cacheManager.getCache("employees").get("all")).isNull();
        
        // 3. Снова загружаем данные (кеш заполняется)
        servicePainter.getPainterList();
        assertThat(cacheManager.getCache("employees").get("all")).isNotNull();
        
        // 4. Удаляем сотрудника (кеш очищается)
        servicePainter.deletePainter(testEmployee.getId());
        assertThat(cacheManager.getCache("employees").get("all")).isNull();
    }
}