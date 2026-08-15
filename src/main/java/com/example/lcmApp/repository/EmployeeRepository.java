package com.example.lcmApp.repository;

import com.example.lcmApp.entity.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.lang.Iterable;
import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, String> {
    
    Optional<Employee> findByFullName(String fullName);
    Optional<Employee> findById(Long id);
    List<Employee> findAll();
    void deleteById(Long id);
    Employee save(Employee employee);
    
}