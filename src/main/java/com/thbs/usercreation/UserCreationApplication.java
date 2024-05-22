package com.thbs.usercreation;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

import com.thbs.usercreation.dto.RegisterRequest;
import com.thbs.usercreation.enumerate.Role;
import com.thbs.usercreation.repository.UserRepo;
import com.thbs.usercreation.service.AuthenticationService;

@SpringBootApplication
@EnableDiscoveryClient
public class UserCreationApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserCreationApplication.class, args);
	}
	 @Bean
	    public CommandLineRunner commandLineRunner(
	            AuthenticationService service,
	            UserRepo userRepository // Inject UserRepository
	    ) {
	        return args -> {
	            // Register an admin user
	            var admin = RegisterRequest.builder()
	                    .firstname("Admin")
	                    .lastname("Admin")
	                    .email("admin@mail.com")
	                    .password("password") // Note: Password will be hashed to bcrypt format by the service
	                    .role(Role.ADMIN)
	                    .businessUnit("NBU-TRAINING")
	                    .employeeId(7000L)
	                    .isemailverified(false)
	                    .build();
	    
	            // Check if admin user already exists
	            if (userRepository.findByEmail(admin.getEmail()).isEmpty()) {
	                // Save and register admin user
	                System.out.println("Admin token: " + service.register(admin).getAccessToken());
	            } else {
	                System.out.println("Admin user already exists.");
	            }
	    
	            // Register a trainer user
	            var trainer = RegisterRequest.builder()
	                    .firstname("Trainer")
	                    .lastname("Trainer")
	                    .email("trainer@mail.com")
	                    .password("password") // Note: Password will be hashed to bcrypt format by the service
	                    .role(Role.TRAINER)
	                    .businessUnit("NBU-TRAINING")
	                    .employeeId(7001L)
	                    .isemailverified(false)
	                    .build();
	    
	            // Check if trainer user already exists
	            if (userRepository.findByEmail(trainer.getEmail()).isEmpty()) {
	                // Save and register trainer user
	                System.out.println("Trainer token: " + service.register(trainer).getAccessToken());
	            } else {
	                System.out.println("Trainer user already exists.");
	            }
	        };
	    }
	    
}
