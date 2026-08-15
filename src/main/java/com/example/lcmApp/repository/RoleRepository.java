package com.example.lcmApp.repository;


import com.example.lcmApp.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, String> {
    Optional<Role> findByName(String name);
    Role save(Role role);
}
