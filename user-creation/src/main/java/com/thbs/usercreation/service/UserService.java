package com.thbs.usercreation.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;

import com.thbs.usercreation.dto.UserDTO;
import com.thbs.usercreation.entity.User;
import com.thbs.usercreation.enumerate.Role;
import com.thbs.usercreation.exception.UserManagementException;
import com.thbs.usercreation.repository.UserRepo;
import org.springframework.beans.factory.annotation.Value;

import jakarta.servlet.http.HttpServletResponse;


@Service
public class UserService {
	private final UserRepo userRepo;

    @Autowired
    public UserService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

	@Value("${user.home}")
	private String userHome;

	
	BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();
	
	public UserDTO mapToUserDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setEmployeeId(user.getEmployeeId());
        userDTO.setFirstName(user.getFirstname());
        userDTO.setLastName(user.getLastname());
        userDTO.setBusinessUnit(user.getBusinessUnit());
        userDTO.setEmail(user.getEmail());
        userDTO.setRole(user.getRole());
        return userDTO;
    }

	public List<User> saveUser(MultipartFile file) throws EncryptedDocumentException, IOException {
	    
	        List<List<String>> rows = new ArrayList<>();

	        Workbook workbook = WorkbookFactory.create(file.getInputStream());
	        Sheet sheet = workbook.getSheetAt(0); // Change index to 0
	        rows = StreamSupport.stream(sheet.spliterator(), false)
	                .skip(1)
	                .map(row -> StreamSupport
	                        .stream(row.spliterator(), false)
	                        .map(this::getCellStringValue)
	                        .collect(Collectors.toList()))
	                .collect(Collectors.toList());
	        System.out.println("rows :: " + rows);

	        // Save data to the database
	        List<User> excelDataList = new ArrayList<>();
	        for (List<String> row : rows) {
	            double employeeIdDouble = Double.parseDouble(row.get(0)); // Assuming empId is in the 1st column (index 0)
	            long employeeId = (long) employeeIdDouble;
	            String email = row.get(3); // Assuming email is in the 4th column (index 3)

	            // Check if employee ID already exists
	            if (userRepo.existsByEmployeeId(employeeId)) {
	                throw new UserManagementException("User with employeeID " + employeeId + " already exists");
	            }

	            // Check if email already exists
	            if (userRepo.existsByEmail(email)) {
	                throw new UserManagementException("User with email " + email + " already exists");
	            }

	            // If both employee ID and email are unique, proceed with saving the data
	            User excelData = new User();
	            User existingUser = userRepo.findByEmployeeId(employeeId);
	    	    if (existingUser == null) {
  
	            excelData.setEmployeeId(employeeId);
	            excelData.setFirstname(row.get(1));
	            excelData.setLastname(row.get(2));
	            excelData.setBusinessUnit(row.get(4));
	            excelData.setEmail(email);
	            excelData.setRole(Role.USER);
	            excelData.setIsemailverified(true);

	            // Generate password
	            String password = "root";
	            String generatedPassword = bcrypt.encode(password);

	            excelData.setPassword(generatedPassword);
	            excelDataList.add(excelData);
	    	    }
	    	    else {
	    	        throw new UserManagementException("User with employee ID "+excelData.getEmployeeId()+" is already exists");
	    	    }
	        }

	        // Save all successfully processed rows to the database
	        return userRepo.saveAll(excelDataList);
	    }

	

	public String getCellStringValue(Cell cell) {
		CellType cellType = cell.getCellType();

		if (cellType == CellType.STRING) {
			return cell.getStringCellValue();
		} else if (cellType == CellType.NUMERIC) {
			return String.valueOf(cell.getNumericCellValue());
		} else if (cellType == CellType.BOOLEAN) {
			return String.valueOf(cell.getBooleanCellValue());
		}

		return null;
	}
	
	public List<User> findAll(){
		return userRepo.findAll();
	}
	
	public User getUserByEmployeeId(Long employeeId) {
	    return userRepo.findByEmployeeId(employeeId);
	}

	public List<UserDTO> getUsersByRole(Role role) {
		List<User> users  =userRepo.findByRole(role);
		return users.stream()
                .map(this::mapToUserDTO)
                .collect(Collectors.toList());
	}
	
	public List<Long> findUserEmployeeIds() {
        List<User> trainees = userRepo.findByRole(Role.USER);
        return trainees.stream()
                .map(User::getEmployeeId)
                .collect(Collectors.toList());
    }
	
	public List<Long> findEmployeeIdsByBusinessUnit(String businessUnit) {
        List<User> users = userRepo.findByBusinessUnit(businessUnit);
        return users.stream()
                .map(User::getEmployeeId)
                .collect(Collectors.toList());
    }
	
	 public List<UserDTO> findAllUsers() {
	        List<User> users = userRepo.findAll();
	        return users.stream()
	                .map(this::mapToUserDTO)
	                .collect(Collectors.toList());
	    }
	 
	 public List<UserDTO> getTrainer(){
	        List<User> trainees = userRepo.findByRole(Role.TRAINER);
	        return trainees.stream()
	                .map(this::mapToUserDTO)
	                .collect(Collectors.toList());
	         
	 }
	    
	 public void updateUsersRoleToTrainer(List<Long> employeeIds) {
		    List<User> usersToUpdate = userRepo.findByEmployeeIdIn(employeeIds);

		    for (User user : usersToUpdate) {
		        user.setRole(Role.TRAINER);
		    }

		    userRepo.saveAll(usersToUpdate);
		}
		public void updateTrainerRoleToUser(List<Long> employeeIds) {
		List<User> usersToUpdate = userRepo.findByEmployeeIdIn(employeeIds);
 
		for (User user : usersToUpdate) {
			user.setRole(Role.USER);
		}
 
		userRepo.saveAll(usersToUpdate);
	}


 
public void generateExcelFile(HttpServletResponse response) {
		Workbook workbook = new XSSFWorkbook();
		CreationHelper createHelper = workbook.getCreationHelper();
		String fileName = "User sample format.xlsx";
		String filePath = userHome + "\\Downloads\\" + fileName;
		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
		Sheet sheet = workbook.createSheet("Sample Format");
		Row headerRow = sheet.createRow(0);
		headerRow.createCell(0).setCellValue("Employee Id");
		headerRow.createCell(1).setCellValue("First Name");
		headerRow.createCell(2).setCellValue("Last Name");
		headerRow.createCell(3).setCellValue("Email");
		headerRow.createCell(4).setCellValue("Business Unit");
		for (int i = 0; i < 5; i++) { // Autosize only for the first three columns (Employee ID, Employee Name,
			// Grade)
			sheet.autoSizeColumn(i);
		}
		try {
			workbook.write(response.getOutputStream());
			// System.out.println(" Evaluation report sent as response.");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				workbook.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
 
	}

}
