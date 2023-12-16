package datamodel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

public class Student extends Person {
    private String studentID;
    private ObservableMap<String, StringProperty> attendance;
    private static int lastUsedID = 1000;

    public Student(String firstName, String lastName) {
        super(firstName, lastName);
        this.studentID = generateStudentID();
        this.attendance = FXCollections.observableHashMap();
    }

    public void addAttendance(String date, String value) {
        attendance.put(date, new SimpleStringProperty(value));
    }

    public StringProperty attendanceProperty(String date) {
        return attendance.get(date);
    }

    public ObservableMap<String, StringProperty> getAttendance() {
        return attendance;
    }

    private String generateStudentID() {
        lastUsedID++;
        return String.valueOf(lastUsedID);
    }

    public String getStudentID() {
        return studentID;
    }

    public void setStudentID(String studentID) {
        this.studentID = studentID;
    }

    @Override
    public String getRole() {
        return "Student";
    }
}