package com.github.ccdetector.changes;

public class MethodRenaming extends CompoundChangeRecord {

    private String oldMethodName;
    private String newMethodName;

    /**
     * Method accessibility after change, based on {@link MethodRenaming#newMethodName}
     */
    private Boolean weaklyPrivate;

    public enum Type {
        PRIVATE_TO_PUBLIC,
        PUBLIC_TO_PRIVATE,
        NO_SWITCH,
    }

    private Type type;

    public MethodRenaming(String oldMethodName, String newMethodName, Type type) {
        super();
        this.oldMethodName = oldMethodName;
        this.newMethodName = newMethodName;
        if (newMethodName.startsWith("_")) {
            this.weaklyPrivate = true;
        } else {
            this.weaklyPrivate = false;
        }
        this.type = type;
    }

    public String getName() {
        return "Method Renaming";
    }

    public String getOldMethodName() {
        return oldMethodName;
    }

    public String getNewMethodName() {
        return newMethodName;
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
        return String.format("===\n%s\n---\nold method name: %s\nnew method name: %s\n%s\n",
                getName(), oldMethodName, newMethodName, accessibilityNotice);
    }
}
