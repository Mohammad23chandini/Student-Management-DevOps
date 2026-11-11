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

    // Academic Details
    @Column(name = "roll_number")
    private String rollNumber;

    @Column(name = "semester")
    private String semester; // e.g., "Semester 1" to "Semester 8"

    @Column(name = "gpa")
    private Double gpa; // e.g., 3.85

    @Column(name = "attendance")
    private Integer attendance; // e.g., 92%

    @Column(name = "academic_status")
    private String academicStatus; // "Good Standing", "Dean's List", "Probation"

    // Fee & Billing Details
    @Column(name = "total_fee")
    private Double totalFee; // e.g., 5000.0

    @Column(name = "paid_fee")
    private Double paidFee; // e.g., 3500.0

    @Column(name = "fee_status")
    private String feeStatus; // "PAID", "PARTIAL", "DUE", "OVERDUE"

    @Column(name = "fee_due_date")
    private LocalDate feeDueDate;

    public Student() {
    }

    public Student(String name, String email, String course) {
        this.name = name;
        this.email = email;
        this.course = course;
        this.registrationDate = LocalDate.now();
        initDefaults();
    }

    public Student(String name, String email, String course, LocalDate registrationDate) {
        this.name = name;
        this.email = email;
        this.course = course;
        this.registrationDate = registrationDate != null ? registrationDate : LocalDate.now();
        initDefaults();
    }

    @PrePersist
    @PreUpdate
    public void prePersistOrUpdate() {
        if (this.registrationDate == null) {
            this.registrationDate = LocalDate.now();
        }
        initDefaults();
        recalculateFeeStatus();
    }

    private void initDefaults() {
        if (this.rollNumber == null || this.rollNumber.trim().isEmpty()) {
            this.rollNumber = "STU-" + (id != null ? String.format("%04d", id) : "2026");
        }
        if (this.semester == null || this.semester.trim().isEmpty()) {
            this.semester = "Semester 1";
        }
        if (this.gpa == null) {
            this.gpa = 3.50;
        }
        if (this.attendance == null) {
            this.attendance = 90;
        }
        if (this.academicStatus == null || this.academicStatus.trim().isEmpty()) {
            this.academicStatus = (this.gpa >= 3.8) ? "Dean's List" : (this.gpa < 2.0 ? "Probation" : "Good Standing");
        }
        if (this.totalFee == null || this.totalFee <= 0) {
            this.totalFee = 5000.0;
        }
        if (this.paidFee == null) {
            this.paidFee = 0.0;
        }
        if (this.feeDueDate == null) {
            this.feeDueDate = LocalDate.now().plusMonths(2);
        }
    }

    public void recalculateFeeStatus() {
        double total = (totalFee != null) ? totalFee : 5000.0;
        double paid = (paidFee != null) ? paidFee : 0.0;

        if (paid >= total) {
            this.feeStatus = "PAID";
        } else if (paid > 0) {
            this.feeStatus = "PARTIAL";
        } else {
            if (feeDueDate != null && feeDueDate.isBefore(LocalDate.now())) {
                this.feeStatus = "OVERDUE";
            } else {
                this.feeStatus = "DUE";
            }
        }
    }

    // Getters and Setters with Safe Fallbacks
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate != null ? registrationDate : LocalDate.now();
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getRollNumber() {
        return rollNumber != null ? rollNumber : ("STU-" + (id != null ? id : "001"));
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public String getSemester() {
        return semester != null ? semester : "Semester 1";
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public Double getGpa() {
        return gpa != null ? gpa : 3.50;
    }

    public void setGpa(Double gpa) {
        this.gpa = gpa;
        if (gpa != null) {
            if (gpa >= 3.8) this.academicStatus = "Dean's List";
            else if (gpa < 2.0) this.academicStatus = "Probation";
            else this.academicStatus = "Good Standing";
        }
    }

    public Integer getAttendance() {
        return attendance != null ? attendance : 90;
    }

    public void setAttendance(Integer attendance) {
        this.attendance = attendance;
    }

    public String getAcademicStatus() {
        return academicStatus != null ? academicStatus : "Good Standing";
    }

    public void setAcademicStatus(String academicStatus) {
        this.academicStatus = academicStatus;
    }

    public Double getTotalFee() {
        return totalFee != null ? totalFee : 5000.0;
    }

    public void setTotalFee(Double totalFee) {
        this.totalFee = totalFee;
        recalculateFeeStatus();
    }

    public Double getPaidFee() {
        return paidFee != null ? paidFee : 0.0;
    }

    public void setPaidFee(Double paidFee) {
        this.paidFee = paidFee;
        recalculateFeeStatus();
    }

    public Double getDueFee() {
        double total = getTotalFee();
        double paid = getPaidFee();
        return Math.max(0.0, total - paid);
    }

    public String getFeeStatus() {
        if (feeStatus == null) {
            recalculateFeeStatus();
        }
        return feeStatus;
    }

    public void setFeeStatus(String feeStatus) {
        this.feeStatus = feeStatus;
    }

    public LocalDate getFeeDueDate() {
        return feeDueDate != null ? feeDueDate : LocalDate.now().plusMonths(1);
    }

    public void setFeeDueDate(LocalDate feeDueDate) {
        this.feeDueDate = feeDueDate;
        recalculateFeeStatus();
    }

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

    @Transient
    public String getDepartment() {
        return course;
    }
}
