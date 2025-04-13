package com.cst438.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.cst438.domain.AssignmentRepository;
import com.cst438.domain.Assignment;
import com.cst438.dto.AssignmentDTO;
import com.cst438.domain.Grade;
import com.cst438.domain.GradeRepository;
import com.cst438.dto.GradeDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/*
 * Unit test to add grade to existing assignment
 */

//TODO: review functionality and if needed, create more unit tests.

@AutoConfigureMockMvc
@SpringBootTest
public class GradeControllerUnitTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    GradeRepository gradeRepository;

    @Autowired
    AssignmentRepository assignmentRepository;


    //TODO: COMPLETE: Instructor grades a single assignment
    @Test
    public void addGrade() throws Exception {

        MockHttpServletResponse response;

        //1. Find the assignment and return the grade object from assignment


        //2. Update the grade for the given assignment, based on the assignmentId

        // create DTO with data for new grade.
        // the primary key, secNo, is set to 0. it will be
        // set by the database when the grade is updated.

        int assignmentId = 1;

        response = mvc.perform(
                MockMvcRequestBuilders
                    .get("/assignments/" + assignmentId + "/grades"))
            .andReturn()
            .getResponse();

        // check the response code for 200 meaning OK
        assertEquals(200, response.getStatus());

        System.out.println(response.getContentAsString());

        // return data converted from String to DTO
        GradeDTO[] result = fromJsonString(response.getContentAsString(), GradeDTO[].class);

        //TODO: ENHANCEMENT: Update so that array indexes are not required for checking

        // primary key should have a non zero value from the database
        assertNotEquals(0, result[0].gradeId());
        // check other fields of the DTO for expected values
        assertEquals(95, result[2].score());

        //Update grade to 98 for assignment

        GradeDTO grade = new GradeDTO(
            1,
            "thomas edison",
            "tedison@csumb.edu",
            "db homework 1",
            "cst363",
            8,
            98
        );

        List<GradeDTO> grades = new ArrayList<>();
        grades.add(grade);

        response = mvc.perform(
                MockMvcRequestBuilders
                    .put("/grades")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJsonString(grades)))
            .andReturn()
            .getResponse();

        assertEquals(200, response.getStatus());

        response = mvc.perform(
                MockMvcRequestBuilders
                    .get("/assignments/" + assignmentId + "/grades"))
            .andReturn()
            .getResponse();

        GradeDTO[] result1 = fromJsonString(response.getContentAsString(), GradeDTO[].class);

        //TODO: ENHANCEMENT: update so that array indexes aren't required for checking

        // check the database
        Grade g = gradeRepository.findById(result1[2].gradeId()).orElse(null);
        assertNotNull(g);
        assertEquals(98, g.getScore());
        assertEquals("cst363", g.getEnrollment().getSection().getCourse().getCourseId());

        //Retrieve null grade for assignment 'db homework 2'
    }

    //TODO: COMPLETE: Instructor grades an assignment and enters scores for all enrolled students and uploads
    //the scores

    @Test
    public void updateAllGradesForAssignment( ) throws Exception {
        MockHttpServletResponse response;

        int assignmentId = 1;

        response = mvc.perform(
                MockMvcRequestBuilders
                    .get("/assignments/"+ assignmentId +"/grades"))
            .andReturn()
            .getResponse();

        assertEquals(200, response.getStatus());

        // return data converted from String to DTO
        GradeDTO[] result = fromJsonString(response.getContentAsString(), GradeDTO[].class);

        assertEquals(3, result.length, "Number of grades does not match expected");

        GradeDTO grade1 = new GradeDTO (
            result[0].gradeId(),
            result[0].studentName(),
            result[0].studentEmail(),
            result[0].assignmentTitle(),
            result[0].courseId(),
            result[0].sectionId(),
            100
        );

        GradeDTO grade2 = new GradeDTO (
            result[1].gradeId(),
            result[1].studentName(),
            result[1].studentEmail(),
            result[1].assignmentTitle(),
            result[1].courseId(),
            result[1].sectionId(),
            100
        );

        GradeDTO grade3 = new GradeDTO (
            result[2].gradeId(),
            result[2].studentName(),
            result[2].studentEmail(),
            result[2].assignmentTitle(),
            result[2].courseId(),
            result[2].sectionId(),
            100
        );

        List<GradeDTO> grades = new ArrayList<>();
        grades.add(grade1);
        grades.add(grade2);
        grades.add(grade3);

        response = mvc.perform(
                MockMvcRequestBuilders
                    .put("/grades")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJsonString(grades)))
            .andReturn()
            .getResponse();

        assertEquals(200, response.getStatus());

        response = mvc.perform(
                MockMvcRequestBuilders
                    .get("/assignments/" + assignmentId + "/grades"))
            .andReturn()
            .getResponse();

        GradeDTO[] resultAfter = fromJsonString(response.getContentAsString(), GradeDTO[].class);

        // check the database
        Grade g1 = gradeRepository.findById(resultAfter[0].gradeId()).orElse(null);
        assertNotNull(g1);
        assertEquals(100, g1.getScore());

        Grade g2 = gradeRepository.findById(resultAfter[1].gradeId()).orElse(null);
        assertNotNull(g2);
        assertEquals(100, g2.getScore());

        Grade g3 = gradeRepository.findById(resultAfter[2].gradeId()).orElse(null);
        assertNotNull(g3);
        assertEquals(100, g3.getScore());
    }

    @Test
    public void getNullGradeFromAssignment( ) throws Exception {

        MockHttpServletResponse response;

        int assignmentId = 2;

        response = mvc.perform(
                MockMvcRequestBuilders
                    .get("/assignments/"+ assignmentId +"/grades"))
            .andReturn()
            .getResponse();

        // check the response code for 200 meaning OK
        assertEquals(200, response.getStatus());

        // return data converted from String to DTO
        GradeDTO[] result = fromJsonString(response.getContentAsString(), GradeDTO[].class);

        // primary key should have a non zero value from the database
        assertNotEquals(0, result[0].gradeId());
        // check other fields of the DTO for expected values
        assertNull(result[0].score());
    }

    @Test
    public void addGradeFailsBadUser( ) throws Exception {

        MockHttpServletResponse response;

        GradeDTO grade = new GradeDTO(
            1,
            "",
            "",
            "",
            "",
            0,
            98
        );

        List<GradeDTO> grades = new ArrayList<>();
        grades.add(grade);

        // issue the POST request
        response = mvc.perform(
                MockMvcRequestBuilders
                    .put("/grades")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJsonString(grades)))
            .andReturn()
            .getResponse();

        // response should be 400, the student info is not correct
        assertEquals(400, response.getStatus());

        // check the expected error message
        String message = response.getErrorMessage();
        assertEquals("Student Name or Email does not match Assignment Enrollment for Grade", message);

    }

    //TODO: COMPLETE: Instructor attempts to grade assignment but assignmentId is invalid
    @Test
    public void addGradeFailAssignmentIdInvalid () throws Exception {
        MockHttpServletResponse response;

        int assignmentId = 15;

        response = mvc.perform(
                MockMvcRequestBuilders
                    .get("/assignments/"+ assignmentId +"/grades"))
            .andReturn()
            .getResponse();

        assertEquals(404, response.getStatus());
        assertEquals("Assignment not found.", response.getErrorMessage());
    }

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> T  fromJsonString(String str, Class<T> valueType ) {
        try {
            return new ObjectMapper().readValue(str, valueType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
