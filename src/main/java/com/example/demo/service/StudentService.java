package com.example.demo.service;

import com.example.demo.model.ActivityLog;
import com.example.demo.model.Student;
import com.example.demo.repository.ActivityLogRepository;
import com.example.demo.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final ActivityLogRepository activityLogRepository;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,63}$"
    );

    public StudentService(StudentRepository studentRepository, ActivityLogRepository activityLogRepository) {
        this.studentRepository = studentRepository;
        this.activityLogRepository = activityLogRepository;
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    @Transactional
    public Student saveStudent(Student student) {
        if (student.getRegistrationDate() == null) {
            student.setRegistrationDate(LocalDate.now());
        }
        student.recalculateFeeStatus();
        Student saved = studentRepository.save(student);
        logActivity("CREATE", "Added student " + saved.getName() + " (" + saved.getCourse() + ")", saved.getName());
        return saved;
    }

    public Student getStudentById(Long id) {
        return studentRepository.findById(id).orElse(null);
    }

    @Transactional
    public void updateStudent(Long id, Student updatedStudent) {
        Student student = studentRepository.findById(id).orElse(null);

        if (student != null) {
            student.setName(updatedStudent.getName());
            student.setEmail(updatedStudent.getEmail());
            student.setCourse(updatedStudent.getCourse());

            if (updatedStudent.getRollNumber() != null) {
                student.setRollNumber(updatedStudent.getRollNumber());
            }
            if (updatedStudent.getSemester() != null) {
                student.setSemester(updatedStudent.getSemester());
            }
            if (updatedStudent.getGpa() != null) {
                student.setGpa(updatedStudent.getGpa());
            }
            if (updatedStudent.getAttendance() != null) {
                student.setAttendance(updatedStudent.getAttendance());
            }
            if (updatedStudent.getAcademicStatus() != null) {
                student.setAcademicStatus(updatedStudent.getAcademicStatus());
            }
            if (updatedStudent.getTotalFee() != null) {
                student.setTotalFee(updatedStudent.getTotalFee());
            }
            if (updatedStudent.getPaidFee() != null) {
                student.setPaidFee(updatedStudent.getPaidFee());
            }
            if (updatedStudent.getFeeDueDate() != null) {
                student.setFeeDueDate(updatedStudent.getFeeDueDate());
            }
            if (updatedStudent.getRegistrationDate() != null) {
                student.setRegistrationDate(updatedStudent.getRegistrationDate());
            }

            student.recalculateFeeStatus();
            studentRepository.save(student);
            logActivity("UPDATE", "Updated academic & fee records for " + student.getName(), student.getName());
        }
    }

    @Transactional
    public void recordPayment(Long id, Double paymentAmount) {
        Student student = studentRepository.findById(id).orElse(null);
        if (student != null && paymentAmount != null && paymentAmount > 0) {
            double currentPaid = student.getPaidFee();
            student.setPaidFee(currentPaid + paymentAmount);
            student.recalculateFeeStatus();
            studentRepository.save(student);
            logActivity("PAYMENT", String.format("Recorded payment of $%.2f for %s (Remaining Due: $%.2f)",
                    paymentAmount, student.getName(), student.getDueFee()), student.getName());
        }
    }

    @Transactional
    public void deleteStudent(Long id) {
        Student student = studentRepository.findById(id).orElse(null);
        if (student != null) {
            String name = student.getName();
            studentRepository.deleteById(id);
            logActivity("DELETE", "Deleted student record for " + name, name);
        }
    }

    @Transactional
    public int deleteStudentsBulk(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        List<Student> students = studentRepository.findAllById(ids);
        int count = students.size();
        studentRepository.deleteAll(students);
        logActivity("BULK_DELETE", "Bulk deleted " + count + " student records", count + " students");
        return count;
    }

    public boolean isEmailTaken(String email) {
        if (email == null) return false;
        return studentRepository.existsByEmail(email.trim().toLowerCase());
    }

    public boolean isEmailTakenByAnother(String email, Long currentId) {
        if (email == null) return false;
        return studentRepository.existsByEmailAndIdNot(email.trim().toLowerCase(), currentId);
    }

    public List<String> getDistinctCourses() {
        List<String> list = studentRepository.findDistinctCourses();
        if (list == null || list.isEmpty()) {
            return List.of("Computer Science", "Information Technology", "Electronics", "Mechanical Engineering");
        }
        return list;
    }

    public Map<String, Long> getDepartmentDistribution() {
        List<Student> students = studentRepository.findAll();
        Map<String, Long> distribution = new TreeMap<>();
        for (Student s : students) {
            String dept = (s.getCourse() != null && !s.getCourse().trim().isEmpty())
                    ? s.getCourse().trim()
                    : "Unassigned";
            distribution.put(dept, distribution.getOrDefault(dept, 0L) + 1);
        }
        return distribution;
    }

    public long getNewStudentsThisMonthCount() {
        List<Student> students = studentRepository.findAll();
        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();

        return students.stream().filter(s -> {
            LocalDate reg = s.getRegistrationDate();
            return reg != null && reg.getYear() == currentYear && reg.getMonthValue() == currentMonth;
        }).count();
    }

    public double getTotalFeesCollected() {
        return studentRepository.findAll().stream()
                .mapToDouble(Student::getPaidFee)
                .sum();
    }

    public double getTotalFeesPending() {
        return studentRepository.findAll().stream()
                .mapToDouble(Student::getDueFee)
                .sum();
    }

    public double getCollectionRate() {
        double collected = getTotalFeesCollected();
        double pending = getTotalFeesPending();
        double total = collected + pending;
        if (total <= 0) return 100.0;
        return Math.round((collected / total) * 1000.0) / 10.0;
    }

    public double getAverageGpa() {
        List<Student> students = studentRepository.findAll();
        if (students.isEmpty()) return 3.5;
        double avg = students.stream().mapToDouble(Student::getGpa).average().orElse(3.5);
        return Math.round(avg * 100.0) / 100.0;
    }

    public int getAverageAttendance() {
        List<Student> students = studentRepository.findAll();
        if (students.isEmpty()) return 90;
        return (int) Math.round(students.stream().mapToInt(Student::getAttendance).average().orElse(90.0));
    }

    public Map<String, Long> getFeeStatusDistribution() {
        List<Student> students = studentRepository.findAll();
        Map<String, Long> map = new HashMap<>();
        map.put("PAID", students.stream().filter(s -> "PAID".equalsIgnoreCase(s.getFeeStatus())).count());
        map.put("PARTIAL", students.stream().filter(s -> "PARTIAL".equalsIgnoreCase(s.getFeeStatus())).count());
        map.put("DUE", students.stream().filter(s -> "DUE".equalsIgnoreCase(s.getFeeStatus()) || "OVERDUE".equalsIgnoreCase(s.getFeeStatus())).count());
        return map;
    }

    public List<Student> getRecentlyAddedStudents(int limit) {
        return studentRepository.findAll().stream()
                .sorted((a, b) -> Long.compare(b.getId() != null ? b.getId() : 0, a.getId() != null ? a.getId() : 0))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<ActivityLog> getRecentActivities() {
        List<ActivityLog> logs = activityLogRepository.findTop15ByOrderByTimestampDesc();
        if (logs.isEmpty()) {
            ActivityLog initial = new ActivityLog("SYSTEM", "System initialized with Academic & Fee modules", "System");
            activityLogRepository.save(initial);
            logs = List.of(initial);
        }
        return logs;
    }

    public void logActivity(String action, String description, String studentName) {
        try {
            ActivityLog log = new ActivityLog(action, description, studentName);
            activityLogRepository.save(log);
        } catch (Exception ignored) {
        }
    }

    public void exportStudentsToCsv(Writer writer) throws IOException {
        PrintWriter pw = new PrintWriter(writer);
        pw.println("ID,Roll Number,Name,Email,Department,Semester,GPA,Attendance %,Total Fee,Paid Fee,Due Balance,Fee Status,Registration Date");

        List<Student> students = studentRepository.findAll();
        for (Student s : students) {
            pw.printf("%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%.2f,%d,%.2f,%.2f,%.2f,\"%s\",%s%n",
                    s.getId() != null ? s.getId() : 0,
                    escapeCsv(s.getRollNumber()),
                    escapeCsv(s.getName()),
                    escapeCsv(s.getEmail()),
                    escapeCsv(s.getCourse()),
                    escapeCsv(s.getSemester()),
                    s.getGpa(),
                    s.getAttendance(),
                    s.getTotalFee(),
                    s.getPaidFee(),
                    s.getDueFee(),
                    escapeCsv(s.getFeeStatus()),
                    s.getRegistrationDate() != null ? s.getRegistrationDate().toString() : LocalDate.now().toString()
            );
        }
        pw.flush();
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }

    public static class CsvImportResult {
        private int totalProcessed = 0;
        private int importedCount = 0;
        private int skippedCount = 0;
        private final List<String> errorMessages = new ArrayList<>();

        public int getTotalProcessed() { return totalProcessed; }
        public int getImportedCount() { return importedCount; }
        public int getSkippedCount() { return skippedCount; }
        public List<String> getErrorMessages() { return errorMessages; }
    }

    @Transactional
    public CsvImportResult importStudentsFromCsv(InputStream inputStream) throws IOException {
        CsvImportResult result = new CsvImportResult();
        List<Student> studentsToSave = new ArrayList<>();
        Set<String> processedEmailsInFile = new HashSet<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line = reader.readLine(); // Header
            if (line == null) {
                result.getErrorMessages().add("The uploaded CSV file is empty.");
                return result;
            }

            int lineNum = 1;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                result.totalProcessed++;
                String[] parts = parseCsvLine(line);

                if (parts.length < 3) {
                    result.skippedCount++;
                    result.getErrorMessages().add("Line " + lineNum + ": Insufficient columns (expected Name, Email, Department).");
                    continue;
                }

                if (parts[0].equalsIgnoreCase("name") && parts[1].toLowerCase().contains("email")) {
                    continue;
                }

                // Format can be: Name, Email, Course... OR ID, RollNo, Name, Email, Course...
                int nameIdx = 0;
                int emailIdx = 1;
                int courseIdx = 2;

                if (parts[0].matches("^\\d+$") && parts.length >= 5) {
                    nameIdx = 2;
                    emailIdx = 3;
                    courseIdx = 4;
                }

                String name = parts[nameIdx].trim();
                String email = parts[emailIdx].trim().toLowerCase();
                String course = parts[courseIdx].trim();

                if (name.isEmpty()) {
                    result.skippedCount++;
                    result.getErrorMessages().add("Line " + lineNum + ": Student name is empty.");
                    continue;
                }

                if (!EMAIL_PATTERN.matcher(email).matches()) {
                    result.skippedCount++;
                    result.getErrorMessages().add("Line " + lineNum + ": Invalid email format for '" + email + "'.");
                    continue;
                }

                if (course.isEmpty()) {
                    course = "General";
                }

                if (processedEmailsInFile.contains(email)) {
                    result.skippedCount++;
                    result.getErrorMessages().add("Line " + lineNum + ": Duplicate email in file for '" + email + "'.");
                    continue;
                }

                if (studentRepository.existsByEmail(email)) {
                    result.skippedCount++;
                    result.getErrorMessages().add("Line " + lineNum + ": Email '" + email + "' already registered in database.");
                    continue;
                }

                processedEmailsInFile.add(email);
                Student student = new Student(name, email, course);
                studentsToSave.add(student);
            }
        }

        if (!studentsToSave.isEmpty()) {
            studentRepository.saveAll(studentsToSave);
            result.importedCount = studentsToSave.size();
            logActivity("IMPORT", "Imported " + result.importedCount + " students via CSV file", result.importedCount + " students");
        }

        return result;
    }

    private String[] parseCsvLine(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '\"') {
                    sb.append('\"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString().trim());
        return tokens.toArray(new String[0]);
    }
}
