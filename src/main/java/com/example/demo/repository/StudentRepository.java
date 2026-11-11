package com.example.demo.repository;

import com.example.demo.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    @Query("SELECT DISTINCT s.course FROM Student s WHERE s.course IS NOT NULL AND TRIM(s.course) != '' ORDER BY s.course ASC")
    List<String> findDistinctCourses();

    List<Student> findTop5ByOrderByIdDesc();
}
