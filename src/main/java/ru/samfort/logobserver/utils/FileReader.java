package ru.samfort.logobserver.utils;

import org.fxmisc.richtext.StyleClassedTextArea;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class FileReader {
    File file;
    StyleClassedTextArea textArea;

    public FileReader(File file, StyleClassedTextArea textArea) {
        this.file = file;
        this.textArea = textArea;
    }

    //reading text string by string from file to StyleClassedTextArea
    public void read() {
        try (BufferedReader reader = new BufferedReader(new java.io.FileReader(file))){
            char[] buf = new char[102400];
            while (reader.ready()){
                reader.read(buf);
                textArea.appendText(new String(buf));
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
