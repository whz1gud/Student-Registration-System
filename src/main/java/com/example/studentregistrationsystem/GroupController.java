package com.example.studentregistrationsystem;

import datamodel.Student;
import datamodel.StudentGroup;
import datamodel.StudentGroupData;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class GroupController {
    @FXML
    private TextField groupField;

    public StudentGroup processResults() {
        String groupName = groupField.getText().trim();
        ObservableList<Student> studentList = FXCollections.observableArrayList();

        StudentGroup newGroup = new StudentGroup(groupName, studentList);
        StudentGroupData.getInstance().addStudentGroup(newGroup);
        return newGroup;
    }
}
