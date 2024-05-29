package com.thbs.usercreation.controller;


import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.thbs.usercreation.exception.UserManagementException;
import com.thbs.usercreation.service.AuthenticationService;
import com.thbs.usercreation.dto.AuthenticationRequest;
import com.thbs.usercreation.dto.AuthenticationResponse;
import com.thbs.usercreation.dto.ChangePasswordRequest;
import com.thbs.usercreation.dto.EmailRequest;
import com.thbs.usercreation.dto.RegisterRequest;
import com.thbs.usercreation.dto.ResetPasswordRequest;
import com.thbs.usercreation.dto.VerifyPasswordToken;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
// @CrossOrigin(origins = {"172.18.4.192", "172.18.5.13:5173"})
public class AuthenticationController {

  private final AuthenticationService service;

  // Endpoint for user registration
  @PostMapping("/register")
  public ResponseEntity<AuthenticationResponse> register(
      @RequestBody RegisterRequest request
  ) {
    try {
        return ResponseEntity.ok(service.register(request)); // Delegate the registration request to the AuthenticationService
    } catch (UserManagementException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            AuthenticationResponse.builder()
                .message(e.getMessage())
                .build()
        );
    }
  }

  // Endpoint for user authentication
  @PostMapping("/authenticate")
  public ResponseEntity<AuthenticationResponse> authenticate(
      @RequestBody AuthenticationRequest request
  ) {
    
    try {
        return ResponseEntity.ok(service.authenticate(request));
    } catch (BadCredentialsException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(AuthenticationResponse.builder()
            .message("Invalid email or password.")
            .build());
    } catch (UsernameNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(AuthenticationResponse.builder()
            .message(e.getMessage())
            .build());
    }
  }

  @GetMapping("/verifyEmailToken")
public ResponseEntity<String> verifyEmailToken(@RequestParam("token") String token) {
    return service.verifyEmailToken(token);

    
}

@PostMapping("/forgotpassword")
public ResponseEntity<AuthenticationResponse> forgotPassword(@RequestBody EmailRequest emails) {
    try {
        AuthenticationResponse response = service.forgotPassword(emails);
        return ResponseEntity.ok(response);
    } catch (UsernameNotFoundException e) {
        // Log this exception if needed
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(AuthenticationResponse.builder()
            .message("No account associated with the provided email.")
            .build());
    }
}


@PostMapping("/verifypasswordtoken")
public ResponseEntity<String> generatePassword(@RequestBody VerifyPasswordToken token) {
    return service.verifypassword(token);

}
// @PostMapping("/resetpassword")
// public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> request) {

//     String  newPassword = request.get("newPassword");
//     String token=request.get("token");

//     return service.resetPassword(token,newPassword);
// }
@PostMapping("/resetpassword")
public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
    String newPassword = request.getNewPassword();
    String token = request.getToken();

    return service.resetPassword(token, newPassword);
}

@PutMapping("/changepassword")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordRequest changePasswordRequest) {
        return service.changePassword(changePasswordRequest.getEmail(),changePasswordRequest.getOldPassword(), changePasswordRequest.getNewPassword());
    }

    




// @GetMapping("/generatepassword")
// public ResponseEntity<String> generatePassword(@RequestParam String token) {
//     if (jwtService.validateToken(token)) {
//         return ResponseEntity.status(HttpStatus.OK).body("redirect:/reset-password");
//     } else {
//         return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token is expired.");
//     }
// }

  
  

}
