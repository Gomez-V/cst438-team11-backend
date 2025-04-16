package com.cst438.service;

import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.dto.EnrollmentDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
public class GradebookServiceProxy {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    EnrollmentRepository enrollmentRepository;

    Queue gradebookServiceQueue = new Queue("gradebook_service", true);

    @Bean
    public Queue createQueue() {
        return new Queue("registrar_service", true);
    }

    @RabbitListener(queues = "registrar_service")
    public void receiveFromGradebook(String message) {
        try {
            String[] parts = message.split(" ", 2);
            String action = parts[0];

            if (action.equals("updateFinalGrade")) {
                // parse JSON into EnrollmentDTO
                EnrollmentDTO dto = fromJsonString(parts[1], EnrollmentDTO.class);
                // find and update enrollment’s grade
                Enrollment enrollment = enrollmentRepository.findById(dto.enrollmentId()).orElse(null);
                if (enrollment != null) {
                    enrollment.setGrade(dto.grade());
                    enrollmentRepository.save(enrollment);
                }
            }
        } catch (Exception e) {
            System.out.println("Error processing message from Gradebook: " + e.getMessage());
            // do NOT rethrow, or message will loop infinitely
        }
    }

   
    public void sendMessage(String s) {
        rabbitTemplate.convertAndSend(gradebookServiceQueue.getName(), s);
    }

    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T fromJsonString(String str, Class<T> valueType) {
        try {
            return new ObjectMapper().readValue(str, valueType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
