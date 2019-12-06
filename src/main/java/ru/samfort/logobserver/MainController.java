package ru.samfort.logobserver;

import javafx.event.ActionEvent;
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

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MainController {

    private static List<String> styleClasses = Arrays.asList("yellow");

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

    private StyleClassedTextArea textArea;

    private VirtualizedScrollPane<StyleClassedTextArea> scrollPane;
    @FXML
    private ListView<String> listView;
    @FXML
    private Button previousButton;
    @FXML
    private Button nextButton;
    @FXML
    private Label matchesLabel;

    private EventHandler<MouseEvent> mouseEventHandle = (MouseEvent event) -> handleMouseClicked(event);

    private void handleMouseClicked(MouseEvent event) {
        Node node = event.getPickResult().getIntersectedNode();
        // Accept double clicks only on node cells, and not on empty spaces of the TreeView
        if (event.getClickCount() == 2 && (node instanceof Text || (node instanceof TreeCell && ((TreeCell) node).getText() != null))) {
            matchesLabel.setText("");
            String str = listView.getSelectionModel().getSelectedItem();
            System.out.println("Node double click at: " + str);
            TextFileManager textFileManager = new TextFileManager();
            textFileManager.isFileContainText(textToSearch.getText(), Paths.get(str), false);
            System.out.println("Found " + textFileManager.getWordsPositions().size() + " matches");
            textArea = new StyleClassedTextArea();
            //read text from file to textArea
            TextFileManager.read(new File(str), textArea);
            for (Map.Entry<Integer, MatchWord> pair : textFileManager.getWordsPositions().entrySet()) {
                //add yellow background to words
                textArea.setStyle(pair.getValue().getFrom(), pair.getValue().getTo(), styleClasses);
//              System.out.println(pair.getKey()+" - from "+pair.getValue().getFrom()+" to "+pair.getValue().getTo());
            }
            scrollPane = new VirtualizedScrollPane(textArea);
            addNewTab(textArea, textFileManager.getWordsPositions());
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
    private void addNewTab(StyleClassedTextArea textArea, Map<Integer, MatchWord> wordPositions) {
        MyTab newTab = new MyTab(textArea, wordPositions);
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
        Integer allMatches = currentTab.getWordsPositions().size();
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
        Integer allMatches = currentTab.getWordsPositions().size();
        Integer currentMatch = currentTab.getMatchCounter() - 1;
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
