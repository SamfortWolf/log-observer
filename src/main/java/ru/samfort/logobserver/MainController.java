package ru.samfort.logobserver;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;
import org.fxmisc.richtext.StyleClassedTextArea;
import ru.samfort.logobserver.utils.ListViewFiller;
import ru.samfort.logobserver.utils.SimpleFileTreeItem;
import ru.samfort.logobserver.utils.TextFileManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class MainController {

    private volatile int tabCounter = 0;
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
    private StyleClassedTextArea textArea;
    @FXML
    private TreeView <File> treeView;
    @FXML
    private ListView <String> listView;
    private SimpleFileTreeItem treeItem;

    EventHandler<MouseEvent> mouseEventHandle = (MouseEvent event) -> handleMouseClicked(event);

    private void handleMouseClicked(MouseEvent event) {
        Node node = event.getPickResult().getIntersectedNode();
        // Accept double clicks only on node cells, and not on empty spaces of the TreeView
        if (event.getClickCount()==2 && (node instanceof Text || (node instanceof TreeCell && ((TreeCell) node).getText() != null))) {
            File file = (File) ((TreeItem)treeView.getSelectionModel().getSelectedItem()).getValue();
            String filePath = file.toString();
            if (filePath.endsWith(ext.getText())){
                textArea = new StyleClassedTextArea();
                //read text from file to textArea
                TextFileManager.read(file, textArea);
                addNewTab();
                tabPane.getTabs().get(tabCounter-1).setContent(textArea);
                tabPane.getTabs().get(tabCounter-1).setText(file.getName());
            }
            System.out.println("Node double click at: " + file.getAbsolutePath());
        }
    }


    @FXML
    private void directoryChooser (ActionEvent event) throws IOException {
        final DirectoryChooser directoryChooser = new DirectoryChooser();
        Window dcWindow = directoryChooserButton.getScene().getWindow();
        File directory = directoryChooser.showDialog(dcWindow);
        ListViewFiller filler = new ListViewFiller();
        listView = filler.getListView(directory, ext.getText(), textToSearch.getText());


    }

    @FXML
    private void addNewTab () {
            Tab newTab = new Tab("Tab #" + (tabCounter));
            FXMLLoader loader = new FXMLLoader(getClass().getResource("tabV2.fxml"));
        try {
            newTab.setContent(loader.load());
        } catch (IOException e) {
            e.printStackTrace();
        }
        tabCounter++;
            // add newtab
            tabPane.getTabs().add(tabPane.getTabs().size(), newTab);
            //set selection
            tabPane.getSelectionModel().select(tabPane.getTabs().size() - 1);
    }
}
