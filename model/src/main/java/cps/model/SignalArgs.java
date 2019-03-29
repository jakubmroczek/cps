package cps.model;

import lombok.*;

import java.io.Serializable;
import java.time.Duration;

@Getter
@Setter
@Builder
public class SignalArgs implements Serializable {

    private String signalName;
    private double amplitude;
    private Duration period;
    private Duration initialTime;
    private double kw;

    //Przesuniecie probki dla skoku jednostokowego
    private int Ns;
    //TODO: Moze zunifikowac z period?
    private Duration samplingFrequency;
    private double probability;

    private double averageValue;
    private double averageAbsoulteValue;
    private double averagePowerValue;
    private double varianceValue;
    private double effectivePowerValue;
}
