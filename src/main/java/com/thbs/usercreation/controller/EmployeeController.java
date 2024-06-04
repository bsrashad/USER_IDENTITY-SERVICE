package com.thbs.usercreation.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;

@RestController
public class EmployeeController {

    @PostMapping("/getEmployeeToken")
    public ResponseEntity<Map<String, String>> getEmployeeToken(@RequestBody String empid) {
        try {
            // Run the Python script with the employee ID
            ProcessBuilder pb = new ProcessBuilder("python", "scripts/WriteTokenS3.py", empid);
            Process process = pb.start();

            // Read the standard output
            BufferedReader stdOutput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            // Read the standard error
            BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            StringBuilder output = new StringBuilder();
            String line;

            // Capture the standard output
            while ((line = stdOutput.readLine()) != null) {
                output.append(line);
            }

            // Capture the standard error
            StringBuilder errorOutput = new StringBuilder();
            while ((line = stdError.readLine()) != null) {
                errorOutput.append(line);
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                String token = output.toString().trim();
                return ResponseEntity.ok(Map.of("token", token));
            } else {
                System.err.println("Python script error: " + errorOutput.toString());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Python script error: " + errorOutput.toString()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }
}
