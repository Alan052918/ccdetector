package com.github.ccdetector.changes;

public class FunctionRelocation extends CompoundChangeRecord {

    private String functionName;
    private int oldLocation;
    private int newLocation;

    public enum Type {
        METHOD_INSERTION,
        METHOD_REMOVAL,
    }

    public FunctionRelocation(String functionName, int oldLocation, int newLocation) {
        super();
        this.functionName = functionName;
        this.oldLocation = oldLocation + 1;
        this.newLocation = newLocation + 1;
    }

    public String getName() {
        return "Function relocation";
    }

    public String getFunctionName() {
        return functionName;
    }

    public int getOldLocation() {
        return oldLocation;
    }

    public int getNewLocation() {
        return newLocation;
    }

    @Override
    public String toString() {
        return String.format("===\n%s\n---\nfunction name: %s\nold location: method No.%d\nnew location: method No.%d\n",
                getName(), functionName, oldLocation, newLocation);
    }
}
