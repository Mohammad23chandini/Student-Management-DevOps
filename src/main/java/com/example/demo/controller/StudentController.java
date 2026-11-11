package com.example.demo.controller;

import com.example.demo.model.Student;
import com.example.demo.service.StudentService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

@Controller
public class StudentController {

    private final StudentService studentService;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,63}$"
    );

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    private void populateDashboardModel(Model model, String activeSection) {
        List<Student> students = studentService.getAllStudents();
        Map<String, Long> departmentDistribution = studentService.getDepartmentDistribution();
        List<String> departments = studentService.getDistinctCourses();

        model.addAttribute("students", students);
        model.addAttribute("totalStudents", students.size());
        model.addAttribute("totalDepartments", departmentDistribution.size());
        model.addAttribute("newThisMonth", studentService.getNewStudentsThisMonthCount());
        model.addAttribute("departments", departments);
        model.addAttribute("departmentDistribution", departmentDistribution);
        model.addAttribute("recentActivities", studentService.getRecentActivities());
        model.addAttribute("recentStudents", studentService.getRecentlyAddedStudents(5));

        // Academic (10.0 Scale) & INR Financial KPIs
        model.addAttribute("totalFeesCollected", studentService.getTotalFeesCollected());
        model.addAttribute("totalFeesPending", studentService.getTotalFeesPending());
        model.addAttribute("collectionRate", studentService.getCollectionRate());
        model.addAttribute("avgGpa", studentService.getAverageGpa());
        model.addAttribute("avgAttendance", studentService.getAverageAttendance());
        model.addAttribute("feeStatusDistribution", studentService.getFeeStatusDistribution());

        model.addAttribute("activeSection", activeSection != null ? activeSection : "overview");
        model.addAttribute("currentDate", LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
    }

    @GetMapping("/")
    public String home(@RequestParam(value = "section", required = false) String section, Model model) {
        populateDashboardModel(model, section != null ? section : "overview");
        return "index";
    }

    @GetMapping("/students")
    public String studentsPage(Model model) {
        populateDashboardModel(model, "students");
        return "index";
    }

    @GetMapping("/analytics")
    public String analyticsPage(Model model) {
        populateDashboardModel(model, "analytics");
        return "index";
    }

    @GetMapping("/reports")
    public String reportsPage(Model model) {
        populateDashboardModel(model, "reports");
        return "index";
    }

    @GetMapping("/settings")
    public String settingsPage(Model model) {
        populateDashboardModel(model, "settings");
        return "index";
    }

    @PostMapping("/students")
    public String addStudent(@RequestParam String name,
                             @RequestParam String email,
                             @RequestParam String course,
                             @RequestParam(required = false) String rollNumber,
                             @RequestParam(required = false) String semester,
                             @RequestParam(required = false) Double gpa,
                             @RequestParam(required = false) Integer attendance,
                             @RequestParam(required = false) Double totalFee,
                             @RequestParam(required = false) Double paidFee,
                             @RequestParam(required = false) String feeDueDate,
                             @RequestParam(required = false) String registrationDate,
                             RedirectAttributes redirectAttributes) {

        String trimmedName = name != null ? name.trim() : "";
        String trimmedEmail = email != null ? email.trim().toLowerCase() : "";
        String trimmedCourse = course != null ? course.trim() : "";

        if (trimmedName.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Student name cannot be empty.");
            return "redirect:/students";
        }

        if (trimmedEmail.isEmpty() || !EMAIL_PATTERN.matcher(trimmedEmail).matches()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please provide a valid email address.");
            return "redirect:/students";
        }

        if (trimmedCourse.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Department / Course is required.");
            return "redirect:/students";
        }

        if (studentService.isEmailTaken(trimmedEmail)) {
            redirectAttributes.addFlashAttribute("errorMessage", "A student with email '" + trimmedEmail + "' already exists.");
            return "redirect:/students";
        }

        LocalDate regDate = LocalDate.now();
        if (registrationDate != null && !registrationDate.trim().isEmpty()) {
            try {
                regDate = LocalDate.parse(registrationDate.trim());
            } catch (Exception ignored) {
            }
        }

        LocalDate dueDate = LocalDate.now().plusMonths(2);
        if (feeDueDate != null && !feeDueDate.trim().isEmpty()) {
            try {
                dueDate = LocalDate.parse(feeDueDate.trim());
            } catch (Exception ignored) {
            }
        }

        Student student = new Student(trimmedName, trimmedEmail, trimmedCourse, regDate);
        if (rollNumber != null && !rollNumber.trim().isEmpty()) student.setRollNumber(rollNumber.trim());
        if (semester != null && !semester.trim().isEmpty()) student.setSemester(semester.trim());
        if (gpa != null) student.setGpa(gpa);
        if (attendance != null) student.setAttendance(attendance);
        if (totalFee != null) student.setTotalFee(totalFee);
        if (paidFee != null) student.setPaidFee(paidFee);
        student.setFeeDueDate(dueDate);

        studentService.saveStudent(student);

        redirectAttributes.addFlashAttribute("successMessage", "Student '" + trimmedName + "' enrolled successfully with CGPA & INR fee ledger!");
        return "redirect:/students";
    }

    @GetMapping("/students/edit/{id}")
    public String editStudent(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Student student = studentService.getStudentById(id);

        if (student == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Student not found with ID: " + id);
            return "redirect:/students";
        }

        model.addAttribute("student", student);
        model.addAttribute("departments", studentService.getDistinctCourses());
        return "edit";
    }

    @PostMapping("/students/update/{id}")
    public String updateStudent(@PathVariable Long id,
                                @RequestParam String name,
                                @RequestParam String email,
                                @RequestParam String course,
                                @RequestParam(required = false) String rollNumber,
                                @RequestParam(required = false) String semester,
                                @RequestParam(required = false) Double gpa,
                                @RequestParam(required = false) Integer attendance,
                                @RequestParam(required = false) Double totalFee,
                                @RequestParam(required = false) Double paidFee,
                                @RequestParam(required = false) String feeDueDate,
                                @RequestParam(required = false) String registrationDate,
                                RedirectAttributes redirectAttributes) {

        String trimmedName = name != null ? name.trim() : "";
        String trimmedEmail = email != null ? email.trim().toLowerCase() : "";
        String trimmedCourse = course != null ? course.trim() : "";

        if (trimmedName.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Student name cannot be empty.");
            return "redirect:/students/edit/" + id;
        }

        if (trimmedEmail.isEmpty() || !EMAIL_PATTERN.matcher(trimmedEmail).matches()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please provide a valid email address.");
            return "redirect:/students/edit/" + id;
        }

        if (trimmedCourse.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Department is required.");
            return "redirect:/students/edit/" + id;
        }

        if (studentService.isEmailTakenByAnother(trimmedEmail, id)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Another student is already using email: " + trimmedEmail);
            return "redirect:/students/edit/" + id;
        }

        LocalDate regDate = null;
        if (registrationDate != null && !registrationDate.trim().isEmpty()) {
            try {
                regDate = LocalDate.parse(registrationDate.trim());
            } catch (Exception ignored) {
            }
        }

        LocalDate dueDate = null;
        if (feeDueDate != null && !feeDueDate.trim().isEmpty()) {
            try {
                dueDate = LocalDate.parse(feeDueDate.trim());
            } catch (Exception ignored) {
            }
        }

        Student updated = new Student(trimmedName, trimmedEmail, trimmedCourse, regDate);
        if (rollNumber != null) updated.setRollNumber(rollNumber.trim());
        if (semester != null) updated.setSemester(semester.trim());
        if (gpa != null) updated.setGpa(gpa);
        if (attendance != null) updated.setAttendance(attendance);
        if (totalFee != null) updated.setTotalFee(totalFee);
        if (paidFee != null) updated.setPaidFee(paidFee);
        if (dueDate != null) updated.setFeeDueDate(dueDate);

        studentService.updateStudent(id, updated);

        redirectAttributes.addFlashAttribute("successMessage", "Student record for '" + trimmedName + "' updated successfully.");
        return "redirect:/students";
    }

    @PostMapping("/students/payment/{id}")
    public String recordPayment(@PathVariable Long id,
                                @RequestParam Double paymentAmount,
                                RedirectAttributes redirectAttributes) {
        if (paymentAmount == null || paymentAmount <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please provide a valid payment amount greater than ₹0.");
            return "redirect:/students";
        }

        Student student = studentService.getStudentById(id);
        if (student == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Student record not found.");
            return "redirect:/students";
        }

        studentService.recordPayment(id, paymentAmount);
        redirectAttributes.addFlashAttribute("successMessage", String.format("Fee payment of ₹%.2f successfully credited to %s.", paymentAmount, student.getName()));
        return "redirect:/students";
    }

    @GetMapping("/students/delete/{id}")
    public String deleteStudent(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Student student = studentService.getStudentById(id);
        String studentName = (student != null) ? student.getName() : "Student #" + id;

        studentService.deleteStudent(id);
        redirectAttributes.addFlashAttribute("successMessage", "Student '" + studentName + "' removed successfully.");
        return "redirect:/students";
    }

    @PostMapping("/students/bulk-delete")
    public String bulkDeleteStudents(@RequestParam("ids") List<Long> ids, RedirectAttributes redirectAttributes) {
        if (ids == null || ids.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "No students were selected for deletion.");
            return "redirect:/students";
        }

        int count = studentService.deleteStudentsBulk(ids);
        redirectAttributes.addFlashAttribute("successMessage", "Successfully deleted " + count + " student records.");
        return "redirect:/students";
    }

    @GetMapping("/students/export")
    public void exportCsv(HttpServletResponse response) throws IOException {
        String filename = "students_export_" + LocalDate.now() + ".csv";
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        try (Writer writer = new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8)) {
            writer.write('\ufeff');
            studentService.exportStudentsToCsv(writer);
        }
    }

    @PostMapping("/students/import")
    public String importCsv(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please select a valid CSV file to import.");
            return "redirect:/reports";
        }

        try {
            StudentService.CsvImportResult result = studentService.importStudentsFromCsv(file.getInputStream());
            if (result.getImportedCount() > 0) {
                String msg = "Successfully imported " + result.getImportedCount() + " student(s).";
                if (result.getSkippedCount() > 0) {
                    msg += " (" + result.getSkippedCount() + " rows skipped).";
                }
                redirectAttributes.addFlashAttribute("successMessage", msg);
            } else {
                String error = "No records were imported.";
                if (!result.getErrorMessages().isEmpty()) {
                    error += " First error: " + result.getErrorMessages().get(0);
                }
                redirectAttributes.addFlashAttribute("errorMessage", error);
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to process CSV: " + e.getMessage());
        }

        return "redirect:/students";
    }

    @GetMapping("/api/students/{id}")
    @ResponseBody
    public ResponseEntity<?> getStudentApi(@PathVariable Long id) {
        Student student = studentService.getStudentById(id);
        if (student == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(student);
    }

    @GetMapping("/api/stats")
    @ResponseBody
    public ResponseEntity<?> getStatsApi() {
        Map<String, Object> stats = new HashMap<>();
        List<Student> students = studentService.getAllStudents();
        stats.put("totalStudents", students.size());
        stats.put("totalDepartments", studentService.getDepartmentDistribution().size());
        stats.put("newThisMonth", studentService.getNewStudentsThisMonthCount());
        stats.put("departmentDistribution", studentService.getDepartmentDistribution());
        stats.put("totalFeesCollected", studentService.getTotalFeesCollected());
        stats.put("totalFeesPending", studentService.getTotalFeesPending());
        stats.put("collectionRate", studentService.getCollectionRate());
        stats.put("averageGpa", studentService.getAverageGpa());
        stats.put("averageAttendance", studentService.getAverageAttendance());
        stats.put("feeStatusDistribution", studentService.getFeeStatusDistribution());
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/students/seed-demo")
    public String seedDemoStudents(RedirectAttributes redirectAttributes) {
        Object[][] demoData = {
                {"23CSE001", "Lakshmi Prasanna", "lakshmi.p@campus.edu", "Computer Science", "Semester 6", 8.85, 94, 85000.0, 85000.0},
                {"23CSE015", "Jahanvi Sharma", "jahanvi.s@campus.edu", "Computer Science", "Semester 6", 9.20, 96, 85000.0, 50000.0},
                {"23CSE045", "Chandini Mohammad", "mohammadchandini.23.cse@anits.edu.in", "Computer Science", "Semester 6", 9.55, 98, 85000.0, 85000.0},
                {"23IT022", "Marcus Vance", "marcus.v@campus.edu", "Information Technology", "Semester 4", 8.40, 88, 75000.0, 75000.0},
                {"23ME034", "Elena Rostova", "elena.r@campus.edu", "Mechanical Engineering", "Semester 8", 8.95, 91, 80000.0, 40000.0},
                {"23EE019", "Devon Miller", "devon.m@campus.edu", "Electrical Engineering", "Semester 2", 7.45, 82, 75000.0, 30000.0},
                {"23BA008", "Aisha Patel", "aisha.p@campus.edu", "Business Administration", "Semester 4", 9.10, 97, 85000.0, 85000.0},
                {"23DS011", "Maya Lin", "maya.l@campus.edu", "Data Science", "Semester 6", 9.35, 95, 90000.0, 90000.0}
        };

        int count = 0;
        for (Object[] row : demoData) {
            String email = (String) row[2];
            if (!studentService.isEmailTaken(email)) {
                Student s = new Student((String) row[1], email, (String) row[3],
                        LocalDate.now().minusDays((long) (Math.random() * 90)));
                s.setRollNumber((String) row[0]);
                s.setSemester((String) row[4]);
                s.setGpa((Double) row[5]);
                s.setAttendance((Integer) row[6]);
                s.setTotalFee((Double) row[7]);
                s.setPaidFee((Double) row[8]);
                s.setFeeDueDate(LocalDate.now().plusDays((long) (Math.random() * 30 - 10)));
                s.recalculateFeeStatus();

                studentService.saveStudent(s);
                count++;
            }
        }

        redirectAttributes.addFlashAttribute("successMessage", "Seeded " + count + " academic & INR fee records (10.0 CGPA scale).");
        return "redirect:/students";
    }
}
