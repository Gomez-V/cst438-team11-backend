package com.cst438.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cst438.domain.Assignment;
import com.cst438.domain.AssignmentRepository;
import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.domain.Grade;
import com.cst438.domain.GradeRepository;
import com.cst438.dto.AssignmentStudentDTO;
import com.cst438.dto.EnrollmentDTO;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class StudentController {

    @Autowired
    AssignmentRepository assignmentRepository;

    @Autowired
    EnrollmentRepository enrollmentRepository;

    @Autowired
    GradeRepository gradeRepository;

    /**
     students lists their enrollments given year and semester value
     returns list of enrollments, may be empty
     logged in user must be the student (assignment 7)
     */
   @GetMapping("/enrollments")
   public List<EnrollmentDTO> getSchedule(
           @RequestParam("year") int year,
           @RequestParam("semester") String semester,
           @RequestParam("studentId") int studentId) {
      List<Enrollment> enrollments = enrollmentRepository.findByYearAndSemesterOrderByCourseId(year, semester, studentId);
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


}
