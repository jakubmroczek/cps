package cps.filtering;

import lombok.Builder;
import sun.misc.Signal;

import java.util.List;

@Builder
public class FRFilter {

    private int M;
    private double frequency;
    private FilterFunction filterFunction;
    private WindowFunction windowFunction;

    public List<Signal> filter() {
        return null;
    }

}
