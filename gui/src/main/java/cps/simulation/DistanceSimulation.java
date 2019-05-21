package cps.simulation;

import cps.model.FunctionFactory;
import cps.model.SignalArgs;
import javafx.animation.AnimationTimer;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;

import javafx.scene.control.TextField;

import java.time.Duration;
import java.util.Timer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;
import java.util.stream.IntStream;

public class DistanceSimulation {

    @FXML
    private LineChart<Number, Number> transmittedSignalChart, receivedSignalChart;

    @FXML
    private TextField timeUnitTextField,
            probingSignalFrequencyTextField,
            bufferSizeTextField,
            objectSpeedInMetersPerSecond,
            realDistanceInMetersTextField;

    private ConcurrentLinkedQueue<XYChart.Series<Number, Number>> seriesConcurrentLinkedQueue = new ConcurrentLinkedQueue<>();

    private volatile XYChart.Series<Number, Number> bufferedSeries = new XYChart.Series<>();

    private SimpleDoubleProperty realDistanceToTrackedObjectInMeters = new SimpleDoubleProperty(1.0);

    private TrackedObject trackedObject;

    private Timer timer = new Timer();
    private Transmitter transmitter;

    private AnimationTimer animationTimer;

    private Duration timeUnit;

    private XYChart.Series<Number, Number> shiftSeries(double value) {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();


        if (bufferedSeries.getData().isEmpty())
        {
            return series;
        }

        final int size = bufferedSeries.getData().size();
        IntStream.range(1, size).forEach(x -> series.getData().
                add(new XYChart.Data<>(timeUnit.multipliedBy(x).toMillis(),
                bufferedSeries.getData().get(x).getYValue())));


        series.getData().
                add(new XYChart.Data<>(timeUnit.multipliedBy(size - 1).toMillis(),
                        value));

        return series;
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
        IntStream.range(0, getBufferSize()).forEach(x -> series.getData().
                add(new XYChart.Data<>(timeUnit.multipliedBy(x).toMillis(), 0)));
        transmittedSignalChart.getData().add(series);

        //TODO: Use only me
        bufferedSeries = series;

        //TODO: Uwaga bug! zmienia rozmiar popzredniej seri series
//        XYChart.Series<Number, Number> series2 = new XYChart.Series<>();
//        final int NUMBER_OF_PIXELS_IN_CHART2 = (int) receivedSignalChart.getXAxis().getWidth();
//        IntStream.range(0, NUMBER_OF_PIXELS_IN_CHART2).forEach(x -> series.getData().
//                add(new XYChart.Data<>(timeUnit.multipliedBy(x).toMillis(), 0)));
//        receivedSignalChart.getData().add(series2);

        //TODO: Fajny bylby obserwator z mapowaniem
        realDistanceInMetersTextField.textProperty().bind(realDistanceToTrackedObjectInMeters.asString());

        trackedObject = new TrackedObject(getObjectSpeedInMetersPerSecond(),
                getRealDistanceToTrackedObjectInMeters());

        transmitter = new Transmitter(function, this::updateChart, timeUnit);

        timer.scheduleAtFixedRate(transmitter, 0, timeUnit.toMillis());

        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!seriesConcurrentLinkedQueue.isEmpty()) {
                    var series = seriesConcurrentLinkedQueue.remove();
                    transmittedSignalChart.getData().set(0, series);

                }
            }
        };
        animationTimer.start();
    }

    private void stopTransmittingSignal() {

    }

    //TODO: Sprawdzic czy nie ma buga z czasem

    private Void updateChart(Duration duration, Double value) {
        var newSeries = shiftSeries(value);
        seriesConcurrentLinkedQueue.add(newSeries);
        bufferedSeries = newSeries;

        realDistanceToTrackedObjectInMeters.set(trackedObject.getDistanceSinceStart(duration));

        return null;
    }
    @FXML
    public  void start() {
        loadCSS();

        var args = SignalArgs.builder().amplitude(1).initialTimeInNs(0).periodInNs(getProbingSignalPeriodInNs()).initialTimeInNs(0).build();
        var sineFunction = FunctionFactory.createFunction(FunctionFactory.SINUSOIDAL, args);

        var secondSignalArgs = SignalArgs.builder().amplitude(1).initialTimeInNs(0).periodInNs(getProbingSignalPeriodInNs() / 10).initialTimeInNs(0).build();
        var secondSineFunction = FunctionFactory.createFunction(FunctionFactory.SINUSOIDAL, secondSignalArgs);


        Function<Duration, Double> wrapper = (Duration x) -> {
            double timeInNanos = (double) x.toNanos();
            return sineFunction.apply(timeInNanos) * secondSineFunction.apply(timeInNanos);
        };

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

    private double getProbingSignalPeriodInNs() {
        double frequency = Double.valueOf(probingSignalFrequencyTextField.getText());
        var period = 1.0 / frequency;
        //TODO: Do not use magick numbers
        return period * 1_000_000_000;
    }

    private int getBufferSize() {
        return Integer.valueOf(bufferSizeTextField.getText());
    }

    //TODO: Encapsulate speed in object
    private double getObjectSpeedInMetersPerSecond() {
        return Double.valueOf(objectSpeedInMetersPerSecond.getText());
    }

    private double getRealDistanceToTrackedObjectInMeters() {
        return realDistanceToTrackedObjectInMeters.get();
    }
}
