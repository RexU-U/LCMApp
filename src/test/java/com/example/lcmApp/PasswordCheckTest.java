package com.example.lcmApp;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordCheckTest {

    @Test
    public void testPasswordMatch() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // Проверяем хеш для "123456"
        String hash = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E";
        boolean matches123456 = encoder.matches("123456", hash);
        boolean matchesAdmin = encoder.matches("admin", hash);
        
        System.out.println("Hash: " + hash);
        System.out.println("Matches '123456': " + matches123456);
        System.out.println("Matches 'admin': " + matchesAdmin);
        
        // Если не совпадает - генерируем новый хеш
        String newHash123456 = encoder.encode("123456");
        String newHashAdmin = encoder.encode("admin");
        System.out.println("\nNew hash for '123456': " + newHash123456);
        System.out.println("New hash for 'admin': " + newHashAdmin);
    }
}
