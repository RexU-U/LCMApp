package com.example.lcmApp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.main.allow-bean-definition-overriding=true",
    "spring.boot.test.mockito.enabled=false"  // Отключаем Mockito
})
class LcmAppApplicationTests {

	@Test
	void contextLoads() {
		System.out.println("✅ Application context loaded successfully!");
	}
}