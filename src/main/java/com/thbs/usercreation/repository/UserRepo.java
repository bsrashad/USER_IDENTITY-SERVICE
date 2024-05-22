package com.thbs.usercreation.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.thbs.usercreation.entity.User;
import com.thbs.usercreation.enumerate.Role;

public interface UserRepo extends JpaRepository<User, Long> {
	 	boolean existsByEmployeeId(Long employeeId);

		boolean existsByEmail(String email);

		Optional<User> findByEmail(String email);
		
		User findByEmployeeId(Long employeeId);

		List<User> findByRole(Role role);
		
		List<User> findByBusinessUnit(String businessUnit);
		List<User> findByEmployeeIdIn(List<Long> employeeIds);
}
