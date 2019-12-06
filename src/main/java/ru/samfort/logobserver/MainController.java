package ru.samfort.logobserver;

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
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.StyleClassedTextArea;
import ru.samfort.logobserver.utils.ObservableSetFiller;
import ru.samfort.logobserver.utils.TextFileManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
    private VirtualizedScrollPane<StyleClassedTextArea> scrollPane;
    @FXML
    private ListView<String> listView;
    @FXML
    private Button previousButton;
    @FXML
    private Button nextButton;

    EventHandler<MouseEvent> mouseEventHandle = (MouseEvent event) -> handleMouseClicked(event);

    private void handleMouseClicked(MouseEvent event) {
        Node node = event.getPickResult().getIntersectedNode();
        // Accept double clicks only on node cells, and not on empty spaces of the TreeView
        if (event.getClickCount() == 2 && (node instanceof Text || (node instanceof TreeCell && ((TreeCell) node).getText() != null))) {
            String str = listView.getSelectionModel().getSelectedItem();
            System.out.println("Node double click at: " + str);
            TextFileManager textFileManager = new TextFileManager();
            textFileManager.isFileContainText(textToSearch.getText(), Paths.get(str), false);
            System.out.println("Found " + textFileManager.getWordsPositions().size() + " matches");
            textArea = new StyleClassedTextArea();
            //read text from file to textArea
            TextFileManager.read(new File(str), textArea);
            List<String> styleClasses = Arrays.asList("yellow");
            for (Map.Entry<Integer, Integer> pair : textFileManager.getWordsPositions().entrySet()) {
                //add yellow background to words
                textArea.setStyle(pair.getKey(), pair.getValue(), styleClasses);
            }
            scrollPane = new VirtualizedScrollPane(textArea);
            addNewTab();
            tabPane.getTabs().get(tabCounter - 1).setContent(scrollPane);
            textArea.setEditable(false);
            tabPane.getTabs().get(tabCounter - 1).setText(Paths.get(str).getFileName().toString());
        }
    }

    @FXML
    private void directoryChooser(ActionEvent event) {
        final DirectoryChooser directoryChooser = new DirectoryChooser();
        Window dcWindow = directoryChooserButton.getScene().getWindow();
        File directory = directoryChooser.showDialog(dcWindow);
        ObservableSetFiller filler = new ObservableSetFiller();
        new Thread(() -> {
            filler.fillObservableSet(directory, ext.getText(), textToSearch.getText());
            listView.getItems().addAll(filler.getObservableSet());
            listView.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEventHandle);
            listView.getFocusModel().focus(1);
        }).start();
    }

    @FXML
    private void addNewTab() {
        Tab newTab = new Tab("Tab #" + (tabCounter));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("tabV3.fxml"));
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
