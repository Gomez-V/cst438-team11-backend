package com.cst438.controller;

import java.util.ArrayList;
import java.util.List;
import java.security.Principal;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.cst438.domain.Assignment;
import com.cst438.domain.AssignmentRepository;
import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.domain.Grade;
import com.cst438.domain.GradeRepository;
import com.cst438.dto.AssignmentStudentDTO;
import com.cst438.dto.EnrollmentDTO;
import com.cst438.domain.UserRepository;
import com.cst438.domain.Section;
import com.cst438.domain.User;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class StudentController {

    @Autowired
    AssignmentRepository assignmentRepository;

    @Autowired
    EnrollmentRepository enrollmentRepository;

    @Autowired
    GradeRepository gradeRepository;

    @Autowired
    UserRepository userRepository;

    /**
     students lists their enrollments given year and semester value
     returns list of enrollments, may be empty
     logged in user must be the student (assignment 7)
     */
   @PreAuthorize("hasAuthority('SCOPE_ROLE_STUDENT')")
   @GetMapping("/enrollments")
    public List<EnrollmentDTO> getSchedule(
            @RequestParam("year") int year,
            @RequestParam("semester") String semester,
            Principal principal) { 

        String email = principal.getName();
        User student = userRepository.findByEmail(email);
        if (student == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found");
        }

        List<Enrollment> enrollments = enrollmentRepository.findByYearAndSemesterOrderByCourseId(
                year, semester, student.getId()); 

        List<EnrollmentDTO> dtoList = new ArrayList<>();
        for (Enrollment e : enrollments) {
            dtoList.add(new EnrollmentDTO(
                    e.getEnrollmentId(),
                    e.getGrade(),
                    e.getUser().getId(),
                    e.getUser().getName(),
                    e.getUser().getEmail(),
                    e.getSection().getCourse().getCourseId(),
                    e.getSection().getCourse().getTitle(),
                    e.getSection().getSecId(),
                    e.getSection().getSectionNo(),
                    e.getSection().getBuilding(),
                    e.getSection().getRoom(),
                    e.getSection().getTimes(),
                    e.getSection().getCourse().getCredits(),
                    e.getSection().getTerm().getYear(),
                    e.getSection().getTerm().getSemester()
          ));
        }
        return dtoList;
   }

    /**
     students lists their assignments given year and semester value
     returns list of assignments may be empty
     logged in user must be the student (assignment 7)
     */
    @PreAuthorize("hasAuthority('SCOPE_ROLE_STUDENT')")
    @GetMapping("/assignments")
    public List<AssignmentStudentDTO> getStudentAssignments(
            @RequestParam("year") int year,
            @RequestParam("semester") String semester,
            Principal principal) { 

        String email = principal.getName(); 
        User student = userRepository.findByEmail(email);
        if (student == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found");
        }

        int studentId = student.getId(); 
        List<Assignment> assignments = assignmentRepository.findByStudentIdAndYearAndSemesterOrderByDueDate(
                studentId, year, semester); 

        List<AssignmentStudentDTO> dtoList = new ArrayList<>();
        for (Assignment a : assignments) {
            Enrollment enrollment = enrollmentRepository.findEnrollmentBySectionNoAndStudentId(
                    a.getSection().getSectionNo(), studentId);
            Grade grade = null;
            if (enrollment != null) {
                grade = gradeRepository.findByEnrollmentIdAndAssignmentId(
                        enrollment.getEnrollmentId(),
                        a.getAssignmentId());
            }
            Integer score = grade != null ? grade.getScore() : null;
            dtoList.add(new AssignmentStudentDTO(
                    a.getAssignmentId(),
                    a.getTitle(),
                    a.getDueDate(),
                    a.getSection().getCourse().getCourseId(),
                    a.getSection().getSecId(),
                    score
            ));
        }
        return dtoList;
    }

}
