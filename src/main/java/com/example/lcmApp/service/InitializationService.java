package com.example.lcmApp.service;

import com.example.lcmApp.entity.Role;
import com.example.lcmApp.entity.Material;
import com.example.lcmApp.repository.RoleRepository;
import com.example.lcmApp.repository.RepositoryLcmMaterial;
import com.example.lcmApp.util.InitialDatabaseUtilites;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InitializationService {

    private final RoleRepository roleRepository;
    private final RepositoryLcmMaterial materialRepository;

    @Autowired
    public InitializationService(RoleRepository roleRepository,
                                 RepositoryLcmMaterial materialRepository) {
        this.roleRepository = roleRepository;
        this.materialRepository = materialRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeDatabase() {
        System.out.println("Starting database initialization...");
        
        try {
            // Создать роли
            if (roleRepository.count() == 0) {
                for (Role role : InitialDatabaseUtilites.createRoles()) {
                    roleRepository.save(role);
                }
                System.out.println("Roles created: ADMIN, USER");
            } else {
                System.out.println("Roles already exist");
            }

            // Создать материалы
            if (materialRepository.count() == 0) {
                for (Material material : InitialDatabaseUtilites.createMaterials()) {
                    materialRepository.save(material);
                }
                System.out.println("Materials initialized");
            } else {
                System.out.println("Materials already exist");
            }
            
            System.out.println("Database initialization completed!");
        } catch (Exception e) {
            System.err.println("Error during initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
