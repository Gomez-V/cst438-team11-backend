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

import com.cst438.domain.Assignment;
import com.cst438.domain.AssignmentRepository;
import com.cst438.domain.Section;
import com.cst438.domain.SectionRepository;
import com.cst438.dto.AssignmentDTO;

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

    @GetMapping("/sections/{secNo}/assignments")
    public List<AssignmentDTO> getAssignments(
            @PathVariable("secNo") int secNo) {
		
		// hint: use the assignment repository method 
		//  findBySectionNoOrderByDueDate to return 
		//  a list of assignments

        List<Assignment> assignments = assignmentRepository.findBySectionNoOrderByDueDate(secNo);
        List<AssignmentDTO> dtoList = new ArrayList<>();
        for (Assignment a : assignments) {
            dtoList.add(new AssignmentDTO(a.getAssignmentId(), a.getTitle(), a.getDueDate().toString(), a.getSection().getCourse().getCourseId(), a.getSection().getSecId(), a.getSection().getSectionNo()));
        }
        return dtoList;
        //TODO: Is this done?
    }

    /**
     instructor creates an assignment for a section.
     Assignment data with primary key is returned.
     logged in user must be the instructor for the section (assignment 7)
     */
    @PostMapping("/assignments")
    public AssignmentDTO createAssignment(
            @RequestBody AssignmentDTO dto) {
        Section section = sectionRepository.findById(dto.secNo()).orElse(null);
        if (section == null ){
            throw  new ResponseStatusException( HttpStatus.NOT_FOUND, "section not found "+dto.secNo());
        }

        LocalDate endDate = section.getTerm().getEndDate().toLocalDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate assignDate = LocalDate.parse(dto.dueDate(), formatter);

        if (assignDate.isAfter(endDate)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Assignment due date is after section end date."
            );
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
    @PutMapping("/assignments")
    public AssignmentDTO updateAssignment(@RequestBody AssignmentDTO dto) {

        Assignment a = assignmentRepository.findById(dto.id()).orElse(null);
        if (a == null ){
            throw  new ResponseStatusException( HttpStatus.NOT_FOUND, "assignment not found "+dto.id());
        }
        Section section = sectionRepository.findById(dto.secNo()).orElse(null);
        if (section == null ){
            throw  new ResponseStatusException( HttpStatus.NOT_FOUND, "section not found "+dto.secNo());
        }
        a.setAssignmentId(dto.id());
        a.setTitle(dto.title());
        a.setSection(section); //TODO: Does this need to be removed?
        a.setDueDate(Date.valueOf(dto.dueDate()));
        assignmentRepository.save(a);


        // TODO: is this done?

        return null;
    }

    /**
     instructor deletes an assignment for a section.
     logged in user must be the instructor for the section (assignment 7)
     */
    @DeleteMapping("/assignments/{assignmentId}")
    public void deleteAssignment(@PathVariable("assignmentId") int assignmentId) {

        Assignment a = assignmentRepository.findById(assignmentId).orElse(null);
        if(a != null) {
            assignmentRepository.delete(a);
        }
        // TODO: Is this done?
    }
}
