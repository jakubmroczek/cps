package cps.simulation;

import lombok.Getter;

import java.time.Duration;

public class TrackedObject {

    @Getter
    private int constantSpeedInMetersPerSecond;

    private int distanceInMetersFromObserver;

    public TrackedObject(int constantSpeedInMetersPerSecond, int initialDistanceInMetersFromObserver) {
        this.constantSpeedInMetersPerSecond = constantSpeedInMetersPerSecond;
        distanceInMetersFromObserver = initialDistanceInMetersFromObserver;
    }

    public long getDistanceSinceStart(Duration elapsedTimeSinceStart) {
        assert !elapsedTimeSinceStart.isNegative();

        return distanceInMetersFromObserver + constantSpeedInMetersPerSecond * elapsedTimeSinceStart.toSeconds();
    }

}
