package cps.simulation;

import cps.model.FunctionFactory;
import cps.model.SignalArgs;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;

import cps.model.Signal;
import javafx.scene.control.TextField;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static java.lang.Math.min;
import static org.apache.commons.lang.time.DateUtils.MILLIS_IN_SECOND;

public class DistanceSimulation {

    @FXML
    private LineChart<Number, Number> transmittedSignalChart, receivedSignalChart;

    @FXML
    private TextField timeUnitTextField;

    private Duration getTimeUnit() {
        int MILLIS_TO_SECONDS = 1000;
        var text = timeUnitTextField.getText();
        //TODO: Exception handling
        return Duration.ofMillis((long)(Double.valueOf(text) * MILLIS_TO_SECONDS));
    }

    private void plot(Signal signal, LineChart<Number, Number> chart) {
        XYChart.Series series = new XYChart.Series();

        final double NUMBER_OF_PIXELS_IN_CHART = chart.getXAxis().getWidth();

        double singlePointDurationInSeconds = signal.getDurationInNs().toNanos() / 1_000_000_000D;
        if (signal.getSamples().size() != 1) {
            singlePointDurationInSeconds /= min(NUMBER_OF_PIXELS_IN_CHART, signal.getSamples().size() - 1);
        }

        double step = 1.0;
        if (signal.getSamples().size() > NUMBER_OF_PIXELS_IN_CHART)
            step = signal.getSamples().size() / NUMBER_OF_PIXELS_IN_CHART;

        double current = 0.0;
        for (int j = 0; current < signal.getSamples().size(); current += step, j++) {
            double y = signal.getSamples().get((int) current);
            series.getData().add(new XYChart.Data(singlePointDurationInSeconds * j, y));
        }

        chart.getData().add(series);
    }

    private void startTransmittingSignal(Function<Double, Double> function) {
//        Duration timeUnit = getTimeUnit();
//        Task task = null;
//        transmittedSignalChart.dataProperty().bind(task.valueProperty());
//        Thread t = new Thread(() -> {
//            function.apply()
//        });
//        t.start();
    }

    private void stopTransmittingSignal() {
    }

    @FXML
    public  void start() {
        var args = SignalArgs.builder().amplitude(1).initialTimeInNs(0).periodInNs(500_000_000).initialTimeInNs(0).build();
        var sineFunction = FunctionFactory.createFunction(FunctionFactory.SINUSOIDAL, args);

        Signal signal = Signal.createContinousSignal(sineFunction, Duration.ofMillis(1000), Duration.ofMillis(1));

        plot(signal, transmittedSignalChart);
        plot(signal, receivedSignalChart);
    }
}
