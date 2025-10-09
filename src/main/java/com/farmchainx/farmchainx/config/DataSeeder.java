package com.farmchainx.farmchainx.config;

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
        // Define all the roles needed in the system
        String[] roles = {"ROLE_CONSUMER", "ROLE_FARMER", "ROLE_DISTRIBUTOR", "ROLE_ADMIN"};
        
        // Create roles if they don't exist
        for (String roleName : roles) {
            if (roleRepository.findByRoleName(roleName).isEmpty()) {
                Role role = new Role();
                role.setRoleName(roleName);
                roleRepository.save(role);
                System.out.println("Created role: " + roleName);
            }
        }
    }
}
