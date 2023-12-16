package datamodel;

import javafx.collections.ObservableList;

import java.util.function.Predicate;

public class StudentGroup {
    private String groupName;
    private ObservableList<Student> students;

    public StudentGroup(String groupName, ObservableList<Student> students) {
        this.groupName = groupName;
        this.students = students;
    }

    public ObservableList<Student> filterStudents(Predicate<Student> predicate) {
        return students.filtered(predicate);
    }

    public void addStudent(Student student) {
        this.students.add(student);
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public ObservableList<Student> getStudents() {
        return students;
    }

    public void setStudents(ObservableList<Student> students) {
        this.students = students;
    }

    @Override
    public String toString() {
        return groupName;
    }
}