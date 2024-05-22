package com.thbs.usercreation;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import com.thbs.usercreation.entity.User;
import com.thbs.usercreation.repository.UserRepo;
import com.thbs.usercreation.service.JwtService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomLogoutHandler implements LogoutHandler {

    private final UserRepo userRepo;
    private final JwtService jwtService;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            User user = (User) authentication.getPrincipal();
            String jwtToken = request.getHeader("Authorization").substring(7); // Assuming JWT token is in the Authorization header
            runPythonScript(user.getEmployeeId());
        }
        SecurityContextHolder.clearContext();
    }

    private void runPythonScript(Long userId) {


        System.out.println("------------ Employee Id : -----"+ userId);
        try {
            String userIdStr = String.valueOf(userId);
            InputStream scriptInputStream = getClass().getClassLoader().getResourceAsStream("scripts/LogoutScript.py");
            if (scriptInputStream == null) {
                System.err.println("Script file not found in classpath");
                return;
            }

            File tempScriptFile = File.createTempFile("LogoutScript", ".py");
            Files.copy(scriptInputStream, tempScriptFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            ProcessBuilder processBuilder = new ProcessBuilder(
                    "python3",
                    tempScriptFile.getAbsolutePath(),
                    userIdStr
            );

            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                System.err.println("Error: " + errorLine);
            }

            int exitCode = process.waitFor();
            System.out.println("Exited with error code: " + exitCode);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // @Override
    // public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
    //     // TODO Auto-generated method stub
    //     throw new UnsupportedOperationException("Unimplemented method 'logout'");
    // }
}
