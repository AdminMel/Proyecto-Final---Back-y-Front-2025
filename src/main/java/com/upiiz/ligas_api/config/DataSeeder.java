package com.upiiz.ligas_api.config;

import com.upiiz.ligas_api.entity.Role;
import com.upiiz.ligas_api.entity.User;
import com.upiiz.ligas_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedAdmin(
            UserRepository userRepo,
            PasswordEncoder encoder,
            @Value("${app.seed.admin.email:admin@upiiz.com}") String adminEmail,
            @Value("${app.seed.admin.password:Admin12345}") String adminPassword,
            @Value("${app.seed.admin.nombre:ADMIN}") String adminNombre
    ) {
        return args -> {
            String email = adminEmail.toLowerCase().trim();

            if (!userRepo.existsByEmail(email)) {
                User admin = User.builder()
                        .email(email)
                        .nombre(adminNombre)
                        .password(encoder.encode(adminPassword))
                        .roles(Set.of(Role.ROLE_ADMIN, Role.ROLE_USER))
                        .build();
                userRepo.save(admin);
                System.out.println("[SEED] ADMIN creado: " + email);
            } else {
                System.out.println("[SEED] ADMIN ya existe: " + email);
            }
        };
    }
}
