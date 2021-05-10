package com.github.ccdetector;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class APIExtractorTest {

    String coreTestResourcesDirectory;

    @BeforeEach
    void setUp() {
        coreTestResourcesDirectory = "src/test/resources/";
    }

    @Test
    void getAllPythonFiles() {
        String workingDirectoryString =
                Paths.get(coreTestResourcesDirectory, "test_dir_level_0").toAbsolutePath().toString();
        System.out.println("***********************All Python Files***********************");
        APIExtractor.getAllPythonFiles(workingDirectoryString).forEach(pythonFile -> {
            System.out.println(pythonFile.getAbsolutePath());
        });
        System.out.println("***********************All Python Files***********************");
    }

    @Test
    void fileIsPython() {
        String pythonFileString =
                Paths.get(coreTestResourcesDirectory, "test_dst_mrel.py").toAbsolutePath().toString();
        File pythonFile = new File(pythonFileString);
        assertTrue(APIExtractor.fileIsPython(pythonFile));

        File javaFile = new File("CompoundChangeDetectorTest.java");
        assertFalse(APIExtractor.fileIsPython(javaFile));
    }

    @Test
    void printDirectoryTree() {
        String workingDirectoryString =
                Paths.get(coreTestResourcesDirectory, "test_dir_level_0").toAbsolutePath().toString();
        System.out.println("************************Directory Tree************************");
        APIExtractor.printDirectoryTree(workingDirectoryString);
        System.out.println("************************Directory Tree************************");
    }
}