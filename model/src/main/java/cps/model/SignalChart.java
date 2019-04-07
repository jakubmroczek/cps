package cps.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.Duration;
import java.util.List;

@Getter public class SignalChart implements Serializable {

    //Parameters describing signal
    //TODO:
    @Setter private SignalArgs args;

    @Setter private Signal.Type signalType;

    private Duration duration;

    @Setter private Duration probingPeriod;

    @Setter private List<Double> probes;

    public SignalChart(Duration duration, Duration probingPeriod, List<Double> probes) {
        this.duration = duration;
        this.probingPeriod = probingPeriod;
        this.probes = probes;
    }
}
