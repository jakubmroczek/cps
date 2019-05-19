package cps.simulation;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.util.function.Function;

public class Transmitter implements Runnable {

    @Setter
    private Function<Duration, Double> function;

    @Setter
    private Function<Double, Void> callback;

    @Getter
    @Setter
    private Duration timeUnit;

    private Duration time = Duration.ZERO;

    @Override
    public void run() {
        while (true) {
            var emittedValue = function.apply(time);
            callback.apply(emittedValue);

            //TODO: Zainicjalizowacc raz
            long millis = timeUnit.toMillis();
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            time = time.plus(timeUnit);
        }

        //CZY REKURENCJA CZY PETLA?
    }
}
