package com.farmchainx.farmchainx.configuration;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import com.farmchainx.farmchainx.model.User;
import com.farmchainx.farmchainx.repository.UserRepository;

@Component
@Profile("fix-password") // Only run when this profile is active
public class PasswordFixer implements CommandLineRunner {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;

    public PasswordFixer(UserRepository userRepo, PasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        // Fix admin password
        User admin = userRepo.findByEmail("admin@farmchainx.com").orElse(null);
        if (admin != null) {
            String newEncodedPassword = encoder.encode("1234");
            admin.setPassword(newEncodedPassword);
            userRepo.save(admin);
            System.out.println("✅ Admin password has been fixed and re-encoded!");
            System.out.println("✅ You can now login with admin@farmchainx.com / 1234");
        }
        
        // Fix all other users too
        String[] emails = {"farmer@farmchainx.com", "distributor@farmchainx.com", "retailer@farmchainx.com", "consumer@farmchainx.com"};
        for (String email : emails) {
            User user = userRepo.findByEmail(email).orElse(null);
            if (user != null) {
                user.setPassword(encoder.encode("1234"));
                userRepo.save(user);
                System.out.println("✅ Password fixed for: " + email);
            }
        }
    }
}
