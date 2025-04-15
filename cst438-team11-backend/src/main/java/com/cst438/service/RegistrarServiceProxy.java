package com.cst438.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import com.cst438.dto.CourseDTO;
import com.cst438.dto.EnrollmentDTO;
import com.cst438.dto.SectionDTO;
import com.cst438.dto.UserDTO;

@Service
public class RegistrarServiceProxy {

    Queue registrarServiceQueue = new Queue("registrar_service", true);

    @Bean
    public Queue createQueue() {
        return new Queue("gradebook_service", true);
    }

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    SectionRepository sectionRepository;

    @Autowired
    EnrollmentRepository enrollmentRepository;

    @RabbitListener(queues = "gradebook_service")
    public void receiveFromRegistrar(String message) {
        try {
            String[] parts = message.split(" ", 2);
            String action = parts[0];

            switch (action) {
                case "addCourse":
                case "updateCourse": {
                    CourseDTO dto = fromJsonString(parts[1], CourseDTO.class);
                    Course c = new Course();
                    c.setCourseId(dto.courseId());
                    c.setTitle(dto.title());
                    c.setCredits(dto.credits());
                    courseRepository.save(c);
                    break;
                }
                case "deleteCourse": {
                    courseRepository.deleteById(parts[1]);
                    break;
                }

                case "addUser":
                case "updateUser": {
                    UserDTO dto = fromJsonString(parts[1], UserDTO.class);
                    User u = new User();
                    u.setId(dto.id());
                    u.setEmail(dto.email());
                    u.setName(dto.name());
                    u.setPassword("not_shared");
                    u.setType(dto.type());
                    userRepository.save(u);
                    break;
                }
                case "deleteUser": {
                    userRepository.deleteById(Integer.parseInt(parts[1]));
                    break;
                }

                case "addSection":
                case "updateSection": {
                    SectionDTO dto = fromJsonString(parts[1], SectionDTO.class);
                    Section s = new Section();
                    s.setSectionNo(dto.secNo());
                    s.setSecId(dto.secId());
                    s.setBuilding(dto.building());
                    s.setRoom(dto.room());
                    s.setTimes(dto.times());
                    s.setInstructor_email(dto.instructorEmail());
                    s.setCourse(courseRepository.findById(dto.courseId()).orElse(null));
                    s.setTerm(new Term(dto.year(), dto.semester()));
                    sectionRepository.save(s);
                    break;
                }
                case "deleteSection": {
                    sectionRepository.deleteById(Integer.parseInt(parts[1]));
                    break;
                }

                case "addEnrollment": {
                    EnrollmentDTO dto = fromJsonString(parts[1], EnrollmentDTO.class);
                    Enrollment e = new Enrollment();
                    e.setEnrollmentId(dto.enrollmentId());
                    e.setGrade(dto.grade());
                    e.setUser(userRepository.findById(dto.studentId()).orElse(null));
                    e.setSection(sectionRepository.findById(dto.sectionNo()).orElse(null));
                    enrollmentRepository.save(e);
                    break;
                }

                case "deleteEnrollment": {
                    enrollmentRepository.deleteById(Integer.parseInt(parts[1]));
                    break;
                }

                default:
                    System.out.println(" Unknown action: " + action);
            }

        } catch (Exception e) {
            System.out.println(" Error from Registrar: " + e.getMessage());
        }
    }


    private void sendMessage(String s) {
        rabbitTemplate.convertAndSend(registrarServiceQueue.getName(), s);
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
