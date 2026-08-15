package com.example.lcmApp.security;

import com.example.lcmApp.entity.Employee;
import com.example.lcmApp.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.lcmApp.entity.Role;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final EmployeeRepository employeeRepository;

   @Override
public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    log.info(">>> ЗАПРОС НА АВТОРИЗАЦИЮ: ищем пользователя с именем: {}", username);

    Employee employee = employeeRepository.findByFullName(username)
        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

    var rolesList = employee.getRoles();
    log.info(">>> ПОЛЬЗОВАТЕЛЬ НАЙДЕН: fullName={}, rolesList size={}", employee.getFullName(), rolesList != null ? rolesList.size() : 0);

    List<SimpleGrantedAuthority> authorities;

    if (rolesList == null || rolesList.isEmpty()) {
        log.error(">>> КРИТИЧЕСКАЯ ОШИБКА: У ПОЛЬЗОВАТЕЛЯ НЕТ РОЛЕЙ! Вернём пустой список, будет 403.");
        authorities = List.of();
    } else {
        authorities = rolesList.stream()
            .map(r -> {
                String roleName = r.getName();
                // Явно добавляем префикс
                String authority = "ROLE_" + roleName;
                log.info(">>> ОБРАБОТКА РОЛИ: name='{}' -> полномочие '{}'", roleName, authority);
                return new SimpleGrantedAuthority(authority);
            })
            .collect(Collectors.toList());
    }

    return org.springframework.security.core.userdetails.User.builder()
        .username(employee.getFullName())
        .password(employee.getPassword())
        .authorities(authorities)
        .build();
}
}
