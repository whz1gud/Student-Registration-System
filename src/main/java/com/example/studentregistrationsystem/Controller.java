package com.example.studentregistrationsystem;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import datamodel.Student;
import datamodel.StudentGroup;
import datamodel.StudentGroupData;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javafx.util.converter.DefaultStringConverter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class Controller {
    @FXML
    private BorderPane mainBorderPane;
    @FXML
    private ContextMenu listContextMenu;
    @FXML
    private ContextMenu studentTableContextMenu;
    @FXML
    private ListView<StudentGroup> groupListView;
    @FXML
    private TableView<Student> studentTable;
    @FXML
    private DatePicker datePicker;
    @FXML
    private TableColumn<Student, String> firstNameColumn;
    @FXML
    private TableColumn<Student, String> lastNameColumn;
    @FXML
    private TableColumn<Student, String> studentIdColumn;

    public void initialize() {

        listContextMenu = new ContextMenu();
        MenuItem deleteMenuItem = new MenuItem("Delete");
        listContextMenu.getItems().addAll(deleteMenuItem);
        deleteMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                StudentGroup item = groupListView.getSelectionModel().getSelectedItem();
                deleteGroup(item);
            }
        });

        studentTableContextMenu = new ContextMenu();
        MenuItem deleteStudentMenuItem = new MenuItem("Delete");
        MenuItem editStudentMenuItem = new MenuItem("Edit");
        studentTableContextMenu.getItems().addAll(deleteStudentMenuItem);
        studentTableContextMenu.getItems().addAll(editStudentMenuItem);
        deleteStudentMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                Student selectedStudent = studentTable.getSelectionModel().getSelectedItem();
                deleteStudent(selectedStudent);
            }
        });
        editStudentMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                Student selectedStudent = studentTable.getSelectionModel().getSelectedItem();
                showEditStudentDialog(selectedStudent);
            }
        });

        groupListView.setItems(StudentGroupData.getInstance().getStudentGroups());
        groupListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        groupListView.getSelectionModel().selectFirst(); // Temporary code for populating hard coded items for UI tests and creating a file

        firstNameColumn.setCellValueFactory(new PropertyValueFactory<Student, String>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<Student, String>("lastName"));
        studentIdColumn.setCellValueFactory(new PropertyValueFactory<Student, String>("studentID"));

        ChangeListener<StudentGroup> groupChangeListener = new ChangeListener<StudentGroup>() {
            @Override
            public void changed(ObservableValue<? extends StudentGroup> observableValue, StudentGroup oldValue, StudentGroup newValue) {
                if (newValue != null) {
                    StudentGroup group = groupListView.getSelectionModel().getSelectedItem();
                    studentTable.setItems(group.getStudents());
                }
            }
        };

        groupListView.getSelectionModel().selectedItemProperty().addListener(groupChangeListener);
        groupChangeListener.changed(null, null, groupListView.getSelectionModel().getSelectedItem());

        groupListView.setCellFactory(new Callback<ListView<StudentGroup>, ListCell<StudentGroup>>() {
            @Override
            public ListCell<StudentGroup> call(ListView<StudentGroup> todoItemListView) {
                ListCell<StudentGroup> cell = new ListCell<StudentGroup>() {
                    @Override
                    protected void updateItem(StudentGroup studentGroup, boolean empty) {
                        super.updateItem(studentGroup, empty);
                        if (empty) {
                            setText(null);
                        } else {
                            setText(studentGroup.getGroupName());
                        }
                    }
                };

                cell.emptyProperty().addListener(
                        (obs, wasEmpty, isNowEmpty) -> {
                            if (isNowEmpty) {
                                cell.setContextMenu(null);
                            } else {
                                cell.setContextMenu(listContextMenu);
                            }
                        }
                );
                return cell;
            }
        });

        studentTable.setRowFactory(tableView ->
        {
            TableRow<Student> row = new TableRow<>();
            row.contextMenuProperty().bind(
                    javafx.beans.binding.Bindings.when(row.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(studentTableContextMenu)
            );
            return row;
        });

        studentTable.setEditable(true);

        firstNameColumn.setCellValueFactory(new PropertyValueFactory<Student, String>("firstName"));
        firstNameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        firstNameColumn.setOnEditCommit(event -> {
            Student student = event.getTableView().getItems().get(event.getTablePosition().getRow());
            student.setFirstName(event.getNewValue());
        });

        lastNameColumn.setCellValueFactory(new PropertyValueFactory<Student, String>("lastName"));
        lastNameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        lastNameColumn.setOnEditCommit(event -> {
            Student student = event.getTableView().getItems().get(event.getTablePosition().getRow());
            student.setLastName(event.getNewValue());
        });

        studentIdColumn.setCellValueFactory(new PropertyValueFactory<Student, String>("studentID"));
        studentIdColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        studentIdColumn.setOnEditCommit(event -> {
            Student student = event.getTableView().getItems().get(event.getTablePosition().getRow());
            student.setStudentID(event.getNewValue());
        });

        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                addAttendanceColumn(newValue);
            }
        });

        setStudentGroupListener();

        if (!groupListView.getItems().isEmpty()) {
            groupListView.getSelectionModel().select(0);
            StudentGroup firstGroup = groupListView.getSelectionModel().getSelectedItem();
            showStudents(firstGroup);
        }
    }

    @FXML
    void showNewGroupDialog(ActionEvent event) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        dialog.setTitle("Add Group");
        dialog.setHeaderText("Create a new Group");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("groupDialog.fxml"));

        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());
        } catch (IOException e) {
            System.out.println("Couldn't load the dialog");
            e.printStackTrace();
            return;
        }

        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            GroupController controller = fxmlLoader.getController();
            StudentGroup newGroup = controller.processResults();
            groupListView.getSelectionModel().select(newGroup);
        }
    }

    @FXML
    public void showNewStudentDialog(ActionEvent actionEvent) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        dialog.setTitle("Add Student");
        dialog.setHeaderText("Add new student to this Group");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("studentDialog.fxml"));

        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());
        } catch (IOException e) {
            System.out.println("Couldn't load the dialog");
            e.printStackTrace();
            return;
        }

        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            StudentController controller = fxmlLoader.getController();
            StudentGroup selectedGroup = groupListView.getSelectionModel().getSelectedItem();
            controller.processResults(selectedGroup);
        }
    }

    public void showEditStudentDialog(Student selectedStudent) {
        if (selectedStudent == null) {
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        dialog.setTitle("Edit Student");
        dialog.setHeaderText("Edit student details");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("editStudentDialog.fxml"));

        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());
        } catch (IOException e) {
            System.out.println("Couldn't load the dialog");
            e.printStackTrace();
            return;
        }

        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        // Populate the fields with the selected student's data
        EditStudentController controller = fxmlLoader.getController();
        controller.setStudent(selectedStudent);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            controller.updateStudent(selectedStudent);
        }
    }

    @FXML
    public void handleKeyPressed(KeyEvent keyEvent) {
        StudentGroup selectedGroup = groupListView.getSelectionModel().getSelectedItem();
        if (selectedGroup != null) {
            if (keyEvent.getCode().equals(KeyCode.DELETE)) {
                deleteGroup(selectedGroup);
            }
        }
    }

    @FXML
    public void handleKeyPressedTable(KeyEvent keyEvent) {
        Student selectedStudent = studentTable.getSelectionModel().getSelectedItem();
        if (selectedStudent != null) {
            if (keyEvent.getCode().equals(KeyCode.DELETE)) {
                deleteStudent(selectedStudent);
            }
        }
    }

    @FXML
    void exportPdf(ActionEvent event) {
        try {
            StudentGroup selectedGroup = groupListView.getSelectionModel().getSelectedItem();
            if (selectedGroup == null) {
                return;
            }

            String fileName = selectedGroup.getGroupName() + ".pdf";
            com.itextpdf.kernel.pdf.PdfWriter writer = new com.itextpdf.kernel.pdf.PdfWriter(fileName);
            com.itextpdf.kernel.pdf.PdfDocument pdfDocument = new com.itextpdf.kernel.pdf.PdfDocument(writer);
            com.itextpdf.layout.Document document = new com.itextpdf.layout.Document(pdfDocument, com.itextpdf.kernel.geom.PageSize.A4.rotate());

            // Create table with columns equal to the number of columns in the TableView
            Table table = new Table(studentTable.getColumns().size());

            // Set table headers
            PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            for (TableColumn<Student, ?> column : studentTable.getColumns()) {
                Cell cell = new Cell();
                cell.add(new Paragraph(column.getText()).setFont(boldFont));
                cell.setBackgroundColor(new DeviceRgb(211, 211, 211));
                table.addCell(cell);
            }

            // Add table rows
            for (Student student : selectedGroup.getStudents()) {
                table.addCell(student.getFirstName());
                table.addCell(student.getLastName());
                table.addCell(student.getStudentID());

                for (int i = 3; i < studentTable.getColumns().size(); i++) {
                    TableColumn<Student, ?> column = studentTable.getColumns().get(i);
                    String date = column.getText();
                    StringProperty attendanceProperty = student.getAttendance().get(date);
                    String attendance = attendanceProperty != null ? attendanceProperty.getValue() : "";
                    table.addCell(attendance);
                }
            }

            document.add(table);
            document.close();

            // Show confirmation alert
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("PDF Export");
            alert.setHeaderText(null);
            alert.setContentText("The students' data was exported successfully to " + fileName);
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            // Show error alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("PDF Export");
            alert.setHeaderText(null);
            alert.setContentText("An error occurred while exporting the students' data to PDF.");
            alert.showAndWait();
        }
    }

    @FXML
    void exportXlsx(ActionEvent event) {
        exportStudentDataToExcel(groupListView.getSelectionModel().getSelectedItem().getStudents(), "group.xlsx");
    }

    @FXML
    void importXlsx(ActionEvent event) {
        StudentGroup selectedGroup = groupListView.getSelectionModel().getSelectedItem();
        if (selectedGroup != null) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Import Student Data");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Excel Files", "*.xlsx")
            );
            File file = fileChooser.showOpenDialog(mainBorderPane.getScene().getWindow());

            if (file != null) {
                importStudentDataFromExcel(file.getAbsolutePath());
                showStudents(selectedGroup);
            }
        }
    }

    @FXML
    public void handleExit(ActionEvent actionEvent) {
        Platform.exit();
    }

    public void deleteGroup(StudentGroup group) {
        if (group == null) {
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete a Group");
        alert.setHeaderText("Delete group: " + group.getGroupName());
        alert.setContentText("Are you sure? Press OK to confirm");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && (result.get() == ButtonType.OK)) {
            StudentGroupData.getInstance().deleteStudentGroup(group);
        }
    }

    public void deleteStudent(Student student) {
        if (student == null) {
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete a Student");
        alert.setHeaderText("Delete student: " + student.getFirstName() + " " + student.getLastName());
        alert.setContentText("Are you sure? Press OK to confirm");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && (result.get() == ButtonType.OK)) {
            StudentGroup selectedGroup = groupListView.getSelectionModel().getSelectedItem();
            selectedGroup.getStudents().remove(student);
        }
    }

    private void addAttendanceColumn(LocalDate date) {
        String dateString = date.toString();

        // Create a new column for the selected date
        TableColumn<Student, String> attendanceColumn = new TableColumn<>(dateString);
        attendanceColumn.setCellValueFactory(cellData -> cellData.getValue().attendanceProperty(dateString));

        // Set cell factory to show "+" symbol for each student
        attendanceColumn.setCellFactory(tc -> {
            TableCell<Student, String> cell = new TableCell<>();
            cell.textProperty().bind(Bindings.when(cell.emptyProperty()).then("").otherwise("+"));
            return cell;
        });

        // Add the attendance for the selected date to each student in the group
        StudentGroup selectedGroup = groupListView.getSelectionModel().getSelectedItem();
        if (selectedGroup != null) {
            for (Student student : selectedGroup.getStudents()) {
                student.addAttendance(dateString, "+");
            }
        }

        // Add the new column to the TableView
        studentTable.getColumns().add(attendanceColumn);
    }

    private void showStudents(StudentGroup group) {
        studentTable.getColumns().clear();
        studentTable.setItems(group.getStudents());

        // Add the basic columns (Name, Last Name, Student ID)
        studentTable.getColumns().addAll(firstNameColumn, lastNameColumn, studentIdColumn);

        // Add the date columns based on the attendance data
        Set<String> dateColumns = new HashSet<>();
        for (Student student : group.getStudents()) {
            dateColumns.addAll(student.getAttendance().keySet());
        }

        for (String date : dateColumns) {
            TableColumn<Student, String> dateColumn = new TableColumn<>(date);
            dateColumn.setCellValueFactory(param -> param.getValue().attendanceProperty(date));

            // Set the cell factory for editing with a TextField
            dateColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DefaultStringConverter()));

            // Handle the edit commit event
            dateColumn.setOnEditCommit(event -> {
                Student student = event.getRowValue();
                String newValue = event.getNewValue();

                // Update the attendance value for the student on the given date
                student.addAttendance(date, newValue);
            });

            studentTable.getColumns().add(dateColumn);
        }

    }

    private void setStudentGroupListener() {
        groupListView.getSelectionModel().selectedItemProperty().addListener((observableValue, oldGroup, newGroup) -> {
            if (newGroup != null) {
                showStudents(newGroup);
            }
        });
    }

    public void exportStudentDataToExcel(ObservableList<Student> students, String fileName) {
        Workbook workbook = new XSSFWorkbook();

        Sheet sheet = workbook.createSheet("Student data");
        String[] headers = {"Name", "Surname", "Student ID"};
        org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);

        for (int i = 0; i < headers.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        for (int i = 0; i < students.size(); i++) {
            Student student = students.get(i);
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(student.getFirstName());
            row.createCell(1).setCellValue(student.getLastName());
            row.createCell(2).setCellValue(student.getStudentID());
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        try (FileOutputStream fileOut = new FileOutputStream(fileName)) {
            workbook.write(fileOut);
            System.out.println("Student data has been exported to " + fileName);
        } catch (IOException e) {
            System.out.println("Error occurred while exporting student data: " + e.getMessage());
        }
    }

    public void importStudentDataFromExcel(String fileName) {
        try (InputStream inputStream = new FileInputStream(fileName)) {
            Workbook workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(0);

            ObservableList<Student> students = FXCollections.observableArrayList();

            // Start from the second row to skip the column names
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    String firstName = row.getCell(0).getStringCellValue();
                    String lastName = row.getCell(1).getStringCellValue();
                    students.add(new Student(firstName, lastName));
                }
            }

            StudentGroup selectedGroup = groupListView.getSelectionModel().getSelectedItem();
            selectedGroup.setStudents(students);

            // Update the table view with the imported data
            showStudents(selectedGroup);

        } catch (IOException e) {
            System.out.println("Error occurred while importing student data: " + e.getMessage());
        }
    }
}