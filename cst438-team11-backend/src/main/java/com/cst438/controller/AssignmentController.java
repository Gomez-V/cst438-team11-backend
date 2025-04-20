package com.cst438.controller;

import com.cst438.dto.AssignmentDTO;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.http.HttpStatus;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import java.security.Principal;

import com.cst438.domain.Assignment;
import com.cst438.domain.AssignmentRepository;
import com.cst438.domain.Section;
import com.cst438.domain.SectionRepository;
import com.cst438.dto.AssignmentDTO;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class AssignmentController {

    /**
     instructor lists assignments for a section.
     Assignment data is returned ordered by due date.
     logged in user must be the instructor for the section (assignment 7)
     */
    @Autowired
    AssignmentRepository assignmentRepository;

    @Autowired
    SectionRepository sectionRepository;

    @PreAuthorize("hasAuthority('SCOPE_ROLE_INSTRUCTOR')")
    @GetMapping("/sections/{secNo}/assignments")
    public List<AssignmentDTO> getAssignments(@PathVariable("secNo") int secNo, Principal principal) { 
        Section section = sectionRepository.findById(secNo).orElse(null);
        if (section == null || !section.getInstructorEmail().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authorized for this section");
        }

        List<Assignment> assignments = assignmentRepository.findBySectionNoOrderByDueDate(secNo);
        List<AssignmentDTO> dtoList = new ArrayList<>();
        for (Assignment a : assignments) {
            dtoList.add(new AssignmentDTO(
                a.getAssignmentId(),
                a.getTitle(),
                a.getDueDate().toString(),
                a.getSection().getCourse().getCourseId(),
                a.getSection().getSecId(),
                a.getSection().getSectionNo()));
        }
        return dtoList;
        //TODO: Is this done?
    }

    /**
     instructor creates an assignment for a section.
     Assignment data with primary key is returned.
     logged in user must be the instructor for the section (assignment 7)
     */
    @PreAuthorize("hasAuthority('SCOPE_ROLE_INSTRUCTOR')")
    @PostMapping("/assignments")
    public AssignmentDTO createAssignment(@RequestBody AssignmentDTO dto, Principal principal) { 
        Section section = sectionRepository.findById(dto.secNo()).orElse(null);
        if (section == null || !section.getInstructorEmail().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authorized for this section");
        }

        LocalDate endDate = section.getTerm().getEndDate().toLocalDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate assignDate = LocalDate.parse(dto.dueDate(), formatter);

        if (assignDate.isAfter(endDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assignment due date is after section end date.");
        }

        Assignment a = new Assignment();
        a.setSection(section);
        a.setDueDate(Date.valueOf(dto.dueDate()));
        a.setTitle(dto.title());
        assignmentRepository.save(a);

        return new AssignmentDTO(
            a.getAssignmentId(),
            a.getTitle(),
            a.getDueDate().toString(),
            a.getSection().getCourse().getCourseId(),
            a.getSection().getSecId(),
            a.getSection().getSectionNo()
        );

        // TODO: Is this done?
    }

    /**
     instructor updates an assignment for a section.
     only title and dueDate may be changed
     updated assignment data is returned
     logged in user must be the instructor for the section (assignment 7)
     */
    @PreAuthorize("hasAuthority('SCOPE_ROLE_INSTRUCTOR')")
    @PutMapping("/assignments")
    public AssignmentDTO updateAssignment(@RequestBody AssignmentDTO dto, Principal principal) {
        Assignment a = assignmentRepository.findById(dto.id()).orElse(null);
        if (a == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found");
        }

        Section section = sectionRepository.findById(dto.secNo()).orElse(null);
        if (section == null || !section.getInstructorEmail().equals(principal.getName())) { 
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authorized for this section");
        }

        a.setTitle(dto.title());
        a.setDueDate(Date.valueOf(dto.dueDate()));
        assignmentRepository.save(a);

        return new AssignmentDTO( 
            a.getAssignmentId(),
            a.getTitle(),
            a.getDueDate().toString(),
            a.getSection().getCourse().getCourseId(),
            a.getSection().getSecId(),
            a.getSection().getSectionNo()
        );
    }

    /**
     instructor deletes an assignment for a section.
     logged in user must be the instructor for the section (assignment 7)
     */
    @PreAuthorize("hasAuthority('SCOPE_ROLE_INSTRUCTOR')")
    @DeleteMapping("/assignments/{assignmentId}")
    public void deleteAssignment(@PathVariable("assignmentId") int assignmentId, Principal principal) { 
        Assignment a = assignmentRepository.findById(assignmentId).orElse(null);
        if (a == null) return;

        Section section = a.getSection();
        if (!section.getInstructorEmail().equals(principal.getName())) { 
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authorized for this section");
        }

        assignmentRepository.delete(a);
    }
}
