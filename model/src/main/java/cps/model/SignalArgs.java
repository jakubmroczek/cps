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

    private double amplitude;
    private Duration period;
    private Duration initialTime;
    private double kw;
}
