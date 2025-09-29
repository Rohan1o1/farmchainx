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
	
	public void run(String... args) throws Exception{
		if(!roleRepository.existsByRoleName("ROLE_USER")) {
			Role userRole = new Role();
			userRole.setRoleName("ROLE_USER");
			roleRepository.save(userRole);
			System.out.println("ROLE_USER created");
		}
		
		if(!roleRepository.existsByRoleName("ROLE_ADMIN")) {
			Role adminRole = new Role();
			adminRole.setRoleName("ROLE_ADMIN");
			roleRepository.save(adminRole);
			System.out.println("ROLE_ADMIN created");
		}
	}
}
