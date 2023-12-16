package datamodel;

import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;

public class StudentGroupData {
    private static StudentGroupData instance = new StudentGroupData();
    private static String filename = "StudentGroups.txt";
    private ObservableList<StudentGroup> studentGroups;

    private StudentGroupData() {
    }

    public ObservableList<StudentGroup> getStudentGroups() {
        return studentGroups;
    }

    public static StudentGroupData getInstance() {
        return instance;
    }

    public void addStudentGroup(StudentGroup group) {
        studentGroups.add(group);
    }

    public void loadStudentGroups() throws IOException {
        studentGroups = FXCollections.observableArrayList();

        Path path = Paths.get(filename);
        BufferedReader br = Files.newBufferedReader(path);

        String input;

        try {
            while ((input = br.readLine()) != null) {
                String[] groupPieces = input.split("\t");

                String groupName = groupPieces[0];
                ObservableList<Student> studentList = FXCollections.observableArrayList();

                if (groupPieces.length > 1) {
                    String studentsData = groupPieces[1];
                    String[] students = studentsData.split(";");

                    for (String student : students) {
                        String[] studentInfo = student.split(",");
                        String firstName = studentInfo[0];
                        String lastName = studentInfo[1];
                        String studentID = studentInfo[2];
                        Student newStudent = new Student(firstName, lastName);
                        newStudent.setStudentID(studentID);

                        if (studentInfo.length > 3) {
                            String attendanceData = studentInfo[3];
                            String[] attendanceEntries = attendanceData.split("\\|");
                            for (String entry : attendanceEntries) {
                                String[] keyValue = entry.split("=");
                                newStudent.addAttendance(keyValue[0], keyValue[1]);
                            }
                        }

                        studentList.add(newStudent);
                    }
                }

                StudentGroup group = new StudentGroup(groupName, studentList);
                studentGroups.add(group);
            }
        } finally {
            if (br != null) {
                br.close();
            }
        }
    }

    public void storeStudentGroups() throws IOException {
        Path path = Paths.get(filename);
        BufferedWriter bw = Files.newBufferedWriter(path);

        try {
            Iterator<StudentGroup> iter = studentGroups.iterator();
            while (iter.hasNext()) {
                StudentGroup group = iter.next();
                StringBuilder studentsData = new StringBuilder();
                for (Student student : group.getStudents()) {
                    StringBuilder attendanceData = new StringBuilder();
                    for (Map.Entry<String, StringProperty> entry : student.getAttendance().entrySet()) {
                        attendanceData.append(String.format("%s=%s|", entry.getKey(), entry.getValue().get()));
                    }
                    // Remove the last | symbol
                    if (attendanceData.length() > 0) {
                        attendanceData.setLength(attendanceData.length() - 1);
                    }
                    studentsData.append(String.format("%s,%s,%s,%s;", student.getFirstName(), student.getLastName(), student.getStudentID(), attendanceData.toString()));
                }
                // Remove the last semicolon
                if (studentsData.length() > 0) {
                    studentsData.setLength(studentsData.length() - 1);
                }
                bw.write(String.format("%s\t%s", group.getGroupName(), studentsData.toString()));
                bw.newLine(); // adds newline to text file
            }
        } finally {
            if (bw != null) {
                bw.close();
            }
        }
    }

    public void deleteStudentGroup(StudentGroup group) {
        studentGroups.remove(group);
    }
}