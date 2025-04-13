package com.cst438.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
    @GetMapping("/transcripts")
    public List<EnrollmentDTO> getTranscript(@RequestParam("studentId") int studentId) {

     // Fetch all enrollments for this student, sorted by term ID (or by year/semester).
        List<Enrollment> enrollments = enrollmentRepository
                .findEnrollmentsByStudentIdOrderByTermId(studentId);

        // Convert each Enrollment to an EnrollmentDTO
        List<EnrollmentDTO> result = new ArrayList<>();
        for (Enrollment e : enrollments) {
            // gather references
            Section section = e.getSection();
            Term term = (section != null) ? section.getTerm() : null;
            Course course = (section != null) ? section.getCourse() : null;
            User user = e.getUser();

            // build EnrollmentDTO (adjust fields to match your actual DTO constructor)
            EnrollmentDTO dto = new EnrollmentDTO(
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
    @PostMapping("/enrollments/sections/{sectionNo}")
    public EnrollmentDTO addCourse(
            @PathVariable int sectionNo,
            @RequestParam("studentId") int studentId ) {

        // Verify the section exists
        Section section = sectionRepository.findById(sectionNo)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Section not found for sectionNo=" + sectionNo
            ));

        User student = userRepository.findById(studentId)
            .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Student not found for userId=" + studentId
            ));

         //  Check the current date is between addDate and addDeadline
        LocalDate today = LocalDate.now();
        if (section.getTerm().getAddDeadline() != null
                && section.getTerm().getAddDate() != null) {
            LocalDate addStart = section.getTerm().getAddDate().toLocalDate();
            LocalDate addEnd = section.getTerm().getAddDeadline().toLocalDate();
            if (today.isBefore(addStart) || today.isAfter(addEnd)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Not within the add period for this section."
                );
            }
        }
       //  Check that the student is not already enrolled in this section
        Enrollment existingEnrollment =
                enrollmentRepository.findEnrollmentBySectionNoAndStudentId(sectionNo, studentId);
        if (existingEnrollment != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Student " + studentId + " is already enrolled in section " + sectionNo
            );
        }

        //  Create and save a new Enrollment (grade defaults to null)
        Enrollment e = new Enrollment();
        e.setUser(student);
        e.setSection(section);
        e.setGrade(null);

        enrollmentRepository.save(e);

        return new EnrollmentDTO(
            e.getEnrollmentId(),
            null,
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
        );
    }

    /**
     students drops an enrollment for a section
     logged in user must be the student (assignment 7)
     */
    @DeleteMapping("/enrollments/{enrollmentId}")
    public void dropCourse(@PathVariable("enrollmentId") int enrollmentId) {

     // Find the enrollment
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Enrollment not found for ID=" + enrollmentId
                ));

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
