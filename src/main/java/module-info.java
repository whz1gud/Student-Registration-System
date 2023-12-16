module com.example.studentregistrationsystem {
    requires javafx.controls;
    requires javafx.fxml;
    requires poi;
    requires poi.ooxml;
    requires layout;
    requires io;
    requires kernel;

    opens com.example.studentregistrationsystem to javafx.fxml, javafx.base;
    opens datamodel to javafx.base;
    exports com.example.studentregistrationsystem;
}