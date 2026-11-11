package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String course;

    @Column(name = "registration_date")
    private LocalDate registrationDate;

    public Student() {
    }

    public Student(String name, String email, String course) {
        this.name = name;
        this.email = email;
        this.course = course;
        this.registrationDate = LocalDate.now();
    }

    public Student(String name, String email, String course, LocalDate registrationDate) {
        this.name = name;
        this.email = email;
        this.course = course;
        this.registrationDate = registrationDate != null ? registrationDate : LocalDate.now();
    }

    @PrePersist
    public void prePersist() {
        if (this.registrationDate == null) {
            this.registrationDate = LocalDate.now();
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getCourse() {
        return course;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate != null ? registrationDate : LocalDate.now();
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }

    /**
     * Extracts initials for display in the macOS avatar icon.
     * e.g., "John Doe" -> "JD", "Lakshmi" -> "LA"
     */
    @Transient
    public String getInitials() {
        if (name == null || name.trim().isEmpty()) {
            return "ST";
        }
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            String first = parts[0].substring(0, 1).toUpperCase();
            String second = parts[parts.length - 1].substring(0, 1).toUpperCase();
            return first + second;
        } else if (parts[0].length() >= 2) {
            return parts[0].substring(0, 2).toUpperCase();
        } else {
            return parts[0].toUpperCase();
        }
    }

    /**
     * Alias for course to fulfill department requirements seamlessly.
     */
    @Transient
    public String getDepartment() {
        return course;
    }
}
