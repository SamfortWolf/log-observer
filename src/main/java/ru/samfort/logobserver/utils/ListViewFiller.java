package ru.samfort.logobserver.utils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class ListViewFiller {
    private ObservableList<String> observableList = FXCollections.emptyObservableList();

    public ListView<String> getListView (File root, String filter, String textToSearch) throws IOException {
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(root.toPath())) {
            for (Path children : directoryStream) {
                if (Files.isRegularFile(children)&&children.getFileName().endsWith(filter)&&TextFileManager.isFileContainText(textToSearch,children)){
                    observableList.add(children.toString());
                }
                else if (Files.isDirectory(children)){
                    this.getListView(children.toFile(), filter, textToSearch);
                }
            }
        }
        return new ListView<>(observableList);
    }
}
