package kr.co.idgenerator;

public class BenchmarkResult {
    private String generatorName;
    private int sampleSize;
    private long generationTime;
    private boolean sortable;
    private double collisionRate;
    private long dbJoinTime;

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
}
