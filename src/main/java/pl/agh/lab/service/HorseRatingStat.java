package pl.agh.lab.service;

public class HorseRatingStat {

    private final String horseName;
    private final long count;
    private final double average;

    public HorseRatingStat(String horseName, long count, double average) {
        this.horseName = horseName;
        this.count = count;
        this.average = average;
    }

    public String getHorseName() {
        return horseName;
    }

    public long getCount() {
        return count;
    }

    public double getAverage() {
        return average;
    }
}
