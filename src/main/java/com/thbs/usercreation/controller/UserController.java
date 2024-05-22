package com.thbs.usercreation.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.thbs.usercreation.dto.UserDTO;
import com.thbs.usercreation.entity.User;
import com.thbs.usercreation.enumerate.Role;
import com.thbs.usercreation.exception.UserManagementException;
import com.thbs.usercreation.service.UserService;

import jakarta.servlet.http.HttpServletResponse;


@RestController
@RequestMapping("/user")
public class UserController {
	private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

	
	@PostMapping("/upload")
	public ResponseEntity<Object> uploadUserExcel(@RequestParam("file") MultipartFile file) {
		 try {
		        userService.saveUser(file);
		        return ResponseEntity.ok("Trainee's file uploaded successfully.");
		    } catch (IOException e) {
		        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: IOException occurred while processing the file.");
		    }
		 catch (UserManagementException e) {
		        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
		    }
	}
	@GetMapping
	public List<User> getAll(){
		return userService.findAll();
	}
	
	
	 @GetMapping("/{employeeId}")
	    public ResponseEntity<User> getUserByEmployeeId(@PathVariable Long employeeId) {
	        User user = userService.getUserByEmployeeId(employeeId);
	        if (user != null) {
	            return ResponseEntity.ok(user);
	        } else {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
	        }
	    }
	
	 @GetMapping("/role/trainee")
	    public List<UserDTO> getUsersByRoleId() {
		 	Role role = Role.USER;
	        return userService.getUsersByRole(role);
	    }
	 
	 @GetMapping("/getId")
	 public List<Long> getAllUserEmployeeId(){
		 return userService.findUserEmployeeIds();
	 }
	 @GetMapping("/byBusinessUnit/{businessUnit}")
	 public ResponseEntity<List<Long>> getUsersByBusinessUnit(@PathVariable("businessUnit") String businessUnit) {
	        List<Long> users = userService.findEmployeeIdsByBusinessUnit(businessUnit);
	        return ResponseEntity.ok(users);
	    }
	
	    @GetMapping("/all")
	    public List<UserDTO>  findAllUsers() {
	        return userService.findAllUsers();
	    }
	    @GetMapping("/role/trainer")
		  public List<UserDTO> getTrainer() {
			 	Role role = Role.TRAINER;
		        return userService.getUsersByRole(role);
		    }
		  @PutMapping("/updateRoleToTrainer")
		    public String updateUsersRoleToTrainer(@RequestBody List<Long> employeeIds) {
		        try {
		            userService.updateUsersRoleToTrainer(employeeIds);
		            return "Successfully updated roles to TRAINER for the specified users.";
		        } catch (Exception e) {
		            return "Failed to update roles: " + e.getMessage();
		        }
		    }
			@PutMapping("/updateRoleToUser")
		    public String updateTrainerRoleToUser(@RequestBody List<Long> employeeIds) {
		        try {
		            userService.updateTrainerRoleToUser(employeeIds);
		            return "Successfully updated roles to User for the specified users.";
		        } catch (Exception e) {
		            return "Failed to update roles: " + e.getMessage();
		        }
		    }
		  @GetMapping("/format")
		  public void generateSampleFormat(HttpServletResponse response) {
			  userService.generateExcelFile(response);
		  }



}


