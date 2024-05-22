package com.thbs.usercreation.service;



import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.thbs.usercreation.dto.AuthenticationRequest;
import com.thbs.usercreation.dto.AuthenticationResponse;
import com.thbs.usercreation.dto.EmailRequest;
import com.thbs.usercreation.dto.RegisterRequest;
import com.thbs.usercreation.dto.VerifyPasswordToken;
import com.thbs.usercreation.entity.Token;
import com.thbs.usercreation.entity.User;
import com.thbs.usercreation.enumerate.Role;
import com.thbs.usercreation.enumerate.TokenType;
import com.thbs.usercreation.exception.UserManagementException;
import com.thbs.usercreation.repository.TokenRepository;
import com.thbs.usercreation.repository.UserRepo;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepo repository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService  emailService;

    // Method to handle user registration
    public AuthenticationResponse register(RegisterRequest request) {
        // Check if a user with the given email already exists
        if (repository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserManagementException("User with the given email already exists");
        }
        
        if (repository.existsByEmployeeId(request.getEmployeeId())) {
            throw new UserManagementException("User with employeeID  already exists");
        }

        // Create a new user entity based on the registration request
        Role role = request.getRole() != null ? request.getRole() : Role.USER;

        var user = User.builder()
            .firstname(request.getFirstname())
            .lastname(request.getLastname())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .isemailverified(true)
            .role(role)
            .businessUnit(request.getBusinessUnit())
            .employeeId(request.getEmployeeId())
            .build();

        // Save the user to the repository
        var savedUser = repository.save(user);

        // Generate JWT token for the user
        var jwtToken = jwtService.generateToken(user);

    String verificationUrl = "http://localhost:4321/api/v1/auth/verifyEmailToken?token=" + jwtToken;
    emailService.sendEmail(request.getEmail(),"email verification", verificationUrl);
    System.out.println("-------------------"+verificationUrl);
        // Save the user's token in the repository
//        saveUserToken(savedUser, jwtToken);

        // Return the authentication response containing the token
        return AuthenticationResponse.builder()
            
            .message("Registration successful but email has to be verified ")
            .build();
    }
    private void executeBatchScript(Long userId, String jwtToken) {
        try {
        // Convert userId to String
        String userIdStr = String.valueOf(userId);
       
               
     // Read the Python script from the classpath
        InputStream scriptInputStream = getClass().getClassLoader().getResourceAsStream("scripts/WriteTokenS3.py");
       
        if (scriptInputStream == null) {
            System.err.println("Script file not found in classpath");
            return;
        }
 
        // Create a temporary file to write the script content
        File tempScriptFile = File.createTempFile("WriteTokenS3", ".py");
       
        // Write the script content to the temporary file
        Files.copy(scriptInputStream, tempScriptFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
 
        // Create ProcessBuilder
        ProcessBuilder processBuilder = new ProcessBuilder(
            "python3",
            tempScriptFile.getAbsolutePath(), // Path to temporary Python script file
            userIdStr,  // First argument to the script
            jwtToken    // Second argument to the script
        );
       
        System.out.println("Executing command: " + String.join(" ", processBuilder.command()));
       
        // Start the process
        Process process = processBuilder.start();
        System.out.println("Process started");
       
        // Read the output and error streams from the batch script
        BufferedReader reader = new BufferedReader(new
       InputStreamReader(process.getInputStream()));
        BufferedReader errorReader = new BufferedReader(new
       InputStreamReader(process.getErrorStream()));
        System.out.println("Reading input");
       
        // Read output stream
        String line;
        while ((line = reader.readLine()) != null) {
        System.out.println(line);
        }
       
        // Read error stream
        String errorLine;
        while ((errorLine = errorReader.readLine()) != null) {
        System.err.println("Error: " + errorLine); // Print to standard error stream
        }
       
        // Wait for the process to complete
        int exitCode = process.waitFor();
        System.out.println("\nExited with error code: " + exitCode);
       
        } catch (IOException | InterruptedException e) {
        e.printStackTrace();
        }
        }
    // Method to handle user authentication
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        try {
            // Attempt to authenticate the user
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Invalid email or password.");
        }
   
        // Retrieve user details from the repository
        User user = repository.findByEmail(request.getEmail())
            .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + request.getEmail()));
   
        // Check if email is verified
        String message = user.isIsemailverified() ? "Successfully logged in" : "Email has to be verified";
   
        // Generate JWT token for the user
        var jwtToken = jwtService.generateToken(user);
   
        // Revoke all existing user tokens
        // revokeAllUserTokens(user);
   
        // Save the user's new token in the repository
        saveUserToken(user, jwtToken);
 
            // Print user details, user ID, and token
 
            System.out.println("Authenticated User Details:");
 
            System.out.println("User ID: " + user.getEmployeeId());
   
            System.out.println("User Email: " + user.getEmail());
   
            System.out.println("Generated Token: " + jwtToken);
   
 
   
         // Execute the Python script with user ID and token as arguments
   
         executeBatchScript(user.getEmployeeId(), jwtToken);
   
        // Return the authentication response containing the token
        return AuthenticationResponse.builder()
            .accessToken(jwtToken)
            .message(message)
            .build();
    }
    public ResponseEntity<String> verifyEmailToken( String token) {
        System.out.println("+++++++######++++++++"+token);
    if(!jwtService.isTokenExpired(token)){
      String email=jwtService.extractUsername(token);
      User user = repository.findByEmail(email)
            .orElseThrow();
            user.setEmailVerified(true);
            repository.save(user);
      
        return ResponseEntity.ok("Email verified successfully");
    }
    return ResponseEntity.badRequest().body("Invalid token or user already verified");
    
        
    }

    // public ResponseEntity<String> forgotPassword(EmailRequest emails,HttpServletResponse response) {
    //     System.out.println("$$$$$$$$$$"+emails.getEmail());
    //     User user = repository.findByEmail(emails.getEmail()).orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + emails.getEmail()));
    
        
    //     if (user != null) {
    //         String jwt = jwtService.generateToken(user);

    //         // write react applications forgot passwords url
    //         // String verificationUrl = "http://localhost:4321/api/v1/auth/generatepassword?token=" + jwt;
    //         String resetPasswordUrl = "http://your-frontend-url/reset-password";
    //         response.setHeader("X-Token", jwt); // Set the token in a custom header
    //         // return ResponseEntity.status(HttpStatus.FOUND)
    //         //         .header(HttpHeaders.LOCATION, "http://your-react-app-url/reset-password")
    //         //         .build();
    //         response.setHeader(HttpHeaders.LOCATION, resetPasswordUrl);
    //         emailService.sendEmail(emails.getEmail(), "forgot password", resetPasswordUrl);
    //         return ResponseEntity.status(HttpStatus.OK).body("Link sent to your email for reset password");
    //     }
    
    //     return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not exists");
    // }

    public AuthenticationResponse forgotPassword( EmailRequest emails) {
        // System.out.println("$$$$$$$$$$"+emails.getEmail());
        User user = repository.findByEmail(emails.getEmail())
                          .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + emails.getEmail()));
        if(user!=null) {
 
    String jwt = jwtService.generateToken(user);
    // String verificationUrl = "http://localhost:4321/api/v1/auth/generatepassword?token=" + jwt;
    String verificationUrl="http://172.18.4.81:5173/enter-new-password?token="+jwt;
    emailService.sendpasswordurl(emails.getEmail(), verificationUrl);
 
    return AuthenticationResponse.builder()
        .accessToken(jwt)
        .message("Link sent to your email to reset password")
        .build();
        }
    
    return AuthenticationResponse.builder()
        .accessToken("")
        .message("User not exixts")
        .build();
    }
    
    

    public ResponseEntity<String> verifypassword(VerifyPasswordToken token) {
        if(!jwtService.isTokenExpired(token.getToken())){
            return ResponseEntity.ok("token validated successfully");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token is expired.");
        }
      
      }

    public ResponseEntity<String> resetPassword( String token,String newPassword) {

        if(!jwtService.isTokenExpired(token)){
            String email = jwtService.extractUsername(token);
            User user = repository.findByEmail(email).orElseThrow();
            // revokeAllUserTokens(user);
        
           
            user.setPassword(passwordEncoder.encode(newPassword));
            
            // user.setPassword(newPassword);
            user = repository.save(user);

      
            return ResponseEntity.ok("RESET PASSWORD successfully");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token is expired.");
        }
      }


      public ResponseEntity<String> changePassword( String email, String oldPassword,String newPassword) {
        // String oldpasswordencoded= passwordEncoder.encode(oldPassword);
        // Optional<User> userOptional = repository.findByUsernameAndPassword(email, passwordEncoder.matches(newPassword, oldPassword)  oldPassword);
        Optional<User> userOptional = repository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if(passwordEncoder.matches(oldPassword, user.getPassword())){
                String encodednewpassword=passwordEncoder.encode(newPassword);
                user.setPassword(encodednewpassword);
            repository.save(user);
            return ResponseEntity.status(HttpStatus.OK).body("Password changed successfully for " + email);
            }else{
                return ResponseEntity.status(HttpStatus.OK).body("password doesnt match");
            }
            
            
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid email ");
        }
      }

    // Method to save user token in the repository
    private void saveUserToken(User user, String jwtToken) {
        var token = Token.builder()
            .user(user)
            .token(jwtToken)
            .tokenType(TokenType.BEARER)
            .expired(false)
            .revoked(false)
            .build();
        tokenRepository.save(token);
    }

    // Method to revoke all existing user tokens
    // public void revokeAllUserTokens(User user) {
    //     var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
    //     if (validUserTokens.isEmpty()) {
    //         return;
    //     }
    //     validUserTokens.forEach(token -> {
    //         token.setExpired(true);
    //         token.setRevoked(true);
    //     });
    //     tokenRepository.saveAll(validUserTokens);
    // }

}

