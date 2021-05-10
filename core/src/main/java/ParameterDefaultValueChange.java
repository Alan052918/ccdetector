public class ParameterDefaultValueChange extends CompoundChangeRecord {

    private String targetMethodName;
    private String targetParameterName;
    private String oldDefaultValueString;
    private String newDefaultValueString;

    public enum Type {
        PARAMETER_DEFAULT_VALUE_ADDITION,
        PARAMETER_DEFAULT_VALUE_REMOVAL,
        PARAMETER_DEFAULT_VALUE_UPDATE,
    }

    private Type type;

    public ParameterDefaultValueChange(String targetMethodName, String targetParameterName,
                                       String oldDefaultValueString, String newDefaultValueString,
                                       Type type) {
        super();
        this.targetMethodName = targetMethodName;
        this.targetParameterName = targetParameterName;
        this.oldDefaultValueString = oldDefaultValueString;
        this.newDefaultValueString = newDefaultValueString;
        this.type = type;
    }

    public String getName() {
        switch (type) {
            case PARAMETER_DEFAULT_VALUE_ADDITION:
                return "Parameter default value insertion";
            case PARAMETER_DEFAULT_VALUE_REMOVAL:
                return "Parameter default value removal";
            case PARAMETER_DEFAULT_VALUE_UPDATE:
                return "Parameter default value update";
            default:
                return "Invalid parameter default value change";
        }
    }

    public String getTargetParameterName() {
        return targetParameterName;
    }

    public String getOldDefaultValueString() {
        return oldDefaultValueString;
    }

    public String getNewDefaultValueString() {
        return newDefaultValueString;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        String description;
        switch (type) {
            case PARAMETER_DEFAULT_VALUE_ADDITION:
                description = String.format("add default value: %s", newDefaultValueString);
                break;
            case PARAMETER_DEFAULT_VALUE_REMOVAL:
                description = String.format("remove default value: %s", oldDefaultValueString);
                break;
            case PARAMETER_DEFAULT_VALUE_UPDATE:
                description = String.format("old default value: %s\nnew default value: %s",
                        oldDefaultValueString, newDefaultValueString);
                break;
            default:
                description = "Invalid parameter default value change";
        }
        return String.format("===\n%s\n---\ntarget method: %s\ntarget parameter: %s\n%s\n",
                getName(), targetMethodName, targetParameterName, description);
    }
}
