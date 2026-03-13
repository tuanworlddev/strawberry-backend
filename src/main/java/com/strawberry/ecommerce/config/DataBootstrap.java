package com.strawberry.ecommerce.config;

import com.strawberry.ecommerce.user.entity.Role;
import com.strawberry.ecommerce.user.entity.User;
import com.strawberry.ecommerce.user.entity.UserStatus;
import com.strawberry.ecommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataBootstrap implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        String adminEmail = "admin_new@strawberry.com";
        Optional<User> adminOptional = userRepository.findByEmail(adminEmail);
        
        if (adminOptional.isEmpty()) {
            log.info("Creating default admin user: {}", adminEmail);
            User user = new User();
            user.setEmail(adminEmail);
            user.setPassword(passwordEncoder.encode("admin-password"));
            user.setRole(Role.ADMIN);
            user.setStatus(UserStatus.ACTIVE);
            userRepository.save(user);
        } else {
            log.info("Updating admin user password for verification: {}", adminEmail);
            User user = adminOptional.get();
            user.setPassword(passwordEncoder.encode("admin-password"));
            userRepository.save(user);
        }

        String sellerEmail = "seller@example.com";
        Optional<User> sellerOptional = userRepository.findByEmail(sellerEmail);
        if (sellerOptional.isPresent()) {
            User seller = sellerOptional.get();
            seller.setPassword(passwordEncoder.encode("password123"));
            userRepository.save(seller);
        }
    }
}
