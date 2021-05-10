public abstract class CompoundChangeRecord {

    public String framework;
    public String module;
    public String oldReleaseVersion;
    public String newReleaseVersion;

    public CompoundChangeRecord() {
        this.framework = "demo framework";
        this.module = "demo module";
        this.oldReleaseVersion = "0.0.0";
        this.newReleaseVersion = "1.0.0";
    }

    public CompoundChangeRecord(String framework,
                                String module,
                                String oldReleaseVersion,
                                String newReleaseVersion) {
        this.framework = framework;
        this.module = module;
        this.oldReleaseVersion = oldReleaseVersion;
        this.newReleaseVersion = newReleaseVersion;
    }
}
