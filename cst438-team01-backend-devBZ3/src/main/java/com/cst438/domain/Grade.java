package com.cst438.domain;

import jakarta.persistence.*;

@Entity
public class Grade {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="grade_id")
    private int gradeId;
    private Integer score;
    @ManyToOne
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;
    @ManyToOne
    @JoinColumn(name = "enrollment_id", nullable = false)
    private Enrollment enrollment;

    public void setGradeId(int gradeId) {this.gradeId = gradeId;}
    public int getGradeId() {return gradeId;}
    public void setScore(Integer score) {this.score = score;}
    public Integer getScore() {return score;}
    public void setAssignment(Assignment assignment) {this.assignment = assignment;}
    public Assignment getAssignment() {return assignment;}
    public void setEnrollment(Enrollment enrollment) {this.enrollment = enrollment;}
    public Enrollment getEnrollment() {return enrollment;}
 
    // TODO: is this complete?
}
