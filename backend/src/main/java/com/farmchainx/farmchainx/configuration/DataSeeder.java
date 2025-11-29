package com.farmchainx.farmchainx.configuration;

import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import com.farmchainx.farmchainx.model.Role;
import com.farmchainx.farmchainx.model.User;
import com.farmchainx.farmchainx.repository.RoleRepository;
import com.farmchainx.farmchainx.repository.UserRepository;


    @Component
    public class DataSeeder implements CommandLineRunner {

        private final RoleRepository roleRepo;
        private final UserRepository userRepo;
        private final PasswordEncoder encoder;

        public DataSeeder(RoleRepository roleRepo, UserRepository userRepo, PasswordEncoder encoder) {
            this.roleRepo = roleRepo;
            this.userRepo = userRepo;
            this.encoder = encoder;
        }

        @Override
        public void run(String... args) {

            // ✅ 1) Create all roles
            String[] roles = {"ROLE_CONSUMER","ROLE_FARMER","ROLE_DISTRIBUTOR","ROLE_RETAILER","ROLE_ADMIN"};
            for (String r : roles) {
                if (!roleRepo.existsByName(r)) {
                    Role role = new Role();
                    role.setName(r);
                    roleRepo.save(role);
                }
            }

            // ✅ 2) Create all sample users as mentioned in README
            createUserIfNotExists("admin@farmchainx.com", "Admin", "1234", "ROLE_ADMIN");
            createUserIfNotExists("farmer@farmchainx.com", "Farmer", "1234", "ROLE_FARMER");
            createUserIfNotExists("distributor@farmchainx.com", "Distributor", "1234", "ROLE_DISTRIBUTOR");
            createUserIfNotExists("retailer@farmchainx.com", "Retailer", "1234", "ROLE_RETAILER");
            createUserIfNotExists("consumer@farmchainx.com", "Consumer", "1234", "ROLE_CONSUMER");
        }

        private void createUserIfNotExists(String email, String name, String password, String roleName) {
            if (!userRepo.existsByEmail(email)) {
                Role role = roleRepo.findByName(roleName).orElseThrow();

                User user = new User();
                user.setName(name);
                user.setEmail(email);
                user.setPassword(encoder.encode(password));
                user.setRoles(Set.of(role));

                userRepo.save(user);

                System.out.println("✅ Sample user created: " + email + " | password: " + password + " | role: " + roleName);
            }
        }
    }
