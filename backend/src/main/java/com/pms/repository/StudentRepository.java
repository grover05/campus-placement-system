package com.pms.repository;

import com.pms.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByUser_Email(String email);
    Optional<Student> findByUserId(Long userId);
}
