package com.github.ccdetector;

import java.io.*;
import java.util.ArrayList;

public class APIExtractor {

    public static String framework;
    public static String fileName;
    public static String repoUrl;

    private static final String CYAN = "\033[0;36m"; // CYAN
    private static final String RESET = "\033[0m"; // Text Reset

    /**
     * Get all Python files under the directory specified by targetDirectoryString
     *
     * @param targetDirectoryString the String specifying the directory to be searched
     * @return an ArrayList of Files containing all Python files under the specified directory
     */
    public static ArrayList<File> getAllPythonFiles(String targetDirectoryString) {
        File targetDirectory = new File(targetDirectoryString);
        return getAllPythonFiles(targetDirectory);
    }

    /**
     * Get all Python files under targetDirectory
     *
     * @param targetDirectory the directory to be searched
     * @return an ArrayList of Files containing all Python files under targetDirectory
     */
    public static ArrayList<File> getAllPythonFiles(File targetDirectory) {
        if (!targetDirectory.exists()) {
            System.err.printf("Directory \"%s\" not exist.\n", targetDirectory.toString());
            return null;
        }
        ArrayList<File> pythonFiles = new ArrayList<File>();
        ArrayList<File> subDirectories = new ArrayList<File>();
        for (File file : targetDirectory.listFiles()) {
            if (file.isFile()) {
                if (fileIsPython(file)) {
                    pythonFiles.add(file);
                }
            } else if (file.isDirectory()) {
                subDirectories.add(file);
            } else {
                System.out.println("ERROR");
            }
        }
        for (File subDirectory : subDirectories) {
            pythonFiles.addAll(getAllPythonFiles(subDirectory));
        }
        return pythonFiles;
    }

    /**
     * Check if given File is a Python file
     *
     * @param file the File instance to be checked
     * @return true if file is a Python file, false otherwise
     */
    public static boolean fileIsPython(File file) {
        String fileName = file.getName();
        int lastIndex = fileName.lastIndexOf('.');
        if (lastIndex > 0 && fileName.substring(lastIndex + 1).equals("py")) {
            return true;
        }
        return false;
    }

    /**
     * Print the tree structure (files first, directories last) of the directory specified by targetDirectoryString
     *
     * @param targetDirectoryString the String specifying the directory to be printed
     */
    public static void printDirectoryTree(String targetDirectoryString) {
        File targetDirectory = new File(targetDirectoryString);
        printDirectoryTree(targetDirectory);
    }

    /**
     * Print the tree structure (files first, directories last) of targetDirectory
     *
     * @param targetDirectory the directory to be printed
     */
    public static void printDirectoryTree(File targetDirectory) {
        printDirectoryTree(targetDirectory, 0);
    }

    /**
     * Print the tree structure (files first, directories last) of targetDirectory with specified indent level
     *
     * @param targetDirectory the directory to be printed
     * @param indentLevel     indent level marker
     */
    public static void printDirectoryTree(File targetDirectory, int indentLevel) {
        if (!targetDirectory.exists()) {
            System.err.printf("Directory \"%s\" not exist.\n", targetDirectory.toString());
            return;
        }
        ArrayList<File> subDirectories = new ArrayList<File>();
        printIndentation(indentLevel);
        System.out.println(targetDirectory);
        for (File file : targetDirectory.listFiles()) {
            if (file.isFile()) {
                printIndentation(indentLevel);
                if (fileIsPython(file)) {
                    System.out.println(CYAN + file.getName() + RESET);
                } else {
                    System.out.println(file.getName());
                }
            } else if (file.isDirectory()) {
                subDirectories.add(file);
            } else {
                printIndentation(indentLevel);
                System.out.println("ERROR");
            }
        }
        System.out.println();
        for (File subDirectory : subDirectories) {
            printDirectoryTree(subDirectory, indentLevel + 1);
        }
    }

    private static void printIndentation(int indentLevel) {
        for (int i = 0; i < indentLevel; i++) {
            System.out.print("    ");
        }
    }
}
