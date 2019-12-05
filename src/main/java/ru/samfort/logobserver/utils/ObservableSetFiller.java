package ru.samfort.logobserver.utils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class ObservableSetFiller {
    private ObservableSet<String> observableSet = FXCollections.observableSet();

    public void fillObservableSet(File root, String filter, String textToSearch) {
        if (Files.isReadable(root.toPath())) {
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(root.toPath())) {
                for (Path children : directoryStream) {
                    if (Files.isRegularFile(children) && children.getFileName().toString().endsWith(filter) &&
                            TextFileManager.isFileContainText(textToSearch, children)) {
                        observableSet.add(children.toString());
                    } else if (Files.isDirectory(children)) {
                        this.fillObservableSet(children.toFile(), filter, textToSearch);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public ObservableSet<String> getObservableSet() {
        return observableSet;
    }
}
