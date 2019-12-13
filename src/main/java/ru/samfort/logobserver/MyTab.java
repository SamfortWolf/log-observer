package ru.samfort.logobserver;

import javafx.scene.Node;
import javafx.scene.control.Tab;
import org.fxmisc.richtext.StyleClassedTextArea;
import ru.samfort.logobserver.utils.MatchWord;

import java.util.Map;

public class MyTab extends Tab {

    private Integer linesCounter;
    private Integer matchCounter = 0;
    private StyleClassedTextArea textArea;

    public Integer getLinesCounter() {
        return linesCounter;
    }

    private Map<Integer, MatchWord> wordsPositions;

    public MyTab(StyleClassedTextArea textArea, Map<Integer, MatchWord> wordsPositions, int linesCount) {
        this.textArea = textArea;
        this.wordsPositions = wordsPositions;
        this.linesCounter=linesCount;
    }

    public MyTab(String text, StyleClassedTextArea textArea, Map<Integer, MatchWord> wordsPositions) {
        super(text);
        this.textArea = textArea;
        this.wordsPositions = wordsPositions;
    }

    public MyTab(String text, Node content, StyleClassedTextArea textArea, Map<Integer, MatchWord> wordsPositions) {
        super(text, content);
        this.textArea = textArea;
        this.wordsPositions = wordsPositions;
    }

    public StyleClassedTextArea getTextArea() {
        return textArea;
    }

    public void setTextArea(StyleClassedTextArea textArea) {
        this.textArea = textArea;
    }

    public Map<Integer, MatchWord> getWordsPositions() {
        return wordsPositions;
    }

    public void setWordsPositions(Map<Integer, MatchWord> wordsPositions) {
        this.wordsPositions = wordsPositions;
    }

    public Integer getMatchCounter() {
        return matchCounter;
    }

    public void setMatchCounter(Integer matchCounter) {
        this.matchCounter = matchCounter;
    }
}
