package ru.mgubina.booking_service.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.mgubina.booking_service.entity.User;
import ru.mgubina.booking_service.repository.UserRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class BootstrapConfig implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        String adminUsername = "admin";
        String adminPassword = "admin";

        if (userRepository.findByUsername(adminUsername).isEmpty()) {
            userRepository.save(User.builder()
                    .username(adminUsername)
                    .password(passwordEncoder.encode(adminPassword))
                    .role(User.Role.ADMIN)
                    .build());
            log.info("Bootstrap ADMIN user created: {}", adminUsername);
        } else {
            log.info("Bootstrap ADMIN user already exists: {}", adminUsername);
        }

        String username = "user";
        String password = "pass";
        if (userRepository.findByUsername(username).isEmpty()) {
            userRepository.save(User.builder()
                    .username(adminUsername)
                    .password(passwordEncoder.encode(adminPassword))
                    .role(User.Role.ADMIN)
                    .build());
            log.info("Bootstrap TEST user created: {} / {}", username, password);
        } else {
            log.info("Bootstrap TEST user already exists: {}", username);
        }
    }
}
