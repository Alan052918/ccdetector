package com.github.ccdetector;

import com.github.ccdetector.CompoundChangeDetector;
import com.github.gumtreediff.gen.TreeGenerators;
import com.github.gumtreediff.tree.Tree;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;

class CompoundChangeDetectorTest {

    String coreTestResourcesDirectory;

    @BeforeEach
    void setUp() {
        CompoundChangeDetector.init();
        coreTestResourcesDirectory = "src/test/resources/";
    }

    @Test
    void computeFileEditScript() {
        String srcFile = Paths.get(coreTestResourcesDirectory, "test_src_mren.py").toAbsolutePath().toString();
        String dstFile = Paths.get(coreTestResourcesDirectory, "test_dst_mren.py").toAbsolutePath().toString();
        CompoundChangeDetector.computeFileEditScript(srcFile, dstFile);

        System.out.println("src: " + srcFile);
        System.out.println("dst: " + dstFile);
        CompoundChangeDetector.getEditScript().asList().forEach(action -> {
            System.out.println(action.toString());
        });
    }

    @Test
    void computeTreeEditScript() {
        String srcFile = Paths.get(coreTestResourcesDirectory, "test_src_mren.py").toAbsolutePath().toString();
        String dstFile = Paths.get(coreTestResourcesDirectory, "test_dst_mren.py").toAbsolutePath().toString();
        Tree srcTree = null;
        Tree dstTree = null;

        try {
            srcTree = TreeGenerators.getInstance().getTree(srcFile).getRoot();
            System.out.println("*******************************************" + srcFile);
            System.out.println(srcTree.toTreeString());
            System.out.println("*******************************************");
            dstTree = TreeGenerators.getInstance().getTree(dstFile).getRoot();
            System.out.println("*******************************************" + dstFile);
            System.out.println(dstTree.toTreeString());
            System.out.println("*******************************************");
        } catch (IOException e) {
            e.printStackTrace();
        }

        CompoundChangeDetector.computeTreeEditScript(srcTree, dstTree).asList().forEach(action -> {
            System.out.println(action.toString());
        });
    }

    @Test
    void checkCompoundChanges() {
        String srcFile = Paths.get(coreTestResourcesDirectory, "test_src_param.py").toAbsolutePath().toString();
        String dstFile = Paths.get(coreTestResourcesDirectory, "test_dst_param.py").toAbsolutePath().toString();
        CompoundChangeDetector.checkCompoundChanges(srcFile, dstFile);

        System.out.println("src: " + srcFile);
        System.out.println("dst: " + dstFile);
        CompoundChangeDetector.getMethodRenamingRecords().forEach(methodRenaming -> {
            System.out.println(methodRenaming.toString());
        });
        CompoundChangeDetector.getMethodRelocationRecords().forEach(methodRelocation -> {
            System.out.println(methodRelocation.toString());
        });
        CompoundChangeDetector.getParameterChangeRecords().forEach(parameterChange -> {
            System.out.println(parameterChange.toString());
        });
        CompoundChangeDetector.getParameterDefaultValueChangeRecords().forEach(parameterDefaultValueChange -> {
            System.out.println(parameterDefaultValueChange.toString());
        });
        CompoundChangeDetector.getReturnTypeChangeRecords().forEach(returnTypeChange -> {
            System.out.println(returnTypeChange.toString());
        });
    }

    @Test
    void checkMethodRenaming() {
        String srcFile = Paths.get(coreTestResourcesDirectory, "test_src_mren.py").toAbsolutePath().toString();
        String dstFile = Paths.get(coreTestResourcesDirectory, "test_dst_mren.py").toAbsolutePath().toString();
        CompoundChangeDetector.computeFileEditScript(srcFile, dstFile);
        CompoundChangeDetector.checkMethodRenaming();

        System.out.println("src: " + srcFile);
        System.out.println("dst: " + dstFile);
        CompoundChangeDetector.getMethodRenamingRecords().forEach(methodRenaming -> {
            System.out.println(methodRenaming.toString());
        });
    }

    @Test
    void checkMethodRelocation() {
        String srcFile = Paths.get(coreTestResourcesDirectory, "test_src_mrel.py").toAbsolutePath().toString();
        String dstFile = Paths.get(coreTestResourcesDirectory, "test_dst_mrel.py").toAbsolutePath().toString();
        CompoundChangeDetector.computeFileEditScript(srcFile, dstFile);
        CompoundChangeDetector.checkMethodRelocation();

        System.out.println("src: " + srcFile);
        System.out.println("dst: " + dstFile);
        CompoundChangeDetector.getMethodRelocationRecords().forEach(methodRelocation -> {
            System.out.println(methodRelocation.toString());
        });
    }

    @Test
    void checkParameterChanges() {
        String srcFile = Paths.get(coreTestResourcesDirectory, "test_src_param.py").toAbsolutePath().toString();
        String dstFile = Paths.get(coreTestResourcesDirectory, "test_dst_param.py").toAbsolutePath().toString();
        CompoundChangeDetector.computeFileEditScript(srcFile, dstFile);
        CompoundChangeDetector.checkMethodRenaming();
        CompoundChangeDetector.checkParameterChanges();

        System.out.println("src: " + srcFile);
        System.out.println("dst: " + dstFile);
        CompoundChangeDetector.getParameterChangeRecords().forEach(parameterChange -> {
            System.out.println(parameterChange.toString());
        });
    }

    @Test
    void checkParameterDefaultValueChange() {
        String srcFile = Paths.get(coreTestResourcesDirectory, "test_src_param.py").toAbsolutePath().toString();
        String dstFile = Paths.get(coreTestResourcesDirectory, "test_dst_param.py").toAbsolutePath().toString();
        CompoundChangeDetector.computeFileEditScript(srcFile, dstFile);
        CompoundChangeDetector.checkMethodRenaming();
        CompoundChangeDetector.checkParameterChanges();
        CompoundChangeDetector.checkParameterDefaultValueChange();

        System.out.println("src: " + srcFile);
        System.out.println("dst: " + dstFile);
        CompoundChangeDetector.getParameterDefaultValueChangeRecords().forEach(parameterDefaultValueChange -> {
            System.out.println(parameterDefaultValueChange.toString());
        });
    }

    @Test
    void checkReturnTypeChange() {
        String srcFile = Paths.get(coreTestResourcesDirectory, "test_src_ret.py").toAbsolutePath().toString();
        String dstFile = Paths.get(coreTestResourcesDirectory, "test_dst_ret.py").toAbsolutePath().toString();
        CompoundChangeDetector.computeFileEditScript(srcFile, dstFile);
        CompoundChangeDetector.checkReturnTypeChange();

        System.out.println("src: " + srcFile);
        System.out.println("dst: " + dstFile);
        CompoundChangeDetector.getReturnTypeChangeRecords().forEach(returnTypeChange -> {
            System.out.println(returnTypeChange.toString());
        });
    }
}