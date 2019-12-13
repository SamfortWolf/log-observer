package ru.samfort.logobserver.utils;

import org.fxmisc.richtext.StyleClassedTextArea;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

//Util class for working with text files
public class TextFileManager {
    private static List<String> yellowBackStyle = Collections.singletonList("yellow");
    private static final int MAPSIZE = 4 * 1024 * 1024;
    //key is start position and value is end position of matched word
    private HashMap<Integer, MatchWord> wordsPositions = new HashMap<>();
    private int linesCount=0;

    public int getLinesCount() {
        return linesCount;
    }

    //reading text from file to StyleClassedTextArea
    public static StyleClassedTextArea read(File fileFrom) {
        StyleClassedTextArea textAreaTo = new StyleClassedTextArea();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileFrom))) {
            char[] buf = new char[102400];//100Kb buf
            int byteCounter;
            while (reader.ready()) {
                byteCounter = reader.read(buf);
                textAreaTo.appendText(new String(buf, 0, byteCounter));
            }
            return textAreaTo;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static StyleClassedTextArea styleTextArea(StyleClassedTextArea textArea, Set<Map.Entry<Integer, MatchWord>> wordsPositions) {
        for (Map.Entry<Integer, MatchWord> pair : wordsPositions) {
            //add yellow background to words
            textArea.setStyle(pair.getValue().getFrom(), pair.getValue().getTo(), yellowBackStyle);
        }
        return textArea;
    }

    public boolean isFileContainText(String grepFor, Path pathToCheck, Boolean firstOnly) {
        final byte[] toSearch = grepFor.getBytes(StandardCharsets.UTF_8);
        int padding = 1; // need to scan 1 character ahead in case it is a word boundary.
        int carriageReturnCount = 0;//for windows
        int matches = 0;
        boolean inWord = false;
        try (FileChannel channel = FileChannel.open(pathToCheck, StandardOpenOption.READ)) {
            final long length = channel.size();
            int iterations = 0;
            int pos = 0;
            while (pos < length) {
                long remaining = length - pos;
                // int conversion is safe because of a safe MAPSIZE.. Assume a reasonably sized toSearch.
                int tryMap = MAPSIZE + toSearch.length + padding;
                int toMap = (int) Math.min(tryMap, remaining);
                // different limits depending on whether we are the last mapped segment.
                int limit = tryMap == toMap ? MAPSIZE : (toMap - toSearch.length);
                MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, pos, toMap);
                pos += (tryMap == toMap) ? MAPSIZE : toMap;
                for (int i = 0; i < limit; i++) {
                    final byte b = buffer.get(i);
                    if (b == '\n') {
                        inWord = false;
                        linesCount++;
                    } else if (b == ' ') {
                        inWord = false;
                    }
                    else if (b == '\r') {
                        carriageReturnCount++;
                        inWord = false;
                    } else if (!inWord) {
                        if (wordMatch(buffer, i, toMap, toSearch)) {
                            matches++;
                            if (!firstOnly) {
                                wordsPositions.put(matches, new MatchWord(i - carriageReturnCount + iterations, i + grepFor.length() - carriageReturnCount + iterations));
                                i += toSearch.length - 1;
                            } else {
                                return true;
                            }
                        } else {
                            inWord = true;
                        }
                    }
                }
                iterations += limit + 1;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return matches > 0;
    }

    private static boolean wordMatch(MappedByteBuffer buffer, int pos, int toMap, byte[] toSearch) {
        //assume at valid word start.
        for (int i = 0; i < toSearch.length; i++) {
            if (toSearch[i] != buffer.get(pos + i)) {
                return false;
            }
        }
        byte nxt = (pos + toSearch.length) == toMap ? (byte) ' ' : buffer.get(pos + toSearch.length);
        return nxt == ' ' || nxt == '\n' || nxt == '\r';
    }

    public HashMap<Integer, MatchWord> getWordsPositions() {
        return wordsPositions;
    }
}
