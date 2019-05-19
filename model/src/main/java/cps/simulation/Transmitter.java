package cps.simulation;

import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Transmitter implements Runnable {

    @Setter
    private Function<Duration, Double> function;

    @Setter
    private BiFunction<Duration, Double, Void> callback;

    @Getter
    @Setter
    private Duration transmmisionPeriod;

    private Duration time = Duration.ZERO;

    @Override
    public void run() {
        while (true) {
            var emittedValue = function.apply(time);
            callback.apply(time, emittedValue);

            //TODO: Zainicjalizowacc raz
            long millis = transmmisionPeriod.toMillis();
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            time = time.plus(transmmisionPeriod);
        }
    }
}
