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
                regDate = LocalDate.now();
            }
        }

        Student student = new Student(trimmedName, trimmedEmail, trimmedCourse, regDate);
        studentService.saveStudent(student);

        redirectAttributes.addFlashAttribute("successMessage", "Student '" + trimmedName + "' enrolled successfully!");
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

        Student updatedStudent = new Student(trimmedName, trimmedEmail, trimmedCourse, regDate);
        studentService.updateStudent(id, updatedStudent);

        redirectAttributes.addFlashAttribute("successMessage", "Student record for '" + trimmedName + "' updated successfully.");
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
            // Write BOM for Excel compatibility
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
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/students/seed-demo")
    public String seedDemoStudents(RedirectAttributes redirectAttributes) {
        String[][] demoData = {
                {"Alexandre Chen", "alex.chen@campus.edu", "Computer Science"},
                {"Sophia Rodriguez", "sophia.r@campus.edu", "Data Science"},
                {"Marcus Vance", "marcus.vance@campus.edu", "Information Technology"},
                {"Elena Rostova", "elena.rostova@campus.edu", "Mechanical Engineering"},
                {"Devon Miller", "d.miller@campus.edu", "Electrical Engineering"},
                {"Aisha Patel", "aisha.patel@campus.edu", "Business Administration"},
                {"Liam O'Connor", "liam.oc@campus.edu", "Computer Science"},
                {"Maya Lin", "maya.lin@campus.edu", "Data Science"}
        };

        int count = 0;
        for (String[] row : demoData) {
            if (!studentService.isEmailTaken(row[1])) {
                Student s = new Student(row[0], row[1], row[2], LocalDate.now().minusDays((long) (Math.random() * 60)));
                studentService.saveStudent(s);
                count++;
            }
        }

        redirectAttributes.addFlashAttribute("successMessage", "Seeded " + count + " demonstration student records across departments.");
        return "redirect:/students";
    }
}
