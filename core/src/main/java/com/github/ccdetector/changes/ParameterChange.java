package com.github.ccdetector.changes;

public class ParameterChange extends CompoundChangeRecord {

    private String targetFunctionName;
    private String oldParameterName;
    private String newParameterName;

    public enum Type {
        PARAMETER_INSERTION,
        PARAMETER_REMOVAL,
        PARAMETER_NORMAL_UPDATE,
        CLS_SELF_SWITCH,
    }

    private Type type;

    public ParameterChange(String targetFunctionName, String oldParameterName, String newParameterName,
                           Type type) {
        super();
        this.targetFunctionName = targetFunctionName;
        this.oldParameterName = oldParameterName;
        this.newParameterName = newParameterName;
        this.type = type;
    }

    public String getName() {
        switch (this.type) {
            case PARAMETER_INSERTION:
                return "Parameter insertion";
            case PARAMETER_REMOVAL:
                return "Parameter removal";
            case PARAMETER_NORMAL_UPDATE:
                return "Parameter normal update";
            case CLS_SELF_SWITCH:
                return "cls/self switch";
            default:
                return "Invalid parameter change";
        }
    }

    public String getTargetFunctionName() {
        return targetFunctionName;
    }

    public String getOldParameterName() {
        return oldParameterName;
    }

    public String getNewParameterName() {
        return newParameterName;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        String description;
        switch (type) {
            case PARAMETER_INSERTION:
                description = String.format("insert parameter: %s", newParameterName);
                break;
            case PARAMETER_REMOVAL:
                description = String.format("remove parameter: %s", oldParameterName);
                break;
            case PARAMETER_NORMAL_UPDATE:
                description = String.format("old parameter name: %s\nnew parameter name: %s",
                        oldParameterName, newParameterName);
                break;
            case CLS_SELF_SWITCH:
                if (oldParameterName.equals("cls") && newParameterName.equals("self")) {
                    description = "switch from cls to self";
                } else if (oldParameterName.equals("self") && newParameterName.equals("cls")) {
                    description = "switch from self to cls";
                } else {
                    description = "Invalid parameter change";
                }
                break;
            default:
                description = "Invalid parameter change";
        }
        return String.format("===\n%s\n---\ntarget function: %s\n%s\n",
                getName(), targetFunctionName, description);
    }
}
