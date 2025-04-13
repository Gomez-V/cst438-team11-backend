package com.cst438.controller;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.domain.SectionRepository;
import com.cst438.domain.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@AutoConfigureMockMvc
@SpringBootTest
public class StudentScheduleControllerUnitTest {
  @Autowired
  MockMvc mvc;

  @Autowired
  private EnrollmentRepository enrollmentRepository;

  @Autowired
  private SectionRepository sectionRepository;

  @Autowired
  private UserRepository userRepository;

  //TODO: COMPLETE Student attempts to enroll in section
  @Test
  public void studentEnrollsInSection () throws Exception {
    MockHttpServletResponse response;

    int studentId = 5;
    int sectionNo = 10;

    response = mvc.perform(
            MockMvcRequestBuilders
                .post("/enrollments/sections/"+sectionNo)
                .param("studentId", "5"))
        .andReturn()
        .getResponse();

    assertEquals(200, response.getStatus());

    //check the database
    Enrollment e = enrollmentRepository.findEnrollmentBySectionNoAndStudentId(sectionNo, studentId);
    assertNotNull(e);
    assertNull(e.getGrade());
  }

  //TODO: COMPLETE Student attempts to enroll because student is already enrolled

  @Test
  public void studentEnrollsFailsAlreadyEnrolled () throws Exception {
    MockHttpServletResponse response;

    int sectionNo = 8;

    response = mvc.perform(
            MockMvcRequestBuilders
                .post("/enrollments/sections/"+sectionNo)
                .param("studentId", "3"))
        .andReturn()
        .getResponse();

    assertEquals(400, response.getStatus());
    assertEquals("Student 3 is already enrolled in section 8", response.getErrorMessage());
  }

  //TODO: COMPLETE: Student attempts to enroll in a section but the section number is invalid
  @Test
  public void studentEnrollsFailsSectionNoInvalid () throws Exception {
    MockHttpServletResponse response;

    int sectionNo = 15;

    response = mvc.perform(
            MockMvcRequestBuilders
                .post("/enrollments/sections/"+sectionNo)
                .param("studentId", "3"))
        .andReturn()
        .getResponse();

    assertEquals(400, response.getStatus());
    assertEquals("Section not found for sectionNo=15", response.getErrorMessage());
  }

  //TODO: COMPLETE: Student attempts to enroll in a section but it is past the add deadline
  @Test
  public void studentEnrollsFailsPastDeadline () throws Exception {
    MockHttpServletResponse response;

    int sectionNo = 2;

    response = mvc.perform(
            MockMvcRequestBuilders
                .post("/enrollments/sections/"+sectionNo)
                .param("studentId", "5"))
        .andReturn()
        .getResponse();

    assertEquals(400, response.getStatus());
    assertEquals("Not within the add period for this section.", response.getErrorMessage());
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
