package cps.simulation;

import cps.model.FunctionFactory;
import cps.model.SignalArgs;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;

import cps.model.Signal;
import javafx.scene.control.TextField;

import java.time.Duration;
import java.util.function.Function;

import static java.lang.Math.min;

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

    private void startTransmittingSignal(Function<Duration, Double> function) {
        //Adding empty data
        transmittedSignalChart.getData().add(new XYChart.Series<>());

        Duration timeUnit = getTimeUnit();
        Transmitter transmitter = new Transmitter();
        transmitter.setTransmmisionPeriod(timeUnit);
        transmitter.setFunction(function);
        transmitter.setTransmmisionPeriod(timeUnit);
        transmitter.setCallback(this::updateChart);

        //Nie jest to zapewne sposob najbardziej optymalny

        Thread t = new Thread(transmitter);
        t.start();
    }

    private void stopTransmittingSignal() {
    }

    //TODO: Sprawdzic czy nie ma buga z czasem
    private Void updateChart(Duration duration, Double value) {
        Platform.runLater(() -> {
            XYChart.Data chunk = new XYChart.Data(duration.toMillis(), value);
            transmittedSignalChart.getData().get(0).getData().add(chunk);
        });
        return null;
    }

    @FXML
    public  void start() {
        loadCSS();

        var args = SignalArgs.builder().amplitude(1).initialTimeInNs(0).periodInNs(500_000_000).initialTimeInNs(0).build();
        var sineFunction = FunctionFactory.createFunction(FunctionFactory.SINUSOIDAL, args);

        Function<Duration, Double> wrapper = (Duration x) -> sineFunction.apply((double) x.toNanos());

        startTransmittingSignal(wrapper);
    }

    //Cannot be done in initialize method, cause chart's do not have associated scene
    //Where it can be moved?
    private void loadCSS() {
        final String style = "/styles/DistanceSimulation.css";

        var scene = transmittedSignalChart.getScene();
        scene.getStylesheets().add(style);
    }

    @FXML
    public void initialize() {
        transmittedSignalChart.setAnimated(false);
        transmittedSignalChart.setLegendVisible(false);

        receivedSignalChart.setAnimated(false);
        receivedSignalChart.setLegendVisible(false);
    }
}
