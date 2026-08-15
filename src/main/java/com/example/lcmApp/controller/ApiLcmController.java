package com.example.lcmApp.controller;

import com.example.lcmApp.service.ServicePainter;
import com.example.lcmApp.service.ReportService;
import com.example.lcmApp.entity.*;
import com.example.lcmApp.dto.*;
import com.example.lcmApp.exception.*;
import com.example.lcmApp.logger.MaterialLogger;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.bind.annotation.CrossOrigin;
import java.util.*;
import java.io.IOException;

@RestController
@RequestMapping("/lcm")
@CrossOrigin(origins = "*")
public class ApiLcmController {
    
    private final ServicePainter service;
    
    private final ReportService reportService;
    
    @Autowired
    public ApiLcmController(ServicePainter service, ReportService reportService) {
        this.service = service;
        this.reportService = reportService;
    }
    
    @GetMapping("/all")
    public ResponseEntity<List<Material>> getMaterials() {
        List<Material> materials = new ArrayList<>();
        service.getAllMaterials().forEach(materials::add);
        
        if (materials.isEmpty()) {
            throw new ResourceNotFoundException("Materials not found");
        }
        
        return ResponseEntity.ok()
            .header("Cache-Control", "no-cache, no-store, must-revalidate")
            .body(materials);
    }
    
    @GetMapping("/category") //Получить категории материала
    public ResponseEntity<List<Category>> getCategory() {
        List<Material> materials = new ArrayList<>();
        service.getAllMaterials().forEach(materials::add);
        
        if (materials.isEmpty()) {
            throw new ResourceNotFoundException("Categories not found");
        }
        
        Set<String> uniqueTypes = new HashSet<>();
        List<Category> categories = new ArrayList<>();
        
        for (Material m : materials) {
            if (uniqueTypes.add(m.getType())) {
                categories.add(new Category(m.getMaterial_id(), m.getType()));
            }
        }
        
        return ResponseEntity.ok()
            .header("Cache-Control", "no-cache, no-store, must-revalidate")
            .body(categories);
    }
    
    @GetMapping("/materials") //Получить материал по категориям
    public ResponseEntity<List<MaterialDto>> getMaterialByCategory(
            @RequestParam(required = true) String type) {
        
        if (type == null || type.trim().isEmpty()) {
            throw new ValidationException("Category type cannot be empty");
        }
        
        List<MaterialDto> materials = new ArrayList<>();
        service.getMaterialsByType(type).forEach(materials::add);
        
        if (materials.isEmpty()) {
            throw new ResourceNotFoundException("Materials not found for type: " + type);
        }
        
        return ResponseEntity.ok()
            .header("Cache-Control", "no-cache, no-store, must-revalidate")
            .body(materials);
    }
    
    @PostMapping("/materials/update") //обновить список материала
    public ResponseEntity<Void> updateMaterialList(@RequestBody List<MaterialDto> materials) {
        if (materials == null || materials.isEmpty()) {
            throw new ValidationException("Material list cannot be empty");
        }
        
        for (MaterialDto dto : materials) {
            if (dto.getName() == null || dto.getName().trim().isEmpty()) {
                throw new ValidationException("Material name cannot be empty");
            }
            if (dto.getVolume() == null || dto.getVolume() <= 0) {
                throw new BusinessLogicException("Invalid volume for material: " + dto.getName());
            }
            if (dto.getUnit() == null || dto.getUnit().trim().isEmpty()) {
                throw new ValidationException("Unit cannot be empty for material: " + dto.getName());
            }
        }
        
        service.deductMaterials(materials);
        
        return ResponseEntity.ok()
            .header("Cache-Control", "no-cache")
            .build();
    }
    
    @PostMapping("/material/alter") //Изменить метаданные материала
    public ResponseEntity<Void> materialAlter(@RequestBody Material material) {
        if (material == null) {
            throw new ValidationException("Material cannot be null");
        }
        if (material.getMaterial_id() == null || material.getMaterial_id() == 0L) {
            throw new ValidationException("Material not found");
        }
        
        service.updateMaterial(material);
        
        return ResponseEntity.ok()
            .header("Cache-Control", "no-cache")
            .build();
    }
    
    @PostMapping("/material/insert") //Добавить материал
    public ResponseEntity<Void> materialInsert(@RequestBody Material material) {
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
        
        service.insertMaterial(material);
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .header("Cache-Control", "no-cache")
            .build();
    }
    
    @DeleteMapping("/material/delete") //Удалить материал
    public ResponseEntity<Void> deleteMaterial(@RequestParam String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Material name cannot be empty");
        }
        
        service.deleteMaterialByName(name);
        
        return ResponseEntity.ok()
            .header("Cache-Control", "no-cache")
            .build();
    }
    
    @GetMapping("/material/estimate") //Получить остатки материала из базы данных с колличеством материала меньше минимального
    public ResponseEntity<List<Material>> getEstimate() {
        List<Material> estimate = new ArrayList<>();
        service.getAllMaterials().forEach(m -> {
            if (m.getVolume() != null && m.getInventory() != null && 
                m.getVolume() <= m.getInventory()) {
                estimate.add(m);
            }
        });
        
        if (estimate.isEmpty()) {
            throw new ResourceNotFoundException("No materials for estimate");
        }
        
        return ResponseEntity.ok()
            .header("Cache-Control", "no-cache, no-store, must-revalidate")
            .body(estimate);
    }
    
    @PostMapping("/material/addMaterials") //Добавить материал на склад
    public ResponseEntity<Void> addMaterialsToStock(@RequestBody List<MaterialDto> materials) {
        if (materials == null || materials.isEmpty()) {
            throw new ValidationException("Materials list cannot be empty");
        }
        
        for (MaterialDto m : materials) {
            if (m.getName() == null || m.getName().trim().isEmpty()) {
                throw new ValidationException("Material name cannot be empty");
            }
            if (m.getVolume() == null || m.getVolume() <= 0) {
                throw new BusinessLogicException("Invalid volume for material: " + m.getName());
            }
            
            service.updateVolumeByName(m.getName(), m.getVolume());
        }
        
        return ResponseEntity.ok()
            .header("Cache-Control", "no-cache")
            .build();
    }
    
    @GetMapping("/painters") //Пооучить список маляров из базы данных
    public ResponseEntity<List<Employee>> getPainters() {
        List<Employee> painters = service.getPainterList();
        
        if (painters.isEmpty()) {
            throw new ResourceNotFoundException("Painters not found");
        }
        
        return ResponseEntity.ok()
            .header("Cache-Control", "no-cache, no-store, must-revalidate")
            .body(painters);
    }
    
    @GetMapping("/painter/{id}") //Получить маляра по id
    public ResponseEntity<Employee> getPainter(@PathVariable Long id) {
        if (id == null || id == 0) {
            throw new ValidationException("Painter ID cannot be empty");
        }
        
        Employee painter = service.getEmployeeById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Painter not found with id: " + id));
        
        return ResponseEntity.ok()
            .header("Cache-Control", "no-cache, no-store, must-revalidate")
            .body(painter);
    }
    
    
    @DeleteMapping("/painter/{id}") //Удалить маляра по id
    public ResponseEntity<Void> deletePainter(@PathVariable Long id) {
        if (id == null || id == 0) {
            throw new ValidationException("Painter ID cannot be empty");
        }
        
        service.deletePainter(id);
        
        return ResponseEntity.ok()
            .header("Cache-Control", "no-cache")
            .build();
    }
    
    @PostMapping("/painter") //Обновить данные о маляре в базе данных
    public ResponseEntity<?> updatePainter(@RequestBody EmployeeDto employeeDto) {
        if (employeeDto == null) {
            throw new ValidationException("Employee cannot be null");
        }
        if (employeeDto.getFullName() == null || employeeDto.getFullName().trim().isEmpty()) {
            throw new ValidationException("Employee full name cannot be empty");
        }
        
        Employee updated = service.updateEmployee(employeeDto);
        
        return ResponseEntity.ok()
            .header("Cache-Control", "no-cache")
            .body(updated);
    }
    
    @GetMapping("/logs/all") //Получить все логи из файла
    public ResponseEntity<List<LogEntry>> getAllLogs() throws IOException {
        List<LogEntry> logs = MaterialLogger.readAllLogs();
        
        if (logs.isEmpty()) {
            throw new ResourceNotFoundException("Logs not found");
        }
        
        return ResponseEntity.ok()
            .header("Cache-Control", "no-cache, no-store, must-revalidate")
            .body(logs);
    }
    
    @DeleteMapping("/logs/all") //Удалить все логи из файла
    public ResponseEntity<Void> deleteAllLogs() {
        MaterialLogger.clearLogs();
        return ResponseEntity.ok()
            .header("Cache-Control", "no-cache")
            .build();
    }
    
    @PostMapping("/report/materials") //Получить отчет на закупку материала в формате pdf
public ResponseEntity<byte[]> getMaterialReport(@RequestBody List<MaterialDto> materials) {
    
    byte[] pdfBytes = reportService.generateMaterialReport(materials);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_PDF);
    headers.setContentDispositionFormData("attachment", "materials_report.pdf");

    return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

}
