package cps.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.function.Function;

@Data
@AllArgsConstructor
public class SignalMeasurement {
    
    private double average;
    private double absoluteAverage;
    private double averagePower;
    private double variance;
    private double effectivePower;
    
    public SignalMeasurement() {
    }
    
    public static SignalMeasurement measure(Signal signal) {
        SignalMeasurement measurement = new SignalMeasurement();
        measurement.average = Math.averageValue(signal);
        measurement.absoluteAverage = Math.averageAbsoluteValue(signal);
        measurement.averagePower = Math.averagePower(signal);
        measurement.variance = Math.variance(signal);
        measurement.effectivePower = Math.effectivePower(signal);
        return measurement;
    }
    
    public static SignalMeasurement measure(Function<Double, Double> function, double startInNs, double endInNs) {
        SignalMeasurement measurement = new SignalMeasurement();
        measurement.average = Math.averageValue(function, startInNs, endInNs);
        measurement.absoluteAverage = Math.averageAbsoluteValue(function, startInNs,endInNs );
        measurement.averagePower = Math.averagePower(function, startInNs, endInNs);
        measurement.variance = Math.variance(function, startInNs,  endInNs  );
        measurement.effectivePower = Math.effectivePower(function, startInNs, endInNs);
        return measurement;
    }
    

}
