package com.cst438.controller;

import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.dto.EnrollmentDTO;
import com.cst438.dto.GradeDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@AutoConfigureMockMvc
@SpringBootTest
public class EnrollmentControllerUnitTest {
  @Autowired
  MockMvc mvc;

  @Autowired
  EnrollmentRepository enrollmentRepository;

  //TODO: COMPLETE: instructor enters final class grades for all enrolled students
  @Test
  public void enterEnrollmentGrades() throws Exception {
    MockHttpServletResponse response;
    int sectionNo = 10;

    response = mvc.perform(
            MockMvcRequestBuilders
                .get("/sections/" + sectionNo + "/enrollments"))
        .andReturn()
        .getResponse();

    assertEquals(200, response.getStatus());

    ObjectMapper mapper = new ObjectMapper();
    ArrayList<EnrollmentDTO> enrollmentDTOs = mapper.readValue(response.getContentAsString(),
        new TypeReference<ArrayList<EnrollmentDTO>>(){});

    assertEquals(1, enrollmentDTOs.size(), "More results were returned than expected for test");
    assertNull(enrollmentDTOs.get(0).grade());

    ArrayList<EnrollmentDTO> enrollmentDTONew = new ArrayList<>();

    for (EnrollmentDTO enrollmentDTO : enrollmentDTOs) {
      enrollmentDTONew.add(new EnrollmentDTO(
          enrollmentDTO.enrollmentId(),
          "A",
          enrollmentDTO.studentId(),
          enrollmentDTO.name(),
          enrollmentDTO.email(),
          enrollmentDTO.courseId(),
          enrollmentDTO.title(),
          enrollmentDTO.sectionId(),
          enrollmentDTO.sectionNo(),
          enrollmentDTO.building(),
          enrollmentDTO.room(),
          enrollmentDTO.times(),
          enrollmentDTO.credits(),
          enrollmentDTO.year(),
          enrollmentDTO.semester()
      ));
    }

    response = mvc.perform(
            MockMvcRequestBuilders
                .put("/enrollments")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(enrollmentDTONew)))
        .andReturn()
        .getResponse();

    assertEquals(200, response.getStatus());
    assertEquals("A", enrollmentDTONew.get(0).grade());

    //check database

    Enrollment e = enrollmentRepository.findEnrollmentBySectionNoAndStudentId(
        enrollmentDTONew.get(0).sectionNo(),
        enrollmentDTONew.get(0).studentId());

    assertNotNull(e);
    assertEquals("A", e.getGrade());
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
