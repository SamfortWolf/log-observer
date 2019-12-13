package ru.samfort.logobserver.utils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.nio.file.Path;
import java.util.stream.Collectors;


public class TreeViewHelper {
    private static Path rootPath;
    private static TreeItem root;
    private static ObservableList<Path> observableList;

    public static void setRoot(Path path) {
        rootPath = path;
        root = new TreeItem(rootPath);
    }

    public static void setObservableList(ObservableSet<Path> observableSet) {
        observableList = FXCollections.observableList(observableSet.stream().collect(Collectors.toList()));
    }

    public static TreeItem getRoot() {
        return root;
    }

    public static void fill() {
        for (Path path : observableList) {
            TreeItem<String> current = root;
            path = rootPath.relativize(path);
            for (String component : path.toString().split("\\\\")) {
                current = getOrCreateChild(current, component);
            }
        }
    }

    private static TreeItem<String> getOrCreateChild(TreeItem<String> parent, String value) {
        for (TreeItem<String> child : parent.getChildren()) {
            if (value.equals(child.getValue())) {
                return child;
            }
        }
        TreeItem<String> newChild = new TreeItem<>(value);
        parent.getChildren().add(newChild);
        return newChild;
    }

    public static String pathBuilder(TreeItem<String> lastElement) {
        StringBuilder result = new StringBuilder(lastElement.getValue());
        TreeItem parent = lastElement.getParent();
        if (parent != null) {
            while (parent != null) {
                result.insert(0, parent.getValue() + "\\");
                parent = parent.getParent();
            }
        }
        return result.toString();
    }

    public static void expandAll (TreeItem<?> root){
        if(root != null && !root.isLeaf()){
            root.setExpanded(true);
            for(TreeItem<?> child:root.getChildren()){
                expandAll(child);
            }
        }
    }

    public static void collapseAll (TreeItem<?> root){
        if(root != null && !root.isLeaf()){
            root.setExpanded(false);
            for(TreeItem<?> child:root.getChildren()){
                collapseAll(child);
            }
        }
    }


}
