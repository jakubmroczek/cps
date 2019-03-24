package cps.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.time.Duration;

@Getter
@AllArgsConstructor
public class SignalArgs {

    //kw is not initialized cause it may be not needed.
    public SignalArgs(double amplitude, Duration period, Duration initialTime) {
        this.amplitude = amplitude;
        this.period = period;
        this.initialTime = initialTime;
    }

    public SignalArgs(double amplitude, Duration period, Duration initialTime, double kw) {
        this.amplitude = amplitude;
        this.period = period;
        this.initialTime = initialTime;
        this.kw = kw;
    }

    private double amplitude;
    private Duration period;
    private Duration initialTime;
    private double kw;

    //Przesuniecie probki dla skoku jednostokowego
    private int Ns;
    //TODO: Moze zunifikowac z period?
    private Duration samplingFrequency;
}
