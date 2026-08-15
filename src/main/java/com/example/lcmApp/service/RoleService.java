package com.example.lcmApp.service;

import org.springframework.stereotype.Service;
import com.example.lcmApp.repository.RoleRepository;
import com.example.lcmApp.entity.Role;
import java.util.Optional;

@Service
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public Role findOrCreateRole(String rawName) {
        
             String name = rawName.toUpperCase();
        if (!name.startsWith("ROLE_")) {
            name = "ROLE_" + name;
        }

        Optional<Role> existing = roleRepository.findByName(name);

        if (existing.isPresent()) {
            return existing.get();
        }

        Role newRole = new Role();
        newRole.setName(name); 
        
        return roleRepository.save(newRole);
    }
}
