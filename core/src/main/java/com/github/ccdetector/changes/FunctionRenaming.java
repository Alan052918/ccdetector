package com.github.ccdetector.changes;

public class FunctionRenaming extends CompoundChangeRecord {

    private String oldFunctionName;
    private String newFunctionName;

    /**
     * Method accessibility after change, based on {@link FunctionRenaming#newFunctionName}
     */
    private Boolean weaklyPrivate;

    public enum Type {
        PRIVATE_TO_PUBLIC,
        PUBLIC_TO_PRIVATE,
        NO_SWITCH,
    }

    private Type type;

    public FunctionRenaming(String oldFunctionName, String newFunctionName, Type type) {
        super();
        this.oldFunctionName = oldFunctionName;
        this.newFunctionName = newFunctionName;
        if (newFunctionName.startsWith("_")) {
            this.weaklyPrivate = true;
        } else {
            this.weaklyPrivate = false;
        }
        this.type = type;
    }

    public String getName() {
        return "Function Renaming";
    }

    public String getOldFunctionName() {
        return oldFunctionName;
    }

    public String getNewFunctionName() {
        return newFunctionName;
    }

    public Boolean isWeaklyPrivate() {
        return weaklyPrivate;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        String accessibilityNotice;
        switch (type) {
            case PRIVATE_TO_PUBLIC:
                accessibilityNotice = "switched from private method to public method";
                break;
            case PUBLIC_TO_PRIVATE:
                accessibilityNotice = "switched from public method to private method";
                break;
            case NO_SWITCH:
                if (weaklyPrivate) {
                    accessibilityNotice = String.format("no accessibility switch (weakly private)");
                } else {
                    accessibilityNotice = String.format("no accessibility switch (public)");
                }
                break;
            default:
                accessibilityNotice = "Invalid accessibility switch";
        }
        return String.format("===\n%s\n---\nold function name: %s\nnew function name: %s\n%s\n",
                getName(), oldFunctionName, newFunctionName, accessibilityNotice);
    }
}
