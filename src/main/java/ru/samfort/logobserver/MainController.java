package ru.samfort.logobserver;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.StyleClassedTextArea;
import ru.samfort.logobserver.utils.MatchWord;
import ru.samfort.logobserver.utils.ObservableSetFiller;
import ru.samfort.logobserver.utils.TextFileManager;

import java.io.File;
import java.nio.file.Paths;
import java.util.Map;

import static ru.samfort.logobserver.utils.TextFileManager.read;
import static ru.samfort.logobserver.utils.TextFileManager.styleTextArea;
import static ru.samfort.logobserver.utils.TreeViewHelper.*;

public class MainController {

    private static Thread searchThread;

    private static File lastSearchDirectory =null;

    private int tabCounter = 0;
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

    private StyleClassedTextArea textArea;

    private VirtualizedScrollPane<StyleClassedTextArea> scrollPane;
    @FXML
    private TreeView<String> treeView;
    @FXML
    private Label matchesLabel;
    @FXML
    private Label status;

    private EventHandler<MouseEvent> mouseEventHandle = this::handleMouseClicked;

    private EventHandler tabSelectionHandler = (Event event) ->
            matchesLabel.setText(((MyTab) tabPane.getSelectionModel().getSelectedItem()).getMatchCounter() + "/" +
                    ((MyTab) tabPane.getSelectionModel().getSelectedItem()).getWordsPositions().size());

    private void handleMouseClicked(MouseEvent event) {
        // Accept double clicks
        if (event.getClickCount() == 2) {
            matchesLabel.setText("");
            String clickCheck = treeView.getSelectionModel().getSelectedItem().getValue();
            if (clickCheck != null && clickCheck.endsWith(ext.getText())) {
                String str = pathBuilder(treeView.getSelectionModel().getSelectedItem());
                System.out.println("Open: " + str);
                TextFileManager textFileManager = new TextFileManager();
                textFileManager.isFileContainText(textToSearch.getText(), Paths.get(str), false);
                System.out.println("Found " + textFileManager.getWordsPositions().size() + " matches");
                //prepare new thread
                //get new styled text area from file
                Thread textAreaThread = new Thread(() -> {
                    long startTime = System.currentTimeMillis();
                    //get new styled text area from file
                    textArea = styleTextArea(read(new File(str)), textFileManager.getWordsPositions().entrySet());
                    System.out.println("Search time is: " + (System.currentTimeMillis() - startTime) + "ms\n----------------------------");
                    Platform.runLater(() -> {
                        scrollPane = new VirtualizedScrollPane<>(textArea);
                        addNewTab(textArea, textFileManager.getWordsPositions(), textFileManager.getLinesCount());
                        tabPane.getTabs().get(tabCounter - 1).setContent(scrollPane);
                        textArea.setEditable(false);
                        textArea.setDisable(true);
                        tabPane.getTabs().get(tabCounter - 1).setText(Paths.get(str).getFileName().toString());
                        tabPane.getSelectionModel().select(tabPane.getTabs().get(tabCounter - 1));

                    });
                });
                textAreaThread.start();
            }
        }
    }

    @FXML
    private void directoryChooser() {
        final DirectoryChooser directoryChooser = new DirectoryChooser();
        if (lastSearchDirectory !=null){
            directoryChooser.setInitialDirectory(lastSearchDirectory);//set last search directory if it exists
        }
        Window dcWindow = directoryChooserButton.getScene().getWindow();
        //show directory chooser window
        File directory = directoryChooser.showDialog(dcWindow);
        if (treeView.getRoot() != null) {
            treeView.setRoot(null);
        }
        if (directory != null) {
            lastSearchDirectory = directory;//save last search directory
            System.out.println("Root directory is: " + directory.toString());
            status.setText("Processing...");
            status.setStyle("-fx-background-color: rgb(255,176,50)");
            long startTime = System.currentTimeMillis();
            ObservableSetFiller filler = new ObservableSetFiller();
            //prepare thread to search files
            searchThread = new Thread(() -> {
                filler.fillObservableSet(directory, ext.getText(), textToSearch.getText(), checkBoxSubs.isSelected());
                setRoot(Paths.get(directory.getAbsolutePath()));
                setObservableList(filler.getObservableSet());
                fillTreeView();
                Platform.runLater(() -> {
                    if (filler.getObservableSet().size() > 0) {
                        status.setText("Complete!");
                        status.setStyle("-fx-background-color: rgb(81,255,98)");
                        treeView.setRoot(getRoot());
                        treeView.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEventHandle);
                        System.out.println("Search time is: " + (System.currentTimeMillis() - startTime) + "ms\n----------------------------");
                    } else {
                        status.setText("No matches!");
                        status.setStyle("-fx-background-color: rgb(255,67,75)");
                        directoryChooserButton.setDisable(false);
                        System.out.println("Time: " + (System.currentTimeMillis() - startTime) + "ms\n----------------------------");
                    }
                });
            });
            searchThread.start();
            directoryChooserButton.setDisable(true);
        }
    }

    @FXML
    private void addNewTab(StyleClassedTextArea textArea, Map<Integer, MatchWord> wordPositions, int linesCount) {
        MyTab newTab = new MyTab(textArea, wordPositions, linesCount);
        newTab.setOnClosed((Event event) -> tabCounter--);
        newTab.setOnSelectionChanged(tabSelectionHandler);
        tabCounter++;
        // add newtab
        tabPane.getTabs().add(tabPane.getTabs().size(), newTab);
        //set selection
        tabPane.getSelectionModel().select(tabPane.getTabs().size() - 1);
    }

    @FXML
    private void nextButtonOnClick() {
        if (tabPane.getTabs().size() > 0) {
            MyTab currentTab = (MyTab) tabPane.getSelectionModel().getSelectedItem();
            Map<Integer, MatchWord> matchWords = currentTab.getWordsPositions();
            int allMatches = currentTab.getWordsPositions().size();
            Integer currentMatch = currentTab.getMatchCounter() + 1;
            if (currentMatch > matchWords.size()) {
                currentMatch = 1;
            }
            //change label text to show chosen match
            matchesLabel.setText(currentMatch + "/" + allMatches);
            currentTab.getTextArea().displaceCaret(matchWords.get(currentMatch).getFrom());//set a caret to match word pos
            int lineNumber = currentTab.getTextArea().getCurrentParagraph();//get number of line with match word
            currentTab.getTextArea().selectRange(matchWords.get(currentMatch).getFrom(), matchWords.get(currentMatch).getTo());//set a selection
            int linesCount = currentTab.getLinesCounter();
            double scrollPaneHeight = scrollPane.getTotalHeightEstimate();//full height of scrollPane (pixels)
            double oneLineHeight = scrollPaneHeight / linesCount;//height of one line
            if (oneLineHeight * linesCount > 150) {
                scrollPane.scrollYToPixel(oneLineHeight * lineNumber - 100);//scroll to line with match word
            } else {
                scrollPane.scrollYToPixel(oneLineHeight * lineNumber);//scroll to line with match word
            }
            currentTab.setMatchCounter(currentMatch);
        }
    }

    @FXML
    private void previousButtonOnClick() {
        if (tabPane.getTabs().size() > 0) {
            MyTab currentTab = (MyTab) tabPane.getSelectionModel().getSelectedItem();
            Map<Integer, MatchWord> matchWords = currentTab.getWordsPositions();
            int allMatches = currentTab.getWordsPositions().size();
            int currentMatch = currentTab.getMatchCounter() - 1;
            if (currentMatch < 1) {
                currentMatch = matchWords.size();
            }
            matchesLabel.setText(currentMatch + "/" + allMatches);

            currentTab.getTextArea().displaceCaret(matchWords.get(currentMatch).getFrom());//set a caret to match word pos
            int lineNumber = currentTab.getTextArea().getCurrentParagraph();//get number of line with match word
            currentTab.getTextArea().selectRange(matchWords.get(currentMatch).getFrom(), matchWords.get(currentMatch).getTo());//set a selection
            int linesCount = currentTab.getLinesCounter();
            double scrollPaneHeight = scrollPane.getTotalHeightEstimate();//full height of scrollPane (pixels)
            double oneLineHeight = scrollPaneHeight / linesCount;//height of one line
            if (oneLineHeight * linesCount > 150) {
                scrollPane.scrollYToPixel(oneLineHeight * lineNumber - 100);//scroll to line with match word
            } else {
                scrollPane.scrollYToPixel(oneLineHeight * lineNumber);//scroll to line with match word
            }
            currentTab.setMatchCounter(currentMatch);
        }
    }

    @FXML
    private void stopButtonClick() {
        if (searchThread != null && searchThread.isAlive()) {
            //stop thread using deprecating method((
            searchThread.stop();
        }
        //clear all forms and labels
        status.setText("--");
        status.setStyle(null);
        if (tabPane.getTabs().size() > 0) {
            tabPane.getTabs().clear();
        }
        if (treeView.getRoot() != null) {
            treeView.setRoot(null);
        }
        matchesLabel.setText("");
        directoryChooserButton.setDisable(false);
    }

    @FXML
    private void expandAllClick() {
        if (treeView.getRoot() != null) {
            expandAll(treeView.getRoot());
        }
    }

    @FXML
    private void collapseAllClick() {
        if (treeView.getRoot() != null) {
            collapseAll(treeView.getRoot());
        }
    }
}
