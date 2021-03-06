package com.github.ccdetector;

import com.github.ccdetector.changes.*;
import com.github.gumtreediff.actions.EditScript;
import com.github.gumtreediff.actions.EditScriptGenerator;
import com.github.gumtreediff.actions.SimplifiedChawatheScriptGenerator;
import com.github.gumtreediff.actions.model.*;
import com.github.gumtreediff.client.Run;
import com.github.gumtreediff.gen.TreeGenerators;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.Tree;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class CompoundChangeDetector {

    private static EditScript editScript;

    private static ArrayList<FunctionRenaming> functionRenamingRecords = new ArrayList<>();
    private static ArrayList<FunctionRelocation> functionRelocationRecords = new ArrayList<>();
    private static ArrayList<ParameterChange> parameterChangeRecords = new ArrayList<>();
    private static ArrayList<ParameterDefaultValueChange> parameterDefaultValueChangeRecords = new ArrayList<>();
    private static ArrayList<ReturnTypeChange> returnTypeChangeRecords = new ArrayList<>();

    private static ArrayList<Tree> addedDefaultValueNodes = new ArrayList<>();
    private static ArrayList<Tree> removedDefaultValueNodes = new ArrayList<>();

    /**
     * Initialize generators used to compute edit scripts
     */
    public static void init() {
        Run.initGenerators();
    }

    /**
     * Compute edit script deduced that transform AST in srcFileName to AST in dstFileName
     *
     * @param srcFileName source file name
     * @param dstFileName destination file name
     */
    public static void computeFileEditScript(String srcFileName, String dstFileName) {
        Tree srcTree = null;
        Tree dstTree = null;

        try {
            srcTree = TreeGenerators.getInstance().getTree(srcFileName).getRoot();
            dstTree = TreeGenerators.getInstance().getTree(dstFileName).getRoot();
        } catch (IOException e) {
            e.printStackTrace();
        }

        editScript = computeTreeEditScript(srcTree, dstTree);
    }

    /**
     * Compute edit script deduced that transform srcTree to dstTree
     *
     * @param srcTree source file abstract syntax tree
     * @param dstTree destination file abstract syntax tree
     * @return edit script that transform srcTree to dstTree
     */
    public static EditScript computeTreeEditScript(Tree srcTree, Tree dstTree) {
        Matcher defaultMatcher = Matchers.getInstance().getMatcher();
        MappingStore mappings = defaultMatcher.match(srcTree, dstTree);
        EditScriptGenerator editScriptGenerator = new SimplifiedChawatheScriptGenerator();
        return editScriptGenerator.computeActions(mappings);
    }

    /**
     * Check for compound changes in given files, add detected changes to compoundChangeRecords
     *
     * @param srcFileName earlier version of source file
     * @param dstFileName later version of source file
     */
    public static void checkCompoundChanges(String srcFileName, String dstFileName) {
        computeFileEditScript(srcFileName, dstFileName);
        checkFunctionRenaming();
//        checkMethodRelocation();
        checkParameterChanges();
        checkParameterDefaultValueChange();
        checkReturnTypeChange();
    }

    /**
     * Check for {@link FunctionRenaming} in editScript
     */
    public static void checkFunctionRenaming() {
        for (Action action : editScript.asList()) {
            Tree node = action.getNode();
            if (action instanceof Update &&
                    node.getType().toString().equals("name") &&
                    node.getParent().getType().toString().equals("funcdef") &&
                    node.positionInParent() == 0) {
                /* Method renaming */
                FunctionRenaming functionRenaming;
                String oldFunctionName = node.getLabel();
                String newFunctionName = ((Update) action).getValue();
                if (oldFunctionName.startsWith("_") &&
                        !newFunctionName.startsWith("_")) {
                    functionRenaming = new FunctionRenaming(oldFunctionName, newFunctionName,
                            FunctionRenaming.Type.PRIVATE_TO_PUBLIC);
                } else if (!oldFunctionName.startsWith("_") &&
                        newFunctionName.startsWith("_")) {
                    functionRenaming = new FunctionRenaming(oldFunctionName, newFunctionName,
                            FunctionRenaming.Type.PUBLIC_TO_PRIVATE);
                } else {
                    functionRenaming = new FunctionRenaming(oldFunctionName, newFunctionName,
                            FunctionRenaming.Type.NO_SWITCH);
                }
                functionRenamingRecords.add(functionRenaming);
            }
        }
    }

    /**
     * Check for {@link FunctionRelocation} in editScript
     */
    public static void checkFunctionRelocation() {
        for (Action action : editScript.asList()) {
            Tree node = action.getNode();
            if (action instanceof Move &&
                    node.getType().toString().equals("funcdef")) {
                /* Same-file method relocation */
                String functionName = node.getChild(0).getLabel();
                int oldLocation = node.positionInParent();
                int newLocation = ((Move) action).getPosition();
                FunctionRelocation functionRelocation = new FunctionRelocation(functionName, oldLocation, newLocation);
                functionRelocationRecords.add(functionRelocation);
            }
        }
    }

    /**
     * Check for {@link ParameterChange} in editScript
     */
    public static void checkParameterChanges() {
        for (Action action : editScript.asList()) {
            Tree node = action.getNode();
            if (action instanceof TreeInsert &&
                    node.getType().toString().equals("param")) {
                /* Parameter insertion, might affiliate with default value addition */
                checkParameterInsertionRemoval(node, true);
            } else if (action instanceof TreeDelete &&
                    node.getType().toString().equals("param")) {
                /* Parameter removal, might affiliate with default value removal */
                checkParameterInsertionRemoval(node, false);
            } else if (action instanceof Insert &&
                    node.getParent().getType().toString().equals("parameters") &&
                    node.getType().toString().equals("operator") &&
                    node.getLabel().equals("*")) {
                /* Parameter "*" insertion */
                String functionName = updatedNodeFunctionName(node);
                ParameterChange parameterAsteriskInsertion =
                        new ParameterChange(functionName, "", "*",
                                ParameterChange.Type.PARAMETER_INSERTION);
                parameterChangeRecords.add(parameterAsteriskInsertion);
            } else if (action instanceof Delete &&
                    node.getParent().getType().toString().equals("parameters") &&
                    node.getType().toString().equals("operator") &&
                    node.getLabel().equals("*")) {
                /* Parameter "*" removal */
                String functionName = updatedNodeFunctionName(node);
                ParameterChange parameterAsteriskRemoval =
                        new ParameterChange(functionName, "*", "",
                                ParameterChange.Type.PARAMETER_REMOVAL);
                parameterChangeRecords.add(parameterAsteriskRemoval);
            } else if (action instanceof Update &&
                    node.getParent().getType().toString().equals("param") &&
                    node.getType().toString().equals("name")) {
                /* Parameter update */
                String functionName = updatedNodeFunctionName(node);
                ParameterChange parameterUpdate;
                String oldParameterName = node.getLabel();
                String newParameterName = ((Update) action).getValue();
                if ((oldParameterName.equals("cls") &&
                        newParameterName.equals("self")) ||
                        (oldParameterName.equals("self") &&
                                newParameterName.equals("cls"))) {
                    /* cls/self switch */
                    parameterUpdate = new ParameterChange(functionName, oldParameterName, newParameterName,
                            ParameterChange.Type.CLS_SELF_SWITCH);
                } else {
                    /* Normal update */
                    parameterUpdate = new ParameterChange(functionName, oldParameterName, newParameterName,
                            ParameterChange.Type.PARAMETER_NORMAL_UPDATE);
                }
                parameterChangeRecords.add(parameterUpdate);
            }
        }
    }

    /**
     * Check for {@link ParameterDefaultValueChange} in editScript
     */
    public static void checkParameterDefaultValueChange() {
        for (Action action : editScript.asList()) {
            Tree node = action.getNode();
            if (action instanceof TreeInsert) {
                if (node.getType().toString().equals("param") &&
                        node.getChildren().size() > 2 &&
                        node.getChild(1).getLabel().equals("=")) {
                    /* Parameter-removal-affiliate default value addition */
                    checkParameterDefaultValueAdditionRemoval(node, true);
                } else if (node.getParent().getType().toString().equals("param") &&
                        node.getType().toString().equals("atom")) {
                    /* Parameter default value (list/dictionary) addition / update (different data type) */
                    addedDefaultValueNodes.add(node);
                }
            } else if (action instanceof TreeDelete) {
                if (node.getType().toString().equals("param") &&
                        node.getChildren().size() > 2 &&
                        node.getChild(1).getLabel().equals("=")) {
                    /* Parameter-removal-affiliate default value removal */
                    checkParameterDefaultValueAdditionRemoval(node, false);
                } else if (node.getParent().getType().toString().equals("param") &&
                        node.getType().toString().equals("atom")) {
                    /* Parameter default value (list/dictionary) removal / update (different data type) */
                    checkParameterDefaultValueRemovalUpdate(node);
                }
            } else if (action instanceof Insert &&
                    node.getParent().getType().toString().equals("param") &&
                    (node.getType().toString().equals("number") ||
                            node.getType().toString().equals("string"))) {
                /* Parameter default value (number/string) addition / update (different data type) */
                addedDefaultValueNodes.add(node);
            } else if (action instanceof Delete &&
                    node.getParent().getType().toString().equals("param") &&
                    (node.getType().toString().equals("number") ||
                            node.getType().toString().equals("string"))) {
                /* Parameter default value (number/string) removal / update (different data type) */
                checkParameterDefaultValueRemovalUpdate(node);
            } else if (action instanceof Update &&
                    node.getParent().getType().toString().equals("param") &&
                    !node.getType().toString().equals("name")) {
                /* Parameter default value update (same data type) */
                String functionName = updatedNodeFunctionName(node);
                String parameterName = updatedNodeParameterName(node);
                String oldDefaultValueString = node.getLabel();
                String newDefaultValueString = ((Update) action).getValue();
                ParameterDefaultValueChange parameterDefaultValueUpdate =
                        new ParameterDefaultValueChange(functionName, parameterName,
                                oldDefaultValueString, newDefaultValueString,
                                ParameterDefaultValueChange.Type.PARAMETER_DEFAULT_VALUE_UPDATE);
                parameterDefaultValueChangeRecords.add(parameterDefaultValueUpdate);
            }
        }
        collectParameterDefaultValueAdditionRemoval(addedDefaultValueNodes, true);
        collectParameterDefaultValueAdditionRemoval(removedDefaultValueNodes, false);
    }

    /**
     * Check for
     * {@link ParameterChange.Type#PARAMETER_INSERTION} and
     * {@link ParameterChange.Type#PARAMETER_REMOVAL}
     * with given inserted/removed subtree
     *
     * @param node        the root of the subtree inserted/removed
     * @param isInsertion true if the subtree is inserted, false otherwise
     */
    private static void checkParameterInsertionRemoval(Tree node, boolean isInsertion) {
        String functionName = node.getParents().get(1).getChild(0).getLabel();
        String parameterName = node.getChild(0).getLabel();
        /* Parameter change */
        ParameterChange parameterChange = isInsertion ?
                new ParameterChange(functionName, "", parameterName,
                        ParameterChange.Type.PARAMETER_INSERTION) :
                new ParameterChange(functionName, parameterName, "",
                        ParameterChange.Type.PARAMETER_REMOVAL);
        parameterChangeRecords.add(parameterChange);
    }

    /**
     * Check for
     * {@link ParameterDefaultValueChange.Type#PARAMETER_DEFAULT_VALUE_ADDITION} and
     * {@link ParameterDefaultValueChange.Type#PARAMETER_DEFAULT_VALUE_REMOVAL}
     * affiliated with inserting/removing node
     *
     * @param node       the param node inserted/removed
     * @param isAddition true if the subtree is inserted, false otherwise
     */
    private static void checkParameterDefaultValueAdditionRemoval(Tree node, boolean isAddition) {
        String functionName = updatedNodeFunctionName(node);
        String parameterName = node.getChild(0).getLabel();
        String defaultValueString;
        switch (node.getChildren().size()) {
            case 3:
                defaultValueString = "None";
                break;
            case 4:
                defaultValueString = node.getChild(2).getType().toString().equals("atom") ?
                        node.getChild(2).toTreeString() : node.getChild(2).getLabel();
                break;
            default:
                System.err.println("Unidentified TreeInsert/TreeDelete action");
                return;
        }
        /* Parameter default value addition/removal affiliated with parameter insertion/removal */
        ParameterDefaultValueChange parameterDefaultValueChange = isAddition ?
                new ParameterDefaultValueChange(functionName, parameterName, "", defaultValueString,
                        ParameterDefaultValueChange.Type.PARAMETER_DEFAULT_VALUE_ADDITION) :
                new ParameterDefaultValueChange(functionName, parameterName, defaultValueString, "",
                        ParameterDefaultValueChange.Type.PARAMETER_DEFAULT_VALUE_REMOVAL);
        parameterDefaultValueChangeRecords.add(parameterDefaultValueChange);
    }

    /**
     * Check for
     * {@link ParameterDefaultValueChange.Type#PARAMETER_DEFAULT_VALUE_REMOVAL} and
     * {@link ParameterDefaultValueChange.Type#PARAMETER_DEFAULT_VALUE_UPDATE} (different data type, removed number/string)
     * with given removed node
     *
     * @param removedNode removed AST node
     */
    private static void checkParameterDefaultValueRemovalUpdate(Tree removedNode) {
        boolean matched = false;
        for (Iterator<Tree> iterator = addedDefaultValueNodes.iterator(); iterator.hasNext(); ) {
            Tree addedDefaultValueNode = iterator.next();
            String addedNodeFunctionName = updatedNodeFunctionName(addedDefaultValueNode);
            String addedNodeParameterName = updatedNodeParameterName(addedDefaultValueNode);
            String addedDefaultValueString = addedDefaultValueNode.getType().toString().equals("atom") ?
                    addedDefaultValueNode.toTreeString() : addedDefaultValueNode.getLabel();
            String removedNodeFunctionName = updatedNodeFunctionName(removedNode);
            String removedNodeParameterName = updatedNodeParameterName(removedNode);
            String removedDefaultValueString = removedNode.getType().toString().equals("atom") ?
                    removedNode.toTreeString() : removedNode.getLabel();
            if ((addedDefaultValueNode.getPos() == removedNode.getPos() &&
                    addedDefaultValueNode.getEndPos() == removedNode.getEndPos()) ||
                    (addedNodeFunctionName.equals(removedNodeFunctionName) &&
                            addedNodeParameterName.equals(removedNodeParameterName))) {
                // Identical position / method and parameter name
                /* Parameter default value update */
                ParameterDefaultValueChange parameterDefaultValueUpdate =
                        new ParameterDefaultValueChange(addedNodeFunctionName, addedNodeParameterName,
                                removedDefaultValueString, addedDefaultValueString,
                                ParameterDefaultValueChange.Type.PARAMETER_DEFAULT_VALUE_UPDATE);
                parameterDefaultValueChangeRecords.add(parameterDefaultValueUpdate);
                iterator.remove();
                matched = true;
            }
        }
        if (!matched) {
            /* Parameter default value removal */
            removedDefaultValueNodes.add(removedNode);
        }
    }

    /**
     * Check if the method of the default value has been renamed, and return the latest method name
     *
     * @param node the default value Tree node to be checked
     * @return the latest method name
     */
    private static String updatedNodeFunctionName(Tree node) {
        String updatedFunctionName;
        switch (node.getParent().getType().toString()) {
            case "parameters":  // param node
                updatedFunctionName = node.getParents().get(1).getChild(0).getLabel();
                break;
            case "param":  // default value node
                updatedFunctionName = node.getParents().get(2).getChild(0).getLabel();
                break;
            default:
                System.err.println("Unidentified change action");
                return null;
        }
        for (FunctionRenaming functionRenaming : functionRenamingRecords) {
            if (functionRenaming.getOldFunctionName().equals(updatedFunctionName)) {
                updatedFunctionName = functionRenaming.getNewFunctionName();
            }
        }
        return updatedFunctionName;
    }

    /**
     * Check if the parameter of the default value has been renamed, and return the latest parameter name
     *
     * @param node the default value Tree node to be checked
     * @return the latest parameter name
     */
    private static String updatedNodeParameterName(Tree node) {
        String updatedParameterName = node.getParent().getChild(0).getLabel();
        for (ParameterChange parameterChange : parameterChangeRecords) {
            if (parameterChange.getType().equals(ParameterChange.Type.PARAMETER_NORMAL_UPDATE) &&
                    parameterChange.getOldParameterName().equals(updatedParameterName)) {
                updatedParameterName = parameterChange.getNewParameterName();
            }
        }
        return updatedParameterName;
    }

    /**
     * Add all unmatched added/removed parameter default value to records
     *
     * @param nodes      unmatched added/removed node
     * @param isAddition true if the node is added, false otherwise
     */
    private static void collectParameterDefaultValueAdditionRemoval(ArrayList<Tree> nodes, boolean isAddition) {
        for (Tree node : nodes) {
            String functionName = node.getParents().get(2).getChild(0).getLabel();
            String parameterName = node.getParent().getChild(0).getLabel();
            String defaultValueString = node.getType().toString().equals("atom") ?
                    node.toTreeString() : node.getLabel();
            ParameterDefaultValueChange parameterDefaultValueChange = isAddition ?
                    new ParameterDefaultValueChange(functionName, parameterName,
                            "", defaultValueString,
                            ParameterDefaultValueChange.Type.PARAMETER_DEFAULT_VALUE_ADDITION) :
                    new ParameterDefaultValueChange(functionName, parameterName,
                            defaultValueString, "",
                            ParameterDefaultValueChange.Type.PARAMETER_DEFAULT_VALUE_REMOVAL);
            parameterDefaultValueChangeRecords.add(parameterDefaultValueChange);
        }
    }

    /**
     * Check for {@link ReturnTypeChange} in editScript
     */
    public static void checkReturnTypeChange() {
        for (Action action : editScript.asList()) {
            Tree node = action.getNode();
            if (node.getType().toString().equals("name") &&
                    node.getParent().getType().toString().equals("funcdef") &&
                    node.positionInParent() == 3) {
                // 4th child of funcdef node, return type node
                String functionName = node.getParent().getChild(0).getLabel();
                if (action instanceof Insert) {
                    /* Return type addition */
                    ReturnTypeChange returnTypeAddition =
                            new ReturnTypeChange(functionName, "", node.getLabel(),
                                    ReturnTypeChange.Type.RETURN_TYPE_ADDITION);
                    returnTypeChangeRecords.add(returnTypeAddition);
                } else if (action instanceof Delete) {
                    /* Return type removal */
                    ReturnTypeChange returnTypeRemoval =
                            new ReturnTypeChange(functionName, node.getLabel(), "",
                                    ReturnTypeChange.Type.RETURN_TYPE_REMOVAL);
                    returnTypeChangeRecords.add(returnTypeRemoval);
                } else if (action instanceof Update) {
                    /* Return type update */
                    String oldReturnTypeString = node.getLabel();
                    String newReturnTypeString = ((Update) action).getValue();
                    ReturnTypeChange returnTypeUpdate =
                            new ReturnTypeChange(functionName, oldReturnTypeString, newReturnTypeString,
                                    ReturnTypeChange.Type.RETURN_TYPE_UPDATE);
                    returnTypeChangeRecords.add(returnTypeUpdate);
                }
            }
        }
    }

    public static EditScript getEditScript() {
        return editScript;
    }

    public static ArrayList<FunctionRenaming> getMethodRenamingRecords() {
        return functionRenamingRecords;
    }

    public static ArrayList<FunctionRelocation> getMethodRelocationRecords() {
        return functionRelocationRecords;
    }

    public static ArrayList<ParameterChange> getParameterChangeRecords() {
        return parameterChangeRecords;
    }

    public static ArrayList<ParameterDefaultValueChange> getParameterDefaultValueChangeRecords() {
        return parameterDefaultValueChangeRecords;
    }

    public static ArrayList<ReturnTypeChange> getReturnTypeChangeRecords() {
        return returnTypeChangeRecords;
    }
}
