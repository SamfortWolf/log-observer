package ru.samfort.logobserver.utils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class ObservableSetFiller {
    private ObservableSet<Path> observableSet = FXCollections.observableSet();
    private TextFileManager textFileManager = new TextFileManager();

    public void fillObservableSet(File root, String filter, String textToSearch, boolean withSubs) {
        //first - check accessibility of path
        if (Files.isReadable(root.toPath())) {
            //then recursive search and fill observableSet
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(root.toPath())) {
                for (Path children : directoryStream) {
                    if (Files.isRegularFile(children) && children.getFileName().toString().endsWith(filter) &&
                            textFileManager.isFileContainText(textToSearch, children, true)) {
                        observableSet.add(children);
                    } else if (Files.isDirectory(children) && withSubs) {
                        this.fillObservableSet(children.toFile(), filter, textToSearch, withSubs);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public ObservableSet<Path> getObservableSet() {
        return observableSet;
    }
}
