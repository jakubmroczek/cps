package cps.model;


import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.function.Function;

//TODO: Zrobic interfejs funkcyjny
public class Signal  {

    double amplitude;
    double period;
    double initialTime;
    double duration;
    String signalType;
    double kw;

    public void setDuration(double duration) { this.duration = duration; }
    public double getAmplitude() { return amplitude; }
    public double getPeriod() { return period; }
    public double getInitialTime() { return initialTime; }
    public double getDuration() { return duration; }
    public String getSignalType() { return signalType; }
    public void setSignalType(String signalType) { this.signalType = signalType; }
    public transient List<Double> signalSamples = new ArrayList<>();
    protected TreeMap<Double, Double> signalValues = new TreeMap<>();
    public TreeMap<Double, Double> getSignalValues(){ return signalValues; }
    public void setSignalValues(TreeMap<Double, Double> map) { signalValues = map; }

    public Signal(Function<Duration, Double> function) {
        this.function = function;
    }

    public void setArgs(double amplitude, double period, double initialTime){
        this.amplitude = amplitude;
        this.period = period;
        this.initialTime = initialTime;

    }
    //TODO: Adnotacje notnull
    public double calculate(Duration duration) {
        return function.apply(duration);
    }

    //TODO: To chyba nie jed odpowidzialnosc sygnalu, przeniesc gdzies indziej
    public SignalChart createChart(Duration duration, Duration probingPeriod) {
        Duration time = Duration.ofNanos(0);
        List<Double> samples = new ArrayList<>();

        int i=0;
        while (time.compareTo(duration) <= 0) {
            double val = calculate(time);
            samples.add(val);
            signalSamples.add(val);
            time = time.plus(probingPeriod);

            double x = i++ * probingPeriod.toMillis();
            double y = val;
            signalValues.put(x,y);
        }
        return new SignalChart(duration, probingPeriod, samples);
    }
    @JsonIgnore
    private transient  Function<Duration, Double> function;
}
