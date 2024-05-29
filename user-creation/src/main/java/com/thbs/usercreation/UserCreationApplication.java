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
	 
	    
}
