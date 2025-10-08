package com.farmchainx.farmchainx.configuration;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.farmchainx.farmchainx.model.Role;
import com.farmchainx.farmchainx.repository.RoleRepository;

@Component
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public DataSeeder(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) throws Exception {

        String[] roles = {
            "ROLE_CONSUMER",
            "ROLE_FARMER",
            "ROLE_DISTRIBUTER",
            "ROLE_RETAILER",
            "ROLE_ADMIN"
        };

        for (String roleName : roles) {
            if (!roleRepository.existsByRoleName(roleName)) {
                Role role = new Role();
                role.setRoleName(roleName);
                roleRepository.save(role);
            }
        }

        System.out.println("✅ Role seeding completed successfully!");
    }
}
