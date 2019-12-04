package ru.samfort.logobserver;

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
import ru.samfort.logobserver.utils.SimpleFileTreeItem;
import ru.samfort.logobserver.utils.TextFileManager;

import java.io.File;
import java.io.IOException;

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
    private void directoryChooser (ActionEvent event) {
        final DirectoryChooser directoryChooser = new DirectoryChooser();
        Window dcWindow = directoryChooserButton.getScene().getWindow();
        File directory = directoryChooser.showDialog(dcWindow);
        treeItem = new SimpleFileTreeItem(directory, ext.getText(), textToSearch.getText());
        new Thread(()->treeItemClean(treeItem)).start();
        expandTreeView(treeItem);
        treeView.setRoot(treeItem);
        treeView.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEventHandle);
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

    private void treeItemClean (TreeItem <File> root){
        ObservableList <TreeItem<File>> list = root.getChildren();
        //very-very bad...
        for (int j=0;j<5;j++) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).isLeaf() && !list.get(i).getValue().isFile()) {
                    list.get(i).getParent().getChildren().remove(list.get(i));
                    i--;
                } else if (list.get(i).getChildren() != null && list.get(i).getChildren().size() > 0) {
                    treeItemClean(list.get(i));
                } else if (list.get(i).getChildren().size() == 0 && !list.get(i).getValue().isFile()) {
                    list.get(i).getParent().getChildren().remove(list.get(i));
                    i--;
                }
            }
        }
    }
    private void expandTreeView(TreeItem<File> item){
        if(item != null && !item.isLeaf() && item.getValue().isFile()){
            item.setExpanded(true);
            for(TreeItem<File> child:item.getChildren()){
                expandTreeView(child);
            }
        }
    }
}
