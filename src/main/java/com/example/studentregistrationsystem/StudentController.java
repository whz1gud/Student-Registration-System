package com.example.studentregistrationsystem;

import datamodel.Student;
import datamodel.StudentGroup;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class StudentController {
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;

    public void processResults(StudentGroup selectedGroup) {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();

        Student newStudent = new Student(firstName, lastName);
        selectedGroup.addStudent(newStudent);
    }
}
