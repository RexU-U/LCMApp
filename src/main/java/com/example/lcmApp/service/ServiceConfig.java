package com.example.lcmApp.service;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.*;
import org.springframework.context.annotation.*;
import com.example.lcmApp.repository.*;
import com.example.lcmApp.logger.MaterialLogger;
import jakarta.persistence.EntityManager;

@Configuration
public class ServiceConfig {
    
    @Bean
    @Primary
    public ServicePainter createServicePainter(RepositoryLcmMaterial repository, RepositoryLcmIssue repositoryIssue, MaterialLogger materialLogger, EmployeeRepository employeeRepository, RoleRepository roleRepository) {
        return new ServicePainter(repository, repositoryIssue, materialLogger, employeeRepository, roleRepository);
  
    }
    
    @Bean
    public ReportService reportService() {
        return new ReportService();
    }
}