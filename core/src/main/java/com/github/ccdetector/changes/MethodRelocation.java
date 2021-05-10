package com.github.ccdetector.changes;

public class MethodRelocation extends CompoundChangeRecord {

    private String methodName;
    private int oldLocation;
    private int newLocation;

    public MethodRelocation(String methodName, int oldLocation, int newLocation) {
        super();
        this.methodName = methodName;
        this.oldLocation = oldLocation + 1;
        this.newLocation = newLocation + 1;
    }

    public String getName() {
        return "Method relocation";
    }

    public String getMethodName() {
        return methodName;
    }

    public int getOldLocation() {
        return oldLocation;
    }

    public int getNewLocation() {
        return newLocation;
    }

    @Override
    public String toString() {
        return String.format("===\n%s\n---\nmethod name: %s\nold location: method No.%d\nnew location: method No.%d\n",
                getName(), methodName, oldLocation, newLocation);
    }
}
