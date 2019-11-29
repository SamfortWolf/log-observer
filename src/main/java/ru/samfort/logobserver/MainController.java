package ru.samfort.logobserver;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Border;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

public class MainController {

    private int tabCounter = 2;
    @FXML
    private TextField textToSearch;
    @FXML
    private TextField ext;
    @FXML
    private CheckBox checkBoxSubs;
    @FXML
    private Button directoryChooserButton;
    @FXML
    private TabPane tabPane;
    @FXML
    private Tab tabN1;
    @FXML
    private Tab addTab;

    @FXML
    private void switchToSecondary() throws IOException {
        App.setRoot("secondary");
    }

    @FXML
    private void directoryChooser (ActionEvent event){
        final DirectoryChooser directoryChooser = new DirectoryChooser();
        Window dcWindow = directoryChooserButton.getScene().getWindow();
        File directory = directoryChooser.showDialog(dcWindow);
        String directoryPath = directory.getAbsolutePath();
    }
    @FXML
    private void addNewTab () throws IOException {
        if (addTab.isSelected()){
            Tab newTab = new Tab("Tab #" + (tabCounter));
            Node previousTabContent = tabPane.getTabs().get(tabPane.getTabs().size()-2).getContent();
            /*newTab.setContent(previousTabContent);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ru/samfort/logobserver/observer.fxml"));
            newTab.setContent(loader.load());*/


            AnchorPane newAnchorPane = new AnchorPane();
                newAnchorPane.setPrefSize(900,560);
                newTab.setContent(newAnchorPane);
            SplitPane newSplitPane = new SplitPane();
                newSplitPane.setPrefSize(900,540);
                newSplitPane.setDividerPositions(0.3);
            AnchorPane innerAP = new AnchorPane();
                innerAP.setPrefSize(400,540);
            TreeView tw = new TreeView();
                tw.setPrefSize(400,540);
                tw.setMaxWidth(600);
                tw.setLayoutY(7);
            ScrollBar sb = new ScrollBar();
                sb.setOrientation(Orientation.VERTICAL);

            StackPane stackPane = new StackPane();
            stackPane.setPrefSize(900,560);
            stackPane.setAlignment(Pos.TOP_LEFT);
            stackPane.getChildren().addAll(newAnchorPane, newSplitPane, innerAP, tw, sb);
            newTab.setContent(stackPane);


            tabCounter++;
            // add newtab
            tabPane.getTabs().add(tabPane.getTabs().size() - 1, newTab);
            //set selection
            tabPane.getSelectionModel().select(tabPane.getTabs().size() - 2);
            // set event handler to the tab
            //newTab.setOnSelectionChanged(addTab.getOnSelectionChanged());
        }
    }
}
