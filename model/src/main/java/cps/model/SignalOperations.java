package cps.model;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Math.min;

public class SignalOperations {

    private static void validate(SignalChart lhs, SignalChart rhs) {
        //TODO: Change to exception
        assert lhs.getProbingPeriod().equals(rhs.getProbingPeriod());
        assert lhs.getDuration().equals(rhs.getDuration());
        assert lhs.getProbes().size() == rhs.getProbes().size();
        assert lhs.getSignalType().equals(rhs.getSignalType());
    }

    public static SignalChart add(SignalChart lhs, SignalChart rhs) {
        validate(lhs, rhs);

        int size = lhs.getProbes().size();
        List<Double> resultSamples = IntStream.range(0, size).mapToObj(index -> lhs.getProbes().get(index) + rhs.getProbes().get(index)).collect(Collectors.toList());

        return new SignalChart(lhs.getDuration(), lhs.getProbingPeriod(), resultSamples);
    }
    
    public static SignalChart subtract(SignalChart lhs, SignalChart rhs){
        validate(lhs, rhs);

        int size = lhs.getProbes().size();
        List<Double> resultSamples = IntStream.range(0, size).mapToObj(index -> lhs.getProbes().get(index) - rhs.getProbes().get(index)).collect(Collectors.toList());

        return new SignalChart(lhs.getDuration(), lhs.getProbingPeriod(), resultSamples);
    }
    
    public static SignalChart multiply(SignalChart lhs, SignalChart rhs) {
        validate(lhs, rhs);

        int size = lhs.getProbes().size();
        List<Double> resultSamples = IntStream.range(0, size).mapToObj(index -> lhs.getProbes().get(index) * rhs.getProbes().get(index)).collect(Collectors.toList());

        return new SignalChart(lhs.getDuration(), lhs.getProbingPeriod(), resultSamples);
    }
    
    public static SignalChart divide(SignalChart lhs, SignalChart rhs) {
        throw new UnsupportedOperationException("not implemented yet.");
    }
    
}
