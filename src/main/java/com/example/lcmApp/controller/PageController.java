package com.example.lcmApp.controller;

import com.example.lcmApp.entity.Employee;
import com.example.lcmApp.entity.Material;
import com.example.lcmApp.dto.LogEntry;
import com.example.lcmApp.logger.MaterialLogger;
import com.example.lcmApp.service.ServicePainter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.Collection;

@Controller
public class PageController {

    @Autowired
    private ServicePainter service;

    @GetMapping({"/", "/index"})
    public String index(Model model) {
        
        List<Material> materials = castIterableToList(service.getAllMaterials());
          // Категории (уникальные типы)
        List<String> categories = materials.stream()
                .map(Material::getType)
                .distinct()
                .collect(Collectors.toList());
        
        model.addAttribute("categories", categories);
        model.addAttribute("materials", materials);
        return "index";
    }

    @GetMapping("/admin")
public String admin(Model model, HttpServletRequest request, Authentication authentication) {
    List<Material> materials = castIterableToList(service.getAllMaterials());
    List<Employee> employees = castIterableToList(service.getPainterList());
    List<LogEntry> logs;
    try {
        logs = MaterialLogger.readAllLogs();
    } catch (IOException e) {
        logs = List.of();
    }
    
    boolean isAdmin = authentication.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

    if (!isAdmin) {
        return "redirect:/index"; 
    }
    
    String jwtToken = null;
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
        for (Cookie c : cookies) {
            if ("JWT_TOKEN".equals(c.getName())) {
                jwtToken = c.getValue();
                break;
            }
        }
    }
    model.addAttribute("jwtToken", jwtToken);
    model.addAttribute("materials", materials);
    model.addAttribute("employees", employees);
    model.addAttribute("logs", logs);
    return "admin";
}
    
    private <T> List<T> castIterableToList(Iterable<T> iterable) {
        List<T> list = new ArrayList<T>();
        iterable.forEach(list::add);
        return list;
    }
}