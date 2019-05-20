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
import java.util.Timer;
import java.util.function.Function;
import java.util.stream.IntStream;

import static java.lang.Math.min;

public class DistanceSimulation {

    @FXML
    private LineChart<Number, Number> transmittedSignalChart, receivedSignalChart;

    @FXML
    private TextField timeUnitTextField;

    private Timer timer = new Timer();
    private Transmitter transmitter;

    private Duration timeUnit;

    private XYChart.Series<Number, Number> shiftSeries(double value) {
//        var oldSeries = transmittedSignalChart.getData().get(0);
//        XYChart.Series<Number, Number> newSeries = new XYChart.Series<>();
//        for (int i = 1; i < oldSeries.getData().size(); i++) {
//            newSeries.getData().add(oldSeries.getData().get(i));
//        }
//        var timePoint = (oldSeries.getData().size() -1) *1000;
//        newSeries.getData().add(new XYChart.Data<>(timePoint, value));
//        return newSeries;
        XYChart.Series<Number, Number> series2 = new XYChart.Series<>();
        final int NUMBER_OF_PIXELS_IN_CHART2 = (int) receivedSignalChart.getXAxis().getWidth();
        IntStream.range(0, NUMBER_OF_PIXELS_IN_CHART2).forEach(x -> series2.getData().
                add(new XYChart.Data<>(timeUnit.multipliedBy(x).toMillis(), value)));
        return series2;
    }

    private Duration getTimeUnit() {
        int MILLIS_TO_SECONDS = 1000;
        var text = timeUnitTextField.getText();
        //TODO: Exception handling
        return Duration.ofMillis((long)(Double.valueOf(text) * MILLIS_TO_SECONDS));
    }

    private void startTransmittingSignal(Function<Duration, Double> function) {
        timeUnit = getTimeUnit();

        //Adding empty data

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        final int NUMBER_OF_PIXELS_IN_CHART = (int) transmittedSignalChart.getXAxis().getWidth();
        IntStream.range(0, NUMBER_OF_PIXELS_IN_CHART).forEach(x -> series.getData().
                add(new XYChart.Data<>(timeUnit.multipliedBy(x).toMillis(), 0)));
        transmittedSignalChart.getData().add(series);

        XYChart.Series<Number, Number> series2 = new XYChart.Series<>();
        final int NUMBER_OF_PIXELS_IN_CHART2 = (int) receivedSignalChart.getXAxis().getWidth();
        IntStream.range(0, NUMBER_OF_PIXELS_IN_CHART2).forEach(x -> series.getData().
                add(new XYChart.Data<>(timeUnit.multipliedBy(x).toMillis(), 0)));
        receivedSignalChart.getData().add(series2);

        transmitter = new Transmitter(function, this::updateChart, timeUnit);

        timer.scheduleAtFixedRate(transmitter, 0, timeUnit.toMillis());
    }

    private void stopTransmittingSignal() {

    }

    //TODO: Sprawdzic czy nie ma buga z czasem
    private Void updateChart(Duration duration, Double value) {
        var newSeries = shiftSeries(value);
        Platform.runLater(() -> {
            transmittedSignalChart.getData().set(0, newSeries);
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
