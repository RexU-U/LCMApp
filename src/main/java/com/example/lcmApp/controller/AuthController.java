package com.example.lcmApp.controller;

import com.example.lcmApp.dto.LoginRequest;
import com.example.lcmApp.dto.RegisterRequest;
import com.example.lcmApp.entity.Employee;
import com.example.lcmApp.entity.Role;
import com.example.lcmApp.repository.EmployeeRepository;
import com.example.lcmApp.repository.RoleRepository;
import com.example.lcmApp.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;

    private void setJwtCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("JWT_TOKEN", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(cookie);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(
            @RequestBody LoginRequest req,
            HttpServletResponse response) {

        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(auth);

        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        String token = jwtService.generateToken(userDetails);

        setJwtCookie(response, token);

        String role = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.equals("ROLE_ADMIN"))
                .findFirst()
                .orElse("ROLE_USER");

        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("token", token);
        responseBody.put("username", req.getUsername());
        responseBody.put("role", role);
        return ResponseEntity.ok(responseBody);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody RegisterRequest req,
            HttpServletResponse response) {

        if (employeeRepository.findByFullName(req.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Пользователь уже существует");
        }

        Employee employee = new Employee();
        employee.setFullName(req.getUsername());
        employee.setPassword(passwordEncoder.encode(req.getPassword()));

        Role userRole = roleRepository.findByName("USER")
                .orElseGet(() -> {
                    Role newRole = new Role("USER");
                    return roleRepository.save(newRole);
                });

        employee.setRoles(Set.of(userRole));
        employeeRepository.save(employee);

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword());
        Authentication auth = authManager.authenticate(authToken);
        SecurityContextHolder.getContext().setAuthentication(auth);

        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        String token = jwtService.generateToken(userDetails);

        setJwtCookie(response, token);

        String role = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .findFirst()
                .orElse("ROLE_USER");

        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("token", token);
        responseBody.put("username", req.getUsername());
        responseBody.put("role", role);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);
    }

    @PostMapping("/admin/register")
    public ResponseEntity<?> registerAdmin(
            @RequestBody RegisterRequest req,
            HttpServletResponse response) {

        if (employeeRepository.findByFullName(req.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Пользователь уже существует");
        }

        Employee employee = new Employee();
        employee.setFullName(req.getUsername());
        employee.setPassword(passwordEncoder.encode(req.getPassword()));

        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseGet(() -> {
                    Role newRole = new Role("ADMIN");
                    return roleRepository.save(newRole);
                });

        employee.setRoles(Set.of(adminRole));
        employeeRepository.save(employee);

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword());
        Authentication auth = authManager.authenticate(authToken);
        SecurityContextHolder.getContext().setAuthentication(auth);

        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        String token = jwtService.generateToken(userDetails);

        setJwtCookie(response, token);

        String role = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .findFirst()
                .orElse("ROLE_ADMIN");

        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("token", token);
        responseBody.put("username", req.getUsername());
        responseBody.put("role", role);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);
    }
}
