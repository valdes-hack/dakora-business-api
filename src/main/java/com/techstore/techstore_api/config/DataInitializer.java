package com.techstore.techstore_api.config;

import com.techstore.techstore_api.model.Role;
import com.techstore.techstore_api.model.User;
import com.techstore.techstore_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Order(3)
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        log.info("🚀 [INIT] Initialisation et vérification des comptes administrateurs...");

        createOrUpdateAdmin("admin@techstore.com", "Admin1234", "Admin", "Valdes");
        createOrUpdateAdmin("valdeshacking01@gmail.com", "Admin1234", "Valdes", "Hack");
    }

    private void createOrUpdateAdmin(String email, String rawPassword, String firstName, String lastName) {
        try {
            User user = userRepository.findByEmail(email).orElse(null);

            if (user == null) {
                user = User.builder()
                        .firstName(firstName)
                        .lastName(lastName)
                        .email(email)
                        .phone("+237670000000")
                        .password(passwordEncoder.encode(rawPassword))
                        .role(Role.ADMIN)
                        .isGuest(false)
                        .isVerified(true)
                        .isDeleted(false)
                        .createdAt(LocalDateTime.now())
                        .build();
                userRepository.save(user);
                log.info("✅ [INIT] Compte Administrateur {} créé avec succès !", email);
            } else {
                user.setRole(Role.ADMIN);
                user.setPassword(passwordEncoder.encode(rawPassword));
                user.setVerified(true);
                user.setGuest(false);
                user.setDeleted(false);
                userRepository.save(user);
                log.info("✅ [INIT] Compte {} mis à jour en ROLE_ADMIN avec mot de passe opérationnel !", email);
            }
        } catch (Exception e) {
            log.error("❌ [INIT] Erreur configuration compte {}: {}", email, e.getMessage());
        }
    }
}
