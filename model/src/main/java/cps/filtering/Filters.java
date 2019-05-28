package cps.filtering;


import cps.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class Filters {

    //TODO: More expressive name
    private static void check(final Signal lhs, final Signal  rhs) throws IllegalArgumentException {
        assert lhs.getSamplingPeriod().equals(rhs.getSamplingPeriod());
        assert lhs.getType().equals(rhs.getType());
    }


    public static Signal convolute(final Signal lhs, final Signal rhs) {
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
        assert lhs.getSamplingPeriod().equals(rhs.getSamplingPeriod());
        final List<Double> values1 = lhs.getSamples();
        final List<Double> values2 = rhs.getSamples();

        List<Double> newValues = new ArrayList<>();
        var size = values1.size() + values2.size() - 1;
        IntStream.range(0, size).forEach(x -> newValues.add(0.0));


        for (int i = -newValues.size() / 2; i < newValues.size() / 2; i++) {
            for (int k = 0; k < values1.size(); k++) {
                if (i + k >= 0 && i + k < values2.size()) {
                    var value = newValues.get(i + newValues.size() / 2);
                    value += values1.get(k) * values2.get(i + k);
                    newValues.set(i + newValues.size() / 2, value);
                }
            }
        }

        var duration = lhs.getSamplingPeriod().multipliedBy(newValues.size());
        return new Signal(lhs.getType(), duration, lhs.getSamplingPeriod(), newValues);
    }


}
