package org.example;

public final class TravelCalculator {
    private TravelCalculator() { }
    public static double speed(double distanceKm, double durationHours) {
        if (!Double.isFinite(distanceKm) || !Double.isFinite(durationHours)
                || distanceKm < 0 || durationHours <= 0)
            throw new IllegalArgumentException("Distance must be nonnegative and time positive");
        double speed = distanceKm / durationHours;
        if (!Double.isFinite(speed)) throw new IllegalArgumentException("Speed overflow");
        return speed;
    }
}
