package cps.filtering;


import cps.model.*;

import java.util.ArrayList;
import java.util.List;

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

    public static Signal correleate(final Signal lhs, final Signal rhs)  {
        return null;
    }
}
