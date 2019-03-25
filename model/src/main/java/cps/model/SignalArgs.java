package cps.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.io.Serializable;
import java.time.Duration;

@Getter
@Builder
public class SignalArgs implements Serializable {
    private double amplitude;
    private Duration period;
    private Duration initialTime;
    private double kw;

    //Przesuniecie probki dla skoku jednostokowego
    private int Ns;
    //TODO: Moze zunifikowac z period?
    private Duration samplingFrequency;
    private double probability;
}
