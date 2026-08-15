package com.example.lcmApp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;// ← импорт
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.example.lcmApp.repository") // сканируем репозитории
@EntityScan(basePackages = "com.example.lcmApp.entity")                // сканируем @Entity‑классы
public class LcmApp {

    public static void main(String[] args) {
        SpringApplication.run(LcmApp.class, args);
    }
}
