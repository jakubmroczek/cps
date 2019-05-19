package cps.simulation;

import cps.model.FunctionFactory;
import cps.model.SignalArgs;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;

import cps.model.Signal;
import javafx.scene.control.Button;

import java.time.Duration;

import static java.lang.Math.min;

public class DistanceSimulation {

    @FXML
    private LineChart<Number, Number> transmittedSignalChart, receivedSignalChart;

    @FXML
    private Button startSimulationButton;

    @FXML
    public  void start() {
        var args = SignalArgs.builder().amplitude(1).initialTimeInNs(0).periodInNs(500_000_000).initialTimeInNs(0).build();
        var sineFunction = FunctionFactory.createFunction(FunctionFactory.SINUSOIDAL, args);

        Signal signal = Signal.createContinousSignal(sineFunction, Duration.ofMillis(1000), Duration.ofMillis(1));

        plot(signal, transmittedSignalChart);
        plot(signal, receivedSignalChart);
    }

    //TODO: It is a common feature
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
}
