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

    private static ArrayList<MethodRenaming> methodRenamingRecords = new ArrayList<>();
    private static ArrayList<MethodRelocation> methodRelocationRecords = new ArrayList<>();
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
     * Compute edit script deduced that transform AST in srcFile to AST in dstFile
     *
     * @param srcFile source file name
     * @param dstFile destination file name
     */
    public static void computeFileEditScript(String srcFile, String dstFile) {
        Tree srcTree = null;
        Tree dstTree = null;

        try {
            srcTree = TreeGenerators.getInstance().getTree(srcFile).getRoot();
            dstTree = TreeGenerators.getInstance().getTree(dstFile).getRoot();
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
     * @param srcFile earlier version of source file
     * @param dstFile later version of source file
     */
    public static void checkCompoundChanges(String srcFile, String dstFile) {
        computeFileEditScript(srcFile, dstFile);
        checkMethodRenaming();
        checkMethodRelocation();
        checkParameterChanges();
        checkParameterDefaultValueChange();
        checkReturnTypeChange();
    }

    /**
     * Check for {@link MethodRenaming} in editScript
     */
    public static void checkMethodRenaming() {
        for (Action action : editScript.asList()) {
            Tree node = action.getNode();
            if (action instanceof Update &&
                    node.getType().toString().equals("name") &&
                    node.getParent().getType().toString().equals("funcdef") &&
                    node.positionInParent() == 0) {
                /* Method renaming */
                MethodRenaming methodRenaming;
                String oldApiName = node.getLabel();
                String newApiName = ((Update) action).getValue();
                if (oldApiName.startsWith("_") &&
                        !newApiName.startsWith("_")) {
                    methodRenaming = new MethodRenaming(oldApiName, newApiName,
                            MethodRenaming.Type.PRIVATE_TO_PUBLIC);
                } else if (!oldApiName.startsWith("_") &&
                        newApiName.startsWith("_")) {
                    methodRenaming = new MethodRenaming(oldApiName, newApiName,
                            MethodRenaming.Type.PUBLIC_TO_PRIVATE);
                } else {
                    methodRenaming = new MethodRenaming(oldApiName, newApiName,
                            MethodRenaming.Type.NO_SWITCH);
                }
                methodRenamingRecords.add(methodRenaming);
            }
        }
    }

    /**
     * Check for {@link MethodRelocation} in editScript
     */
    public static void checkMethodRelocation() {
        for (Action action : editScript.asList()) {
            Tree node = action.getNode();
            if (action instanceof Move &&
                    node.getType().toString().equals("funcdef")) {
                /* Same-file method relocation */
                String methodName = node.getChild(0).getLabel();
                int oldLocation = node.positionInParent();
                int newLocation = ((Move) action).getPosition();
                MethodRelocation methodRelocation = new MethodRelocation(methodName, oldLocation, newLocation);
                methodRelocationRecords.add(methodRelocation);
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
            } else if (action instanceof Update &&
                    node.getParent().getType().toString().equals("param") &&
                    node.getType().toString().equals("name")) {
                /* Parameter update */
                String methodName = node.getParents().get(2).getChild(0).getLabel();
                // Check if the method of the removed default value has been renamed
                for (MethodRenaming methodRenaming : methodRenamingRecords) {
                    if (methodRenaming.getOldMethodName().equals(methodName)) {
                        methodName = methodRenaming.getNewMethodName();
                    }
                }
                ParameterChange parameterUpdate;
                String oldParameterName = node.getLabel();
                String newParameterName = ((Update) action).getValue();
                if ((oldParameterName.equals("cls") &&
                        newParameterName.equals("self")) ||
                        (oldParameterName.equals("self") &&
                                newParameterName.equals("cls"))) {
                    /* cls/self switch */
                    parameterUpdate = new ParameterChange(methodName, oldParameterName, newParameterName,
                            ParameterChange.Type.CLS_SELF_SWITCH);
                } else {
                    /* Normal update */
                    parameterUpdate = new ParameterChange(methodName, oldParameterName, newParameterName,
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
                        node.getChildren().size() == 3 &&
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
                        node.getChildren().size() == 3 &&
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
                String methodName = updatedNodeMethodName(node);
                String parameterName = updatedNodeParameterName(node);
                String oldDefaultValueString = node.getLabel();
                String newDefaultValueString = ((Update) action).getValue();
                ParameterDefaultValueChange parameterDefaultValueUpdate =
                        new ParameterDefaultValueChange(methodName, parameterName,
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
        String methodName = node.getParents().get(1).getChild(0).getLabel();
        String parameterName = node.getChild(0).getLabel();
        /* Parameter change */
        ParameterChange parameterChange = isInsertion ?
                new ParameterChange(methodName, "", parameterName,
                        ParameterChange.Type.PARAMETER_INSERTION) :
                new ParameterChange(methodName, parameterName, "",
                        ParameterChange.Type.PARAMETER_REMOVAL);
        parameterChangeRecords.add(parameterChange);
    }

    /**
     * Check for
     * {@link ParameterDefaultValueChange.Type#PARAMETER_DEFAULT_VALUE_ADDITION} and
     * {@link ParameterDefaultValueChange.Type#PARAMETER_DEFAULT_VALUE_REMOVAL}
     * with given inserted/removed subtree
     *
     * @param node       the root of the subtree inserted/removed
     * @param isAddition true if the subtree is inserted, false otherwise
     */
    private static void checkParameterDefaultValueAdditionRemoval(Tree node, boolean isAddition) {
        String methodName = node.getParents().get(1).getChild(0).getLabel();
        String parameterName = node.getChild(0).getLabel();
        String defaultValueString = node.getChild(2).getLabel();
        /* Parameter default value change */
        ParameterDefaultValueChange parameterDefaultValueChange = isAddition ?
                new ParameterDefaultValueChange(methodName, parameterName, "", defaultValueString,
                        ParameterDefaultValueChange.Type.PARAMETER_DEFAULT_VALUE_ADDITION) :
                new ParameterDefaultValueChange(methodName, parameterName, defaultValueString, "",
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
            String addedNodeMethodName = addedDefaultValueNode.getParents().get(2).getChild(0).getLabel();
            String addedNodeParameterName = addedDefaultValueNode.getParent().getChild(0).getLabel();
            String addedDefaultValueString = addedDefaultValueNode.getType().toString().equals("atom") ?
                    addedDefaultValueNode.toTreeString() : addedDefaultValueNode.getLabel();
            String removedNodeMethodName = updatedNodeMethodName(removedNode);
            String removedNodeParameterName = updatedNodeParameterName(removedNode);
            String removedDefaultValueString = removedNode.getType().toString().equals("atom") ?
                    removedNode.toTreeString() : removedNode.getLabel();
            if ((addedDefaultValueNode.getPos() == removedNode.getPos() &&
                    addedDefaultValueNode.getEndPos() == removedNode.getEndPos()) ||
                    (addedNodeMethodName.equals(removedNodeMethodName) &&
                            addedNodeParameterName.equals(removedNodeParameterName))) {
                // Identical position / method and parameter name
                /* Parameter default value update */
                ParameterDefaultValueChange parameterDefaultValueUpdate =
                        new ParameterDefaultValueChange(addedNodeMethodName, addedNodeParameterName,
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
    private static String updatedNodeMethodName(Tree node) {
        String updatedMethodName = node.getParents().get(2).getChild(0).getLabel();
        for (MethodRenaming methodRenaming : methodRenamingRecords) {
            if (methodRenaming.getOldMethodName().equals(updatedMethodName)) {
                updatedMethodName = methodRenaming.getNewMethodName();
            }
        }
        return updatedMethodName;
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
            String methodName = node.getParents().get(2).getChild(0).getLabel();
            String parameterName = node.getParent().getChild(0).getLabel();
            String defaultValueString = node.getType().toString().equals("atom") ?
                    node.toTreeString() : node.getLabel();
            ParameterDefaultValueChange parameterDefaultValueChange = isAddition ?
                    new ParameterDefaultValueChange(methodName, parameterName,
                            "", defaultValueString,
                            ParameterDefaultValueChange.Type.PARAMETER_DEFAULT_VALUE_ADDITION) :
                    new ParameterDefaultValueChange(methodName, parameterName,
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
                String methodName = node.getParent().getChild(0).getLabel();
                if (action instanceof Insert) {
                    /* Return type addition */
                    ReturnTypeChange returnTypeAddition =
                            new ReturnTypeChange(methodName, "", node.getLabel(),
                                    ReturnTypeChange.Type.RETURN_TYPE_ADDITION);
                    returnTypeChangeRecords.add(returnTypeAddition);
                } else if (action instanceof Delete) {
                    /* Return type removal */
                    ReturnTypeChange returnTypeRemoval =
                            new ReturnTypeChange(methodName, node.getLabel(), "",
                                    ReturnTypeChange.Type.RETURN_TYPE_REMOVAL);
                    returnTypeChangeRecords.add(returnTypeRemoval);
                } else if (action instanceof Update) {
                    /* Return type update */
                    String oldReturnTypeString = node.getLabel();
                    String newReturnTypeString = ((Update) action).getValue();
                    ReturnTypeChange returnTypeUpdate =
                            new ReturnTypeChange(methodName, oldReturnTypeString, newReturnTypeString,
                                    ReturnTypeChange.Type.RETURN_TYPE_UPDATE);
                    returnTypeChangeRecords.add(returnTypeUpdate);
                }
            }
        }
    }

    public static EditScript getEditScript() {
        return editScript;
    }

    public static ArrayList<MethodRenaming> getMethodRenamingRecords() {
        return methodRenamingRecords;
    }

    public static ArrayList<MethodRelocation> getMethodRelocationRecords() {
        return methodRelocationRecords;
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
