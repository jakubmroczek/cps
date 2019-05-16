package cps.filtering;


import cps.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Filters {

    //TODO: More expressive name
    //TODO: Throw an exception
    private static void check(final Signal lhs, final Signal  rhs) throws IllegalArgumentException {
        assert lhs.getSamplingPeriod().equals(rhs.getSamplingPeriod());
        assert lhs.getType().equals(rhs.getType());
    }


    public static Signal convolute(final Signal lhs, final Signal rhs) {
        check(lhs, rhs);

        List<Double> results = new ArrayList<>();

        //TODO: What if both equal to 0?
        int size = lhs.getSamples().size() + rhs.getSamples().size() - 1;

        for (int i = 0; i < size; i++) {
            results.add(0.0);
            for (int k = 0; k < lhs.getSamples().size(); k++) {
                if (i - k >= 0 && i - k < rhs.getSamples().size()) {
                    var update = results.get(i) + lhs.getSamples().get(k) * rhs.getSamples().get(i - k);
                    results.set(i, update);
                }
            }
        }

       //TODO: Is the resulting duration correct?
       var duration = lhs.getSamplingPeriod().multipliedBy(results.size());
        return new Signal(lhs.getType(), duration, lhs.getSamplingPeriod(), results);
    }

    public static Signal correlate(final Signal lhs, final Signal rhs)  {
       check(lhs, rhs);

       List<Double> results = new ArrayList<>();
       //TODO: What if both are equal to 0?
       var size = lhs.getSamples().size() + rhs.getSamples().size() - 1;

        for (int i = 0; i < size; i++) {
            results.add(0.0);
            for (int k = 0; k < lhs.getSamples().size(); k++) {
                if ((i + k) < rhs.getSamples().size()) {
                    var update = results.get(i) + lhs.getSamples().get(k) * rhs.getSamples().get(i  - k);
                    results.set(i, update);
                }
            }
        }

        //TODO: Is the resulting duration correct?
        var duration = lhs.getSamplingPeriod().multipliedBy(results.size());
        return new Signal(lhs.getType(), duration, lhs.getSamplingPeriod(), results);
    }

    public static Signal correlationUsingConvolution(final Signal lhs, final Signal rhs) {
        return reverse(convolute(lhs, reverse(rhs)));
    }

    public static Signal reverse(final Signal signal) {
        var results =  IntStream.range(0, signal.getSamples().size())
                .map(i -> (signal.getSamples().size() - 1 - i))
                .mapToObj(signal.getSamples()::get)                // Stream<T>
                .collect(Collectors.toList());

        return new Signal(signal.getType(), signal.getDurationInNs(), signal.getSamplingPeriod(), results);
    }
}
