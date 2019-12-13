package ru.samfort.logobserver;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.StyleClassedTextArea;
import ru.samfort.logobserver.utils.MatchWord;
import ru.samfort.logobserver.utils.ObservableSetFiller;
import ru.samfort.logobserver.utils.TextFileManager;
import ru.samfort.logobserver.utils.TreeViewHelper;

import java.io.File;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MainController {

    private static List<String> styleClasses = Collections.singletonList("yellow");

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

    private VirtualizedScrollPane scrollPane;
    @FXML
    private TreeView<String> treeView;
    @FXML
    private Button previousButton;
    @FXML
    private Button nextButton;
    @FXML
    private Label matchesLabel;

    private EventHandler<MouseEvent> mouseEventHandle = this::handleMouseClicked;

    private EventHandler tabSelectionHandler = (Event event) ->
            matchesLabel.setText(((MyTab) tabPane.getSelectionModel().getSelectedItem()).getMatchCounter() + "/" +
                    ((MyTab) tabPane.getSelectionModel().getSelectedItem()).getWordsPositions().size());


    private void handleMouseClicked(MouseEvent event) {
        Node node = event.getPickResult().getIntersectedNode();
        // Accept double clicks only on node cells, and not on empty spaces of the TreeView
        if (event.getClickCount() == 2 && (node instanceof Text || (node instanceof TreeCell && ((TreeCell) node).getText() != null))) {
            matchesLabel.setText("");
            String str = TreeViewHelper.pathBuilder(treeView.getSelectionModel().getSelectedItem());//listView.getSelectionModel().getSelectedItem();
            System.out.println("Node double click at: " + str);
            TextFileManager textFileManager = new TextFileManager();
            textFileManager.isFileContainText(textToSearch.getText(), Paths.get(str), false);//693ms
            System.out.println("Found " + textFileManager.getWordsPositions().size() + " matches");
            textArea = new StyleClassedTextArea();
            long time = System.currentTimeMillis();
            //read text from file to textArea
            TextFileManager.read(new File(str), textArea);
            for (Map.Entry<Integer, MatchWord> pair : textFileManager.getWordsPositions().entrySet()) {
                //add yellow background to words
                textArea.setStyle(pair.getValue().getFrom(), pair.getValue().getTo(), styleClasses);
            }
            System.out.println("time: " + (System.currentTimeMillis() - time) + "ms");
            scrollPane = new VirtualizedScrollPane(textArea);
            addNewTab(textArea, textFileManager.getWordsPositions());
            tabPane.getTabs().get(tabCounter - 1).setContent(scrollPane);
            textArea.setEditable(false);
            tabPane.getTabs().get(tabCounter - 1).setText(Paths.get(str).getFileName().toString());
        }
    }

    @FXML
    private void directoryChooser() {
        final DirectoryChooser directoryChooser = new DirectoryChooser();
        Window dcWindow = directoryChooserButton.getScene().getWindow();
        File directory = directoryChooser.showDialog(dcWindow);
        if (treeView.getRoot() != null) {
            treeView.setRoot(null);
        }
        if (directory != null) {
            System.out.println("Root directory is: " + directory.toString());
            ObservableSetFiller filler = new ObservableSetFiller();
            new Thread(() -> {
                filler.fillObservableSet(directory, ext.getText(), textToSearch.getText(), checkBoxSubs.isSelected());
                TreeViewHelper.setRoot(Paths.get(directory.getAbsolutePath()));
                TreeViewHelper.setObservableList(filler.getObservableSet());
                TreeViewHelper.fill();
                treeView.setRoot(TreeViewHelper.getRoot());
                treeView.getRoot().setExpanded(true);
            }).start();
            treeView.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEventHandle);
        }
    }

    @FXML
    private void addNewTab(StyleClassedTextArea textArea, Map<Integer, MatchWord> wordPositions) {
        MyTab newTab = new MyTab(textArea, wordPositions);
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
        int linesCount = currentTab.getTextArea().getParagraphs().size();
        double scrollPaneHeight = scrollPane.getTotalHeightEstimate();//full height of scrollPane (pixels)
        double oneLineHeight = scrollPaneHeight / linesCount;//height of one line
        if (oneLineHeight * linesCount > 150) {
            scrollPane.scrollYToPixel(oneLineHeight * lineNumber - 100);//scroll to line with match word
        } else {
            scrollPane.scrollYToPixel(oneLineHeight * lineNumber);//scroll to line with match word
        }
        currentTab.setMatchCounter(currentMatch);
    }

    @FXML
    private void previousButtonOnClick() {
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
        int linesCount = currentTab.getTextArea().getParagraphs().size();
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
