package com.cst438.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.access.prepost.PreAuthorize;

import com.cst438.domain.Course;
import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.domain.Section;
import com.cst438.domain.SectionRepository;
import com.cst438.domain.Term;
import com.cst438.domain.User;
import com.cst438.domain.UserRepository;
import com.cst438.dto.EnrollmentDTO;



@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class StudentScheduleController {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     students lists their transcript containing all enrollments
     returns list of enrollments in chronological order
     logged in user must be the student (assignment 7)
     example URL  /transcript?studentId=19803
     */
    @PreAuthorize("hasAuthority('SCOPE_ROLE_STUDENT')")
    @GetMapping("/transcripts")
    public List<EnrollmentDTO> getTranscript(Principal principal) {
        String email = principal.getName(); 
        User student = userRepository.findByEmail(email);
        if (student == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found");
        }

        List<Enrollment> enrollments = enrollmentRepository
                .findEnrollmentsByStudentIdOrderByTermId(student.getId()); 

        List<EnrollmentDTO> result = new ArrayList<>();
        for (Enrollment e : enrollments) {
            Section section = e.getSection();
            Term term = (section != null) ? section.getTerm() : null;
            Course course = (section != null) ? section.getCourse() : null;

            EnrollmentDTO dto = new EnrollmentDTO(
                e.getEnrollmentId(),
                e.getGrade(),
                e.getUser().getId(),
                e.getUser().getName(),
                e.getUser().getEmail(),
                course.getCourseId(),
                course.getTitle(),
                section.getSecId(),
                section.getSectionNo(),
                section.getBuilding(),
                section.getRoom(),
                section.getTimes(),
                course.getCredits(),
                term.getYear(),
                term.getSemester()
            );
            result.add(dto);
        }
        return result;
    }

    /**
     students enrolls into a section of a course
     returns the enrollment data including primary key
     logged in user must be the student (assignment 7)
     */
    @PreAuthorize("hasAuthority('SCOPE_ROLE_STUDENT')")
    @PostMapping("/enrollments/sections/{sectionNo}")
    public EnrollmentDTO addCourse(@PathVariable int sectionNo, Principal principal) { 
        String email = principal.getName(); 
        User student = userRepository.findByEmail(email);
        if (student == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found");
        }

        Section section = sectionRepository.findById(sectionNo)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Section not found for sectionNo=" + sectionNo
            ));

        LocalDate today = LocalDate.now();
        if (section.getTerm().getAddDeadline() != null
                && section.getTerm().getAddDate() != null) {
            LocalDate addStart = section.getTerm().getAddDate().toLocalDate();
            LocalDate addEnd = section.getTerm().getAddDeadline().toLocalDate();
            if (today.isBefore(addStart) || today.isAfter(addEnd)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Not within the add period for this section.");
            }
        }

        Enrollment existingEnrollment =
                enrollmentRepository.findEnrollmentBySectionNoAndStudentId(sectionNo, student.getId());
        if (existingEnrollment != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Already enrolled in section " + sectionNo);
        }

        Enrollment e = new Enrollment();
        e.setUser(student);
        e.setSection(section);
        e.setGrade(null);
        enrollmentRepository.save(e);

        return new EnrollmentDTO(
            e.getEnrollmentId(),
            null,
            student.getId(),
            student.getName(),
            student.getEmail(),
            section.getCourse().getCourseId(),
            section.getCourse().getTitle(),
            section.getSecId(),
            section.getSectionNo(),
            section.getBuilding(),
            section.getRoom(),
            section.getTimes(),
            section.getCourse().getCredits(),
            section.getTerm().getYear(),
            section.getTerm().getSemester()
        );
    }

    /**
     students drops an enrollment for a section
     logged in user must be the student (assignment 7)
     */
    @PreAuthorize("hasAuthority('SCOPE_ROLE_STUDENT')")
    @DeleteMapping("/enrollments/{enrollmentId}")
    public void dropCourse(@PathVariable("enrollmentId") int enrollmentId, Principal principal) { 
        String email = principal.getName(); 
        User student = userRepository.findByEmail(email);
        if (student == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found");
        }

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Enrollment not found for ID=" + enrollmentId
                ));

        if (enrollment.getUser().getId() != student.getId()) { 
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You can only drop your own enrollment.");
        }

        // Check that today is before the dropDeadline
        LocalDate today = LocalDate.now();
        if (enrollment.getSection().getTerm().getDropDeadline() != null) {
            LocalDate dropEnd = enrollment.getSection().getTerm()
                    .getDropDeadline().toLocalDate();
            if (today.isAfter(dropEnd)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Cannot drop after the drop deadline."
                );
            }
        }

        // Delete the enrollment
        enrollmentRepository.delete(enrollment);
    }
}
