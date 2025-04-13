package com.cst438.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.cst438.domain.Assignment;
import com.cst438.domain.AssignmentRepository;
import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.domain.Grade;
import com.cst438.domain.GradeRepository;
import com.cst438.dto.GradeDTO;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class GradeController {

    @Autowired
    AssignmentRepository assignmentRepository;

    @Autowired
    GradeRepository gradeRepository;

    @Autowired
    EnrollmentRepository enrollmentRepository;

    // GET GRADES FOR AN ASSIGNMENT
    @GetMapping("/assignments/{assignmentId}/grades")
    public List<GradeDTO> getAssignmentGrades(@PathVariable int assignmentId) {

        // Find the assignment
        Assignment assignment = assignmentRepository.findById(assignmentId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Assignment not found."
            ));

        // Get the section
        int sectionNo = assignment.getSection().getSectionNo();

        // Get all enrollments for that section
        List<Enrollment> enrollments = enrollmentRepository.findEnrollmentsBySectionNoOrderByStudentName(sectionNo);

        // Build the result list
        List<GradeDTO> result = new ArrayList<>();

        // For each enrollment, find or create a Grade
        for (Enrollment e : enrollments) {
            Grade g = gradeRepository.findByEnrollmentIdAndAssignmentId(e.getEnrollmentId(), assignmentId);
            if (g == null) {
                g = new Grade();
                g.setAssignment(assignment);
                g.setEnrollment(e);
                g.setScore(null);
                g = gradeRepository.save(g);
            }

        
            // Adjust fields to match your actual GradeDTO constructor
            GradeDTO dto = new GradeDTO(
                g.getGradeId(),
                e.getUser().getName(),
                e.getUser().getEmail(),
                assignment.getTitle(),
                String.valueOf(assignment.getSection().getCourse().getCourseId()),
                assignment.getSection().getSectionNo(),
                (g.getScore() != null) ? g.getScore() : null
            );
            result.add(dto);
        }

        return result;
    }

    // UPDATE GRADES
    @PutMapping("/grades")
    public void updateGrades(@RequestBody List<GradeDTO> dlist) {

        // For each GradeDTO, find the Grade by ID, update score, save
        for (GradeDTO dto : dlist) {
            Grade g = gradeRepository.findById(dto.gradeId())
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Grade not found for ID=" + dto.gradeId()
                ));
            // set score

            if(!g.getEnrollment().getUser().getEmail().equals(dto.studentEmail()) ||
                !g.getEnrollment().getUser().getName().equals(dto.studentName())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Student Name or Email does not match Assignment Enrollment for Grade");
            }
            g.setScore(dto.score() == null ? null : dto.score());
            gradeRepository.save(g);
        }
    }
}
