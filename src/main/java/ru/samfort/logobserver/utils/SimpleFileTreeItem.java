package ru.samfort.logobserver.utils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Alexander Bolte - Bolte Consulting (2010 - 2014).
 *
 *         This class shall be a simple implementation of a TreeItem for
 *         displaying a file system tree.
 *
 *         The idea for this class is taken from the Oracle API docs found at
 *         http
 *         ://docs.oracle.com/javafx/2/api/javafx/scene/control/TreeItem.html.
 *
 *         Basically the file sytsem will only be inspected once. If it changes
 *         during runtime the whole tree would have to be rebuild. Event
 *         handling is not provided in this implementation.
 */

/**
 *          Modified by Mikhail Novikov, 2019
 *          Add filtering (file type) functionality
 */
public class SimpleFileTreeItem extends TreeItem<File> {
    private boolean isFirstTimeChildren = true;
    private boolean isFirstTimeLeaf = true;
    private boolean isLeaf;
    //file type to search
    private String filterType;
    private String textToSearch;

    /**
     * Calling the constructor of super class in oder to create a new
     * TreeItem<File>.
     *
     * @param f
     *            an object of type File from which a tree should be build or
     *            which children should be gotten.
     */
    public SimpleFileTreeItem(File f, String filterType) {
        super(f);
        this.filterType=filterType;
    }
    public SimpleFileTreeItem(File f, String filterType, String textToSearch) {
        super(f);
        this.filterType=filterType;
        this.textToSearch=textToSearch;
    }

    /*
     * (non-Javadoc)
     *
     * @see javafx.scene.control.TreeItem#getChildren()
     */
    @Override
    public ObservableList<TreeItem<File>> getChildren() {
        if (isFirstTimeChildren) {
            isFirstTimeChildren = false;
            /*
             * First getChildren() call, so we actually go off and determine the
             * children of the File contained in this TreeItem.
             */
            super.getChildren().setAll(buildChildren(this));
        }
        return super.getChildren();
    }

    /*
     * (non-Javadoc)
     *
     * @see javafx.scene.control.TreeItem#isLeaf()
     */
    @Override
    public boolean isLeaf() {
        if (isFirstTimeLeaf) {
            isFirstTimeLeaf = false;
            File f = getValue();
            isLeaf = f.isFile();
        }
        return isLeaf;
    }

    /**
     * Returning a collection of type ObservableList containing TreeItems, which
     * represent all children available in handed TreeItem.
     *
     * @param TreeItem
     *            the root node from which children a collection of TreeItem
     *            should be created.
     * @return an ObservableList<TreeItem<File>> containing TreeItems, which
     *         represent all children available in handed TreeItem. If the
     *         handed TreeItem is a leaf, an empty list is returned.
     */
    private ObservableList<TreeItem<File>> buildChildren(TreeItem<File> TreeItem) {
        File f = TreeItem.getValue();
            if (f != null && f.isDirectory() && f.listFiles() != null) {
                File[] files = f.listFiles();
                if (files != null) {
                    ObservableList<TreeItem<File>> children = FXCollections.observableArrayList();
                    //childrenPopulator(f, children);
//                System.out.println("We are here. "+f.toPath().toString());
//                    childrenPopulator(f, children);
//                    children.forEach(System.out::println);
                    for (File childFile : files) {
                        //filtering by file type and text to search
                        if (childFile.isFile() && childFile.getName().endsWith(filterType) /*&& (TextFileManager.isFileContainText(textToSearch, childFile.toPath()))*/
                                || (childFile.isDirectory() /*&& hasFiltered(childFile.listFiles())*/)) {
                            children.add(new SimpleFileTreeItem(childFile, filterType, textToSearch));
                        }
                    }
                    return children;
                }
            }
        return FXCollections.emptyObservableList();
    }

    private void childrenPopulator (File root, ObservableList<TreeItem<File>> childrenList){
        File [] filesArr = root.listFiles();
        for (File f: filesArr){
            if (f.isDirectory()){

            }
        }

        List<Path> result = new ArrayList<>();
        Queue<File> fileTree = new PriorityQueue<>();
        Collections.addAll(fileTree, root.listFiles());
        while (!fileTree.isEmpty())
        {
            File currentFile = fileTree.remove();
            if(currentFile.isDirectory()){
                Collections.addAll(fileTree, currentFile.listFiles());
            } else if (currentFile.getName().endsWith(filterType) /*&& TextFileManager.isFileContainText(textToSearch, currentFile.toPath())*/) {
                Path currentPath = currentFile.toPath();
                result.add(currentPath);
                if (!result.contains(currentPath.getParent())){
                    result.add(currentPath.getParent());
                }
            }
        }
        childrenList.addAll(result.stream().map(f->new SimpleFileTreeItem(f.toFile(), filterType, textToSearch)).collect(Collectors.toSet()));
    }

//    private void childrenPopulator (File root, ObservableList<TreeItem<File>> childrenList){
//        List<Path> result = new ArrayList<>();
//        Queue<File> fileTree = new PriorityQueue<>();
//        Collections.addAll(fileTree, root.listFiles());
//        while (!fileTree.isEmpty())
//        {
//            File currentFile = fileTree.remove();
//            if(currentFile.isDirectory()){
//                Collections.addAll(fileTree, currentFile.listFiles());
//            } else if (currentFile.getName().endsWith(filterType) && TextFileManager.isFileContainText(textToSearch, currentFile.toPath())) {
//                Path currentPath = currentFile.toPath();
//                result.add(currentPath);
//                if (!result.contains(currentPath.getParent())){
//                    result.add(currentPath.getParent());
//                }
//            }
//        }
//        childrenList.addAll(result.stream().map(f->new SimpleFileTreeItem(f.toFile(), filterType, textToSearch)).collect(Collectors.toSet()));
//    }



    /*private void childrenPopulator (File root, ObservableList<TreeItem<File>> childrenList){
        System.out.println("populator");
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(root.toPath(), filterType)) {
            for (Path children : directoryStream) {
                System.out.print("Path: "+children.toString());
                if (Files.isRegularFile(children)&&TextFileManager.isFileContainText(textToSearch, children)){
                    System.out.println(" is file");
                    childrenList.add(new SimpleFileTreeItem(children.toFile(), filterType, textToSearch));
                }
                else if (Files.isDirectory(children)){
                    childrenList.add(new SimpleFileTreeItem(children.toFile(), filterType, textToSearch));
                    System.out.println(" is directory");
                    //childrenPopulator(children.toFile(), childrenList);
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    //checking for filtered files in the array
    private boolean hasFiltered (File [] arr){
        List<File> list;
        if (arr!= null && arr.length>0) {
            for (File f: arr){
                hasFiltered(f.listFiles());

            }
            list = Arrays.asList(arr);
            return list.stream().anyMatch(f -> f.getName().endsWith(filterType));
        }
        else
            return false;
    }
}