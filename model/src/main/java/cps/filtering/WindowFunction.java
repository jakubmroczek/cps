package cps.filtering;

@FunctionalInterface
public interface WindowFunction {

    /**
     *
     * @param n numer próbki
     * @param M rząd
     * @return
     */
    public double apply(int n, int M);

}
