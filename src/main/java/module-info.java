module ru.samfort.logobserver {
    requires javafx.controls;
    requires javafx.fxml;

    opens ru.samfort.logobserver to javafx.fxml;
    exports ru.samfort.logobserver;
}