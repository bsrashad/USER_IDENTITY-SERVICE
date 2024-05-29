package com.thbs.usercreation.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/logout")
public class LogoutController {

    @PostMapping
    public ResponseEntity<String> logout(@RequestBody Long employeeId) {
        System.out.println("Received Employee ID: " + employeeId);

        try {
            // Load Python script from resources
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("scripts/DeleteS3Token.py");
            System.out.println("processbuilder recived "+employeeId);
            if (inputStream == null) {
                System.out.println("Python script not found.");
                return ResponseEntity.status(500).body("Python script not found.");
            }

            // Read the script content
            StringBuilder scriptContent = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    scriptContent.append(line).append("\n");
                }
            }

            // Execute Python script with employeeId as argument
            ProcessBuilder processBuilder = new ProcessBuilder("python3", "-c", scriptContent.toString(), employeeId.toString());
            Process process = processBuilder.start();

            // Capture standard error stream of Python process
            try (InputStream errorStream = process.getErrorStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.err.println("Python Script Error: " + line);
                }
            }

            // Wait for the script to finish
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Python script executed successfully.");
            } else {
                System.out.println("Error executing Python script. Exit code: " + exitCode);
                return ResponseEntity.status(500).body("Error executing Python script.");
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Error executing Python script: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error executing Python script.");
        }

        return ResponseEntity.ok("Employee ID=" + employeeId);
    }
}
