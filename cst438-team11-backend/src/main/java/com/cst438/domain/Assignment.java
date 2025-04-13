package com.cst438.domain;

import java.sql.Date;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

@Entity
public class Assignment {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="assignment_id")
    private int assignmentId;
    private String title;
    private Date dueDate;

    //RELATIONSHIPS
    @ManyToOne
    @JoinColumn(name = "section_no", nullable = false)
    private Section section;
    // 
    @OneToMany(mappedBy = "assignment")
    private List<Grade> grades;

    public void setAssignmentId(int assignmentId) {this.assignmentId = assignmentId;}
    public int getAssignmentId() {return assignmentId;}

    public void setTitle(String title) {this.title = title;}
    public String getTitle() {return title;}

    public void setDueDate(Date dueDate) {this.dueDate = dueDate;}
    public Date getDueDate() {return dueDate;}

    public void setSection(Section section) {this.section = section;}
    public Section getSection() {return section;}
    
    //
    public List<Grade> getGrades() { return grades; }
    public void setGrades(List<Grade> grades) { this.grades = grades; }

}