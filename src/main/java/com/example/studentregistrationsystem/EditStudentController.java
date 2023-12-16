package com.example.studentregistrationsystem;

import datamodel.Student;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class EditStudentController {

    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField studentIDField;

    public void setStudent(Student selectedStudent) {
        if (selectedStudent == null) {
            return;
        }

        firstNameField.setText(selectedStudent.getFirstName());
        lastNameField.setText(selectedStudent.getLastName());
        studentIDField.setText(selectedStudent.getStudentID());
    }

    public void updateStudent(Student selectedStudent) {
        if (selectedStudent == null) {
            return;
        }

        selectedStudent.setFirstName(firstNameField.getText());
        selectedStudent.setLastName(lastNameField.getText());
        selectedStudent.setStudentID(studentIDField.getText());
    }
}