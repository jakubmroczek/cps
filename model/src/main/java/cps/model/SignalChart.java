package cps.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static java.lang.Math.*;

@Getter
public class SignalChart {
    //Parameters describing signal
    //TODO:
    @Setter
    private SignalArgs args;
    @Setter
    private Signal.Type signalType;

    private Duration duration;
    private Duration probingPeriod;
    private List<Double> probes;

    public SignalChart(Duration duration, Duration probingPeriod, List<Double> probes) {
        this.duration = duration;
        this.probingPeriod = probingPeriod;
        this.probes = probes;
    }
}
