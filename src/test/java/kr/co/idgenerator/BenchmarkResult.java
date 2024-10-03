package kr.co.idgenerator;

public class BenchmarkResult {
    private String generatorName;
    private int sampleSize;
    private int byteSize;
    private long generationTime;
    private boolean sortable;
    private double collisionRate;
    private long dbJoinTime;
    private String exampleId;

    BenchmarkResult(String generatorName, int sampleSize) {
        this.generatorName = generatorName;
        this.sampleSize = sampleSize;
    }

    public String getGeneratorName() {
        return generatorName;
    }

    public int getSampleSize() {
        return sampleSize;
    }

    public int getByteSize() {
        return byteSize;
    }

    public void setByteSize(int byteSize) {
        this.byteSize = byteSize;
    }


    public long getGenerationTime() {
        return generationTime;
    }

    public void setGenerationTime(long generationTime) {
        this.generationTime = generationTime;
    }

    public boolean isSortable() {
        return sortable;
    }

    public void setSortable(boolean sortable) {
        this.sortable = sortable;
    }

    public double getCollisionRate() {
        return collisionRate;
    }

    public void setCollisionRate(double collisionRate) {
        this.collisionRate = collisionRate;
    }

    public long getDbJoinTime() {
        return dbJoinTime;
    }

    public void setDbJoinTime(long dbJoinTime) {
        this.dbJoinTime = dbJoinTime;
    }

    public String getExampleId() {
        return exampleId;
    }

    public void setExampleId(String exampleId) {
        this.exampleId = exampleId;
    }
}
