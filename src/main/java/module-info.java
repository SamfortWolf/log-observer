module ru.samfort.logobserver {
    requires javafx.controls;
    requires javafx.fxml;
    requires richtextfx;
    requires flowless;

    opens ru.samfort.logobserver to javafx.fxml;
    exports ru.samfort.logobserver;

}