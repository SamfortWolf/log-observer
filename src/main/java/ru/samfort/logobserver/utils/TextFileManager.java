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

//Util class for working with text files
public class TextFileManager {
    private static final int MAPSIZE = 4 * 1024 ; // 4K - make this * 1024 to 4MB in a real system.

    //reading text string by string from file to StyleClassedTextArea
    public static void read(File fileFrom, StyleClassedTextArea textAreaTo) {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileFrom))){
            char[] buf = new char[102400];
            while (reader.ready()){
                reader.read(buf);
                textAreaTo.appendText(new String(buf));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //https://sprosi.pro/questions/10033/byistryiy-sposob-poiska-stroki-v-tekstovom-fayle
    public static boolean isFileContainText(String grepFor, Path pathToCheck) {
        final byte[] toSearch = grepFor.getBytes(StandardCharsets.UTF_8);
        int padding = 1; // need to scan 1 character ahead in case it is a word boundary.
        int matches = 0;
        boolean inWord = false;
        boolean scanToLineEnd = false;
        try (FileChannel channel = FileChannel.open(pathToCheck, StandardOpenOption.READ)) {
            final long length = channel.size();
            int pos = 0;
            while (pos < length) {
                long remaining = length - pos;
                // int conversion is safe because of a safe MAPSIZE.. Assume a reasonably sized toSearch.
                int tryMap = MAPSIZE + toSearch.length + padding;
                int toMap = (int)Math.min(tryMap, remaining);
                // different limits depending on whether we are the last mapped segment.
                int limit = tryMap == toMap ? MAPSIZE : (toMap - toSearch.length);
                MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, pos, toMap);
                pos += (tryMap == toMap) ? MAPSIZE : toMap;
                for (int i = 0; i < limit; i++) {
                    final byte b = buffer.get(i);
                    if (scanToLineEnd) {
                        if (b == '\n') {
                            scanToLineEnd = false;
                            inWord = false;
                        }
                    } else if (b == '\n') {
                        inWord = false;
                    } else if (b == '\r' || b == ' ') {
                        inWord = false;
                    } else if (!inWord) {
                        if (wordMatch(buffer, i, toMap, toSearch)) {
                            matches++;
                            System.out.println("File "+pathToCheck.toString()+" contains \""+grepFor+"\"");
                            break;
                        } else {
                            inWord = true;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return matches>0?true:false;
    }

    private static boolean wordMatch(MappedByteBuffer buffer, int pos, int tomap, byte[] tosearch) {
        //assume at valid word start.
        for (int i = 0; i < tosearch.length; i++) {
            if (tosearch[i] != buffer.get(pos + i)) {
                return false;
            }
        }
        byte nxt = (pos + tosearch.length) == tomap ? (byte)' ' : buffer.get(pos + tosearch.length);
        return nxt == ' ' || nxt == '\n' || nxt == '\r';
    }
}
