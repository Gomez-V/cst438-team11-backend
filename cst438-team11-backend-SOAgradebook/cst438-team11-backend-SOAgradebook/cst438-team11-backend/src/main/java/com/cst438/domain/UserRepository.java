package com.cst438.domain;

import org.springframework.data.repository.CrudRepository;
import com.cst438.domain.UserRepository;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.domain.GradeRepository;


import java.util.List;

public interface UserRepository extends CrudRepository<User, Integer>{
	List<User> findAllByOrderByIdAsc();
	User findByEmail(String email);
}
