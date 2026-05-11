module com.example.capstone {

    requires javafx.controls;
    requires javafx.fxml;

    requires java.sql;

    opens com.example.capstone to javafx.fxml;

    opens com.example.capstone.controller to javafx.fxml;

    opens com.example.capstone.model to javafx.base;


    exports com.example.capstone;

    exports com.example.capstone.controller;
}