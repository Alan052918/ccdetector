package com.github.ccdetector;

import com.github.gumtreediff.gen.TreeGenerators;
import com.github.gumtreediff.tree.Tree;

import java.io.IOException;
import java.nio.file.Paths;

public class CoreRun {

    public static void main(String[] args) {
        CompoundChangeDetector.init();

//        String srcFileName = Paths.get("core/src/main/resources/", "src.py").toAbsolutePath().toString();
//        String dstFileName = Paths.get("core/src/main/resources/", "dst.py").toAbsolutePath().toString();
        String srcFileName = "/Users/aijunda/Developer/GitHub/MyRepos/python-refactor-detection/gumtree-playground/test_pysrc/test_method_2.py";
        String dstFileName = "/Users/aijunda/Developer/GitHub/MyRepos/python-refactor-detection/gumtree-playground/test_pysrc/test_method_2_control.py";

        try {
            Tree srcTree = TreeGenerators.getInstance().getTree(srcFileName).getRoot();
            System.out.println("****************************************************");
            System.out.println(srcTree.toTreeString());
            Tree dstTree = TreeGenerators.getInstance().getTree(dstFileName).getRoot();
            System.out.println("****************************************************");
            System.out.println(dstTree.toTreeString());
            System.out.println("****************************************************");
        } catch (IOException e) {
            e.printStackTrace();
        }

        CompoundChangeDetector.computeFileEditScript(srcFileName, dstFileName);
        CompoundChangeDetector.getEditScript().asList().forEach(action -> {
            System.out.println(action.toString());
        });

        CompoundChangeDetector.checkCompoundChanges(srcFileName, dstFileName);

        System.out.println("src: " + srcFileName);
        System.out.println("dst: " + dstFileName);
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
}
