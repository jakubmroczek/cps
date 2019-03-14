package cps.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static java.lang.Math.*;

@Getter
@AllArgsConstructor
public class SignalChart {
    private Duration duration;
    private Duration probingPeriod;
    private List<Double> probes;
}
