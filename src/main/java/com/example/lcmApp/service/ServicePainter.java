package com.example.lcmApp.service;

import com.example.lcmApp.repository.*;
import com.example.lcmApp.entity.*;
import com.example.lcmApp.dto.*;
import com.example.lcmApp.exception.*;
import com.example.lcmApp.logger.MaterialLogger;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.time.Instant;

@Service
public class ServicePainter {

    private final RepositoryLcmMaterial repository;
    private final RepositoryLcmIssue repositoryIssue;
    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final MaterialLogger materialLogger;
    
    @Autowired
    public ServicePainter(RepositoryLcmMaterial repository,
                         RepositoryLcmIssue repositoryIssue,
                         MaterialLogger materialLogger,
                         EmployeeRepository employeeRepository,
                         RoleRepository roleRepository) {
        
        this.repository = repository;
        this.repositoryIssue = repositoryIssue;
        this.employeeRepository = employeeRepository;
        this.roleRepository = roleRepository;
        this.materialLogger = materialLogger;
    }

   
    @Cacheable(value = "materials", key = "'all'", unless = "#result == null")
    public Iterable<Material> getAllMaterials() {
        System.out.println("[CACHE] Загрузка всех материалов из БД...");
        Iterable<Material> materials = repository.findAll();
        if (materials == null) {
            throw new DatabaseException("Failed to fetch materials");
        }
        return materials;
    }

    public Optional<Material> getMaterialByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Material name cannot be empty");
        }
        
        Optional<Material> material = repository.findByName(name);
        if (!material.isPresent()) {
            throw new ResourceNotFoundException("Material not found: " + name);
        }
        return material;
    }

    public Iterable<MaterialDto> getMaterialsByType(String type) {
        if (type == null || type.trim().isEmpty()) {
            throw new ValidationException("Category type cannot be empty");
        }
        
        List<Material> allMaterials = repository.findAll();
        
        List<MaterialDto> result = allMaterials.stream()
            .filter(m -> Objects.equals(m.getType(), type))
            .map(m -> new MaterialDto(
                m.getName(),
                m.getVolume(),
                m.getUnit()
            ))
            .toList();
        
        if (result.isEmpty()) {
            throw new ResourceNotFoundException("Materials not found for type: " + type);
        }
        
        return result;
    }

    @Cacheable(value = "employees", key = "'all'", unless = "#result == null || #result.isEmpty()")
    public List<Employee> getPainterList() {
        System.out.println("[CACHE] Загрузка всех сотрудников из БД...");
        List<Employee> painters = employeeRepository.findAll();
        if (painters == null) {
            throw new DatabaseException("Failed to fetch painters");
        }
        return painters;
    }

    public Optional<Employee> getEmployeeById(Long id) {
        if (id == null || id == 0) {
            throw new ValidationException("Employee ID cannot be empty");
        }
        
        Optional<Employee> employee = employeeRepository.findById(id);
        if (!employee.isPresent()) {
            throw new ResourceNotFoundException("Employee not found with id: " + id);
        }
        return employee;
    }
        
        
    @CacheEvict(value = {"materials"}, allEntries = true)
    @Transactional
    public int updateVolumeByName(String name, Double volume) {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Material name cannot be empty");
        }
        if (volume == null || volume <= 0) {
            throw new BusinessLogicException("Volume must be positive");
        }
        
        Optional<Material> existing = repository.findByName(name);
        if (!existing.isPresent()) {
            throw new ResourceNotFoundException("Material not found: " + name);
        }
        
        int updated = repository.updateVolumeByName(name, volume);
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Authentication required");
        }
        String user = authentication.getName();
        String unit = existing.get().getUnit();
        
        if (materialLogger != null) {
            materialLogger.logMaterialAddition(
                name,
                volume,
                unit,
                user
            );
        }
        if (updated == 0) {
            throw new BusinessLogicException("Failed to update volume for material: " + name);
        }
        
        return updated;
    }

    @CacheEvict(value = {"materials", "employees"}, allEntries = true)
    @Transactional
    public void deductMaterials(List<MaterialDto> materialPosts) {
        if (materialPosts == null || materialPosts.isEmpty()) {
            throw new ValidationException("Material list cannot be empty");
        }
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Authentication required");
        }

        String employeeName = authentication.getName();
        Employee employee = employeeRepository.findByFullName(employeeName)
            .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + employeeName));

        List<LcmIssue> issuesToSave = new ArrayList<>();

        for (MaterialDto dto : materialPosts) {
            if (dto.getName() == null || dto.getName().trim().isEmpty()) {
                throw new ValidationException("Material name cannot be empty");
            }
            if (dto.getVolume() == null || dto.getVolume() <= 0) {
                throw new BusinessLogicException("Volume must be positive for material: " + dto.getName());
            }
            if (dto.getUnit() == null || dto.getUnit().trim().isEmpty()) {
                throw new ValidationException("Unit cannot be empty for material: " + dto.getName());
            }
            
            Material material = repository.findByName(dto.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Material not found: " + dto.getName()));

            if (material.getVolume() == null || material.getVolume() < dto.getVolume()) {
                throw new InsufficientStockException(
                    "Не достаточно материала на складе: " + material.getName() + 
                    ". имееться: " + (material.getVolume() != null ? material.getVolume() : 0) + 
                    ", требуеться: " + dto.getVolume()
                );
            }

            material.setVolume(material.getVolume() - dto.getVolume());
            repository.save(material);

            LcmIssue issue = new LcmIssue(
                dto.getVolume(),
                Instant.now(),
                material,
                employee
            );
            issuesToSave.add(issue);

            if (materialLogger != null) {
                String user = authentication.getName();
                materialLogger.logMaterialWriteOff(
                    dto.getName(),
                    dto.getVolume(),
                    dto.getUnit(),
                    user
                );
            }
        }

        repositoryIssue.saveAll(issuesToSave);
    }

    @CacheEvict(value = {"materials"}, allEntries = true)
    public void updateMaterial(Material material) {
        if (material == null) {
            throw new ValidationException("Material cannot be null");
        }
        if (material.getName() == null || material.getName().trim().isEmpty()) {
            throw new ValidationException("Material name cannot be empty");
        }
        
        Optional<Material> existing = repository.findByName(material.getName());
        if (!existing.isPresent()) {
            throw new ResourceNotFoundException("Material not found: " + material.getName());
        }
        
        int updated = repository.updateMaterial(
            material.getMaterial_id(),
            material.getName(),
            material.getDescription(),
            material.getVolume(),
            material.getUnit(),
            material.getType(),
            material.getPriority(),
            material.getInventory()
        );
        
        if (updated == 0) {
            throw new BusinessLogicException("Failed to update material: " + material.getName());
        }
    }

    @CacheEvict(value = {"materials"}, allEntries = true)
    @Transactional
    public void insertMaterial(Material material) {
        if (material == null) {
            throw new ValidationException("Material cannot be null");
        }
        if (material.getName() == null || material.getName().trim().isEmpty()) {
            throw new ValidationException("Material name cannot be empty");
        }
        if (material.getVolume() == null || material.getVolume() < 0) {
            throw new ValidationException("Material volume cannot be negative");
        }
        if (material.getUnit() == null || material.getUnit().trim().isEmpty()) {
            throw new ValidationException("Material unit cannot be empty");
        }
        if (material.getType() == null || material.getType().trim().isEmpty()) {
            throw new ValidationException("Material type cannot be empty");
        }
        if (material.getPriority() == null || material.getPriority() < 0) {
            throw new ValidationException("Material priority cannot be negative");
        }
        
        Optional<Material> existing = repository.findByName(material.getName());
        if (existing.isPresent()) {
            throw new ResourceAlreadyExistsException("Material already exists: " + material.getName());
        }
        
        repository.save(material);
    }

    @CacheEvict(value = {"materials"}, allEntries = true)
    @Transactional
    public int deleteMaterialByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Material name cannot be empty");
        }
        
        Optional<Material> existing = repository.findByName(name);
        if (!existing.isPresent()) {
            throw new ResourceNotFoundException("Material not found: " + name);
        }
        
        int deleted = repository.deleteByName(name);
        if (deleted == 0) {
            throw new BusinessLogicException("Failed to delete material: " + name);
        }
        
        return deleted;
    }

    @CacheEvict(value = {"employees"}, allEntries = true)
    @Transactional
    public void deletePainter(Long id) {
        if (id == null || id == 0){
            throw new ValidationException("Employee ID cannot be empty");
        }
        
        Optional<Employee> existing = employeeRepository.findById(id);
        if (!existing.isPresent()) {
            throw new ResourceNotFoundException("Employee not found with id: " + id);
        }
        
        employeeRepository.deleteById(id);
    }

    @CacheEvict(value = {"employees"}, allEntries = true)
    @Transactional
    public Employee updateEmployee(EmployeeDto employeeDto) {
        if (employeeDto == null) {
            throw new ValidationException("Employee cannot be null");
        }
        if (employeeDto.getFullName() == null || employeeDto.getFullName().trim().isEmpty()) {
            throw new ValidationException("Employee full name cannot be empty");
        }
        if (employeeDto.getId() == null) {
            throw new ValidationException("Employee ID cannot be empty");
        }
        
        Employee existing = employeeRepository.findById(employeeDto.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeDto.getId()));
        
        existing.setFullName(employeeDto.getFullName());
        
        Employee saved = employeeRepository.save(existing);
        if (saved == null) {
            throw new BusinessLogicException("Failed to update employee: " + employeeDto.getFullName());
        }
        
        return saved;
    }

   
}
