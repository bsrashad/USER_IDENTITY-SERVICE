package com.thbs.usercreation.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/demo-controller") // This controller handles requests with the base URL "/api/v1/demo-controller"
@Hidden 
// @CrossOrigin(origins = {"172.18.4.192", "172.18.5.13:5173"})
public class DemoController {

    @GetMapping
    public ResponseEntity<String> sayHello() {
        return ResponseEntity.ok("Hello from secured endpoint"); // Returns a response entity with a message indicating successful access to the endpoint
    }
}
