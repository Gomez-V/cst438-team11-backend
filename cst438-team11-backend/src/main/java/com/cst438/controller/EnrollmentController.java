package com.cst438.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.dto.EnrollmentDTO;
import com.cst438.service.RegistrarServiceProxy;


@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class EnrollmentController {

    @Autowired
    EnrollmentRepository enrollmentRepository;
    
    @Autowired
    RegistrarServiceProxy registrarService;
 

    /**
     instructor gets list of enrollments for a section
     list of enrollments returned is in order by student name
     logged in user must be the instructor for the section (assignment 7)
     */
    @GetMapping("/sections/{sectionNo}/enrollments")
    public List<EnrollmentDTO> getEnrollments(
            @PathVariable("sectionNo") int sectionNo ) {
        List<Enrollment> enrollments = enrollmentRepository.findEnrollmentsBySectionNoOrderByStudentName(sectionNo);
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

    // instructor uploads enrollments with the final grades for the section
    // user must be instructor for the section
    /**
     instructor updates enrollment grades
     only the grade attribute of enrollment can be changed
     logged in user must be the instructor for the section (assignment 7)
     */
    @PutMapping("/enrollments")
    public void updateEnrollmentGrade(@RequestBody List<EnrollmentDTO> dlist) {
      for (EnrollmentDTO dto : dlist) {
        Enrollment e = enrollmentRepository.findById(dto.enrollmentId()).orElse(null);
        if (e != null) {
            e.setGrade(dto.grade());
            enrollmentRepository.save(e);

            // Send message to registrar service
           try {
                String msg = "updateFinalGrade " + registrarService.asJsonString(dto);
                registrarService.sendMessage(msg);
            } catch (Exception ex) {
                System.out.println(" Error sending updateFinalGrade to registrar: " + ex.getMessage());
        }
      }
    }

}
