package com.example.demo.controller;

import com.example.demo.model.Student;
import com.example.demo.service.StudentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("students", studentService.getAllStudents());
        return "index";
    }

    @PostMapping("/students")
    public String addStudent(@RequestParam String name,
                             @RequestParam String email,
                             @RequestParam String course) {

        Student student = new Student(name, email, course);
        studentService.saveStudent(student);

        return "redirect:/";
    }

    @GetMapping("/students/edit/{id}")
    public String editStudent(@PathVariable Long id, Model model) {
        Student student = studentService.getStudentById(id);

        if (student == null) {
            return "redirect:/";
        }

        model.addAttribute("student", student);
        return "edit";
    }

    @PostMapping("/students/update/{id}")
    public String updateStudent(@PathVariable Long id,
                                @RequestParam String name,
                                @RequestParam String email,
                                @RequestParam String course) {

        Student updatedStudent = new Student(name, email, course);
        studentService.updateStudent(id, updatedStudent);

        return "redirect:/";
    }

    @GetMapping("/students/delete/{id}")
    public String deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return "redirect:/";
    }
}