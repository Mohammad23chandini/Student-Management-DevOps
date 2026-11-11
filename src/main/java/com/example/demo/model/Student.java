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

    // Academic Details (10.0 CGPA Scale)
    @Column(name = "roll_number")
    private String rollNumber;

    @Column(name = "semester")
    private String semester; // "Semester 1" to "Semester 8"

    @Column(name = "gpa")
    private Double gpa; // Scale 0.00 – 10.00 (e.g., 9.25, 8.80)

    @Column(name = "attendance")
    private Integer attendance; // Percentage 0 – 100%

    @Column(name = "academic_status")
    private String academicStatus; // "Distinction", "First Class", "Second Class", "Academic Warning"

    // Fee & Billing Details (In Indian Rupees ₹)
    @Column(name = "total_fee")
    private Double totalFee; // e.g., 85000.0 (₹85,000)

    @Column(name = "paid_fee")
    private Double paidFee; // e.g., 85000.0 or 50000.0

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
            this.rollNumber = "23CSE" + (id != null ? String.format("%03d", id % 1000) : "001");
        }
        if (this.semester == null || this.semester.trim().isEmpty()) {
            this.semester = "Semester 4";
        }
        if (this.gpa == null) {
            this.gpa = 8.50;
        }
        if (this.attendance == null) {
            this.attendance = 92;
        }
        if (this.academicStatus == null || this.academicStatus.trim().isEmpty()) {
            updateAcademicStatusFromGpa(this.gpa);
        }
        if (this.totalFee == null || this.totalFee <= 0) {
            this.totalFee = 85000.0;
        }
        if (this.paidFee == null) {
            this.paidFee = 85000.0;
        }
        if (this.feeDueDate == null) {
            this.feeDueDate = LocalDate.now().plusMonths(2);
        }
    }

    public void updateAcademicStatusFromGpa(double val) {
        if (val >= 9.0) {
            this.academicStatus = "Distinction";
        } else if (val >= 7.5) {
            this.academicStatus = "First Class";
        } else if (val >= 6.0) {
            this.academicStatus = "Second Class";
        } else {
            this.academicStatus = "Academic Warning";
        }
    }

    public void recalculateFeeStatus() {
        double total = (totalFee != null) ? totalFee : 85000.0;
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
        return rollNumber != null ? rollNumber : ("23CSE" + (id != null ? String.format("%03d", id % 1000) : "001"));
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public String getSemester() {
        return semester != null ? semester : "Semester 4";
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public Double getGpa() {
        return gpa != null ? gpa : 8.50;
    }

    public void setGpa(Double gpa) {
        this.gpa = gpa;
        if (gpa != null) {
            updateAcademicStatusFromGpa(gpa);
        }
    }

    public Integer getAttendance() {
        return attendance != null ? attendance : 92;
    }

    public void setAttendance(Integer attendance) {
        this.attendance = attendance;
    }

    public String getAcademicStatus() {
        return academicStatus != null ? academicStatus : "First Class";
    }

    public void setAcademicStatus(String academicStatus) {
        this.academicStatus = academicStatus;
    }

    public Double getTotalFee() {
        return totalFee != null ? totalFee : 85000.0;
    }

    public void setTotalFee(Double totalFee) {
        this.totalFee = totalFee;
        recalculateFeeStatus();
    }

    public Double getPaidFee() {
        return paidFee != null ? paidFee : 85000.0;
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
