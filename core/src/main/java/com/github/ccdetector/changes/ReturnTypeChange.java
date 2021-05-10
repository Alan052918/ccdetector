package com.github.ccdetector.changes;

public class ReturnTypeChange extends CompoundChangeRecord {

    private String targetMethodName;
    private String oldReturnTypeString;
    private String newReturnTypeString;

    public enum Type {
        RETURN_TYPE_ADDITION,
        RETURN_TYPE_REMOVAL,
        RETURN_TYPE_UPDATE,
    }

    private Type type;

    public ReturnTypeChange(String targetMethodName, String oldReturnTypeString, String newReturnTypeString,
                            Type type) {
        super();
        this.targetMethodName = targetMethodName;
        this.oldReturnTypeString = oldReturnTypeString;
        this.newReturnTypeString = newReturnTypeString;
        this.type = type;
    }

    public String getName() {
        switch (type) {
            case RETURN_TYPE_ADDITION:
                return "Return type addition";
            case RETURN_TYPE_REMOVAL:
                return "Return type removal";
            case RETURN_TYPE_UPDATE:
                return "Return type update";
            default:
                return "Invalid return type change";
        }
    }

    public String getTargetMethodName() {
        return targetMethodName;
    }

    public String getOldReturnTypeString() {
        return oldReturnTypeString;
    }

    public String getNewReturnTypeString() {
        return newReturnTypeString;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        String description;
        switch (type) {
            case RETURN_TYPE_ADDITION:
                description = String.format("add return type hint: %s", newReturnTypeString);
                break;
            case RETURN_TYPE_REMOVAL:
                description = "remove return type hint";
                break;
            case RETURN_TYPE_UPDATE:
                description = String.format("old return type hint: %s\nnew return type hint: %s",
                        oldReturnTypeString, newReturnTypeString);
                break;
            default:
                description = "Invalid return type change";
        }
        return String.format("===\n%s\n---\ntarget method: %s\n%s\n", getName(), targetMethodName, description);
    }
}
