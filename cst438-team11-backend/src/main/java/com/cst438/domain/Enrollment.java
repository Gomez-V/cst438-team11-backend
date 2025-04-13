package com.cst438.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Enrollment {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="enrollment_id")
    int enrollmentId;
    private String grade;

    //RELATIONSHIPS
    @ManyToOne
    @JoinColumn(name="user_id", nullable=false)
    private User student;
    @ManyToOne
    @JoinColumn(name="section_no", nullable=false)
    private Section section;

    public void setEnrollmentId(int enrollmentId) {this.enrollmentId = enrollmentId;}
    public int getEnrollmentId() {return enrollmentId;}
    public void setGrade(String grade) {this.grade = grade;}
    public void setGrade() {this.grade = null;}
    public String getGrade() {return grade;}
    public void setUser(User student) {this.student = student;}
    public User getUser() {return student;}
    public void setSection(Section section) {this.section = section;}
    public Section getSection() {return section;}
}
