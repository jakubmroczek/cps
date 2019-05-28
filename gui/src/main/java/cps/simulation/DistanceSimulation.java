package cps.simulation;

import cps.model.FunctionFactory;
import cps.model.SignalArgs;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;

import javafx.scene.control.TextField;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;
import java.util.stream.IntStream;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class DistanceSimulation {

    private List<Double> samples = new ArrayList();

    @FXML
    private LineChart<Number, Number> transmittedSignalChart, receivedSignalChart, correlationChart;

    @FXML
    private TextField timeUnitTextField,
            probingSignalFrequencyTextField,
            bufferSizeTextField,
            objectSpeedInMetersPerSecond,
            realDistanceInMetersTextField,
            estimatedlDistanceInMetersTextField,
            reportPeriodTextField;

    private ConcurrentLinkedQueue<XYChart.Series<Number, Number>> seriesConcurrentLinkedQueue = new ConcurrentLinkedQueue<>();
    private ConcurrentLinkedQueue<XYChart.Series<Number, Number>> receivedSignaSeriesQueue = new ConcurrentLinkedQueue<>();
    private ConcurrentLinkedQueue<XYChart.Series<Number, Number>> correlationChartSeriesQueue = new ConcurrentLinkedQueue<>();

    private XYChart.Series<Number, Number> bufferedSeries = new XYChart.Series<>();
    private XYChart.Series<Number, Number> bufferReceivedSignalSeries = new XYChart.Series<>();

    private double initialDistanceInMeters = 100.0;
    private volatile SimpleDoubleProperty realDistanceToTrackedObjectInMeters = new SimpleDoubleProperty(100.0);

    private TrackedObject trackedObject;

    private Timer timer = new Timer();
    private Transmitter transmitter;

    private AnimationTimer animationTimer;

    private Duration timeUnit;
    private Duration reportPeriod;
    private Duration previousUpdateTime;

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
        IntStream.range(0, getBufferSize()).forEach(x -> series.getData().
                add(new XYChart.Data<>(timeUnit.multipliedBy(x).toMillis(), 0)));
        transmittedSignalChart.getData().add(series);

        bufferedSeries = series;

        XYChart.Series<Number, Number> receivedSeries = new XYChart.Series<>();
        IntStream.range(0, getBufferSize()).forEach(x -> receivedSeries.getData().
                add(new XYChart.Data<>(timeUnit.multipliedBy(x).toMillis(), 0)));
        receivedSignalChart.getData().add(receivedSeries);

        bufferReceivedSignalSeries = receivedSeries;

        XYChart.Series<Number, Number> correlationSeries = new XYChart.Series<>();
        IntStream.range(0, getBufferSize()).forEach(x -> correlationSeries.getData().
                add(new XYChart.Data<>(timeUnit.multipliedBy(x).toMillis(), 0)));
        correlationChart.getData().add(correlationSeries);

        realDistanceInMetersTextField.textProperty().bind(realDistanceToTrackedObjectInMeters.asString());

        trackedObject = new TrackedObject(getObjectSpeedInMetersPerSecond(),
                getRealDistanceToTrackedObjectInMeters());


        // Obsluga wiekszych od 1
        var seconds = Duration.ofSeconds(1).dividedBy(timeUnit);
        Duration result = Duration.ofSeconds(seconds);

        transmitter = new Transmitter(function, this::updateChart, result);

        timer.scheduleAtFixedRate(transmitter, 0, timeUnit.toMillis());

        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!seriesConcurrentLinkedQueue.isEmpty()) {
                    var series = seriesConcurrentLinkedQueue.remove();
                    transmittedSignalChart.getData().set(0, series);
                }

                if (!receivedSignaSeriesQueue.isEmpty()) {
                    var series = receivedSignaSeriesQueue.remove();
                    receivedSignalChart.getData().set(0, series);
                }

                if (!correlationChartSeriesQueue.isEmpty()) {
                    var series = correlationChartSeriesQueue.remove();
                    correlationChart.getData().set(0 ,series);
                }
            }
        };
        animationTimer.start();
    }

    private void stopTransmittingSignal() {

    }

    //TODO: Sprawdzic czy nie ma buga z czasem

    // Lepsza nazwa
    private Void updateChart(Duration duration, Double value) {
        var newSeries = shiftSeries(value);
        seriesConcurrentLinkedQueue.add(newSeries);
        bufferedSeries = newSeries;
        
        double val = trackedObject.getDistanceSinceStart(duration);
        Platform.runLater(() -> realDistanceToTrackedObjectInMeters.set(val));

        samples.add(value);

        listen(duration);

        tryCalculateDistanceByCorrelation(duration);

        return null;
    }

    private void tryCalculateDistanceByCorrelation(Duration duration) {
        if (isDistanceUpdateTime()) {
            int value = new Random().nextInt(20);
            var series = new XYChart.Series<Number, Number>();
            IntStream.range(0, getBufferSize()).forEach(x -> series.getData().
                    add(new XYChart.Data<>(timeUnit.multipliedBy(x).toMillis(), value)));
            correlationChartSeriesQueue.add(series);
        }
    }

    private boolean isDistanceUpdateTime() {
        //TODO: Not good idea cause using system calls slows down a lot i guess
        Duration now = Duration.ofMillis(System.currentTimeMillis());

        if (now.minus(previousUpdateTime).compareTo(reportPeriod) > 0) {
            previousUpdateTime = now;
            return true;
        }

        return false;
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

        // Intializing vairables!!
        reportPeriod = getReportPeriodInTextField();
        previousUpdateTime = Duration.ofMillis(System.currentTimeMillis());

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
        transmittedSignalChart.getYAxis().setTickLabelsVisible(false);
        transmittedSignalChart.getYAxis().setOpacity(0);
        transmittedSignalChart.getXAxis().setTickLabelsVisible(false);
        transmittedSignalChart.getXAxis().setOpacity(0);

        receivedSignalChart.setAnimated(false);
        receivedSignalChart.setLegendVisible(false);
        receivedSignalChart.getYAxis().setTickLabelsVisible(false);
        receivedSignalChart.getYAxis().setOpacity(0);
        receivedSignalChart.getXAxis().setTickLabelsVisible(false);
        receivedSignalChart.getXAxis().setOpacity(0);

        correlationChart.setAnimated(false);
        correlationChart.setLegendVisible(false);
        correlationChart.getYAxis().setTickLabelsVisible(false);
        correlationChart.getYAxis().setOpacity(0);
        correlationChart.getXAxis().setTickLabelsVisible(false);
        correlationChart.getXAxis().setOpacity(0);
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

    private double getSignalPropagationSpeedInMetersPerSecond() {return 5;}

    private double getRealDistanceToTrackedObjectInMeters() {
        return realDistanceToTrackedObjectInMeters.get();
    }

    private void listen(Duration duration) {
        double index = duration.toMillis() - ((2 * initialDistanceInMeters) / (getSignalPropagationSpeedInMetersPerSecond() - getObjectSpeedInMetersPerSecond()) * 1000.0);

        //TMP
        index *= 100;

        index /= (getProbingSignalPeriodInNs() / 1_000_000);
        index = min(index, samples.size() - 1);
        index = max(0, index);
        System.out.println(index);

        // Dlaczego puste
        if (index >= 0) {
            XYChart.Series<Number, Number> receivedSeries = new XYChart.Series<>();

            if (bufferReceivedSignalSeries.getData().isEmpty())
            {
                return;
            }

            IntStream.range(1, getBufferSize()).forEach(x -> receivedSeries.getData().add(
                    new XYChart.Data<>(x * timeUnit.toMillis(), bufferReceivedSignalSeries.getData().get(x).getYValue())
            ));

            receivedSeries.getData().add(new XYChart.Data((getBufferSize() - 1) * timeUnit.toMillis(), samples.get((int) index)));

            bufferReceivedSignalSeries = receivedSeries;

            receivedSignaSeriesQueue.add(receivedSeries);
        }
    }

    //TODO: Remove code duplication
    Duration getReportPeriodInTextField() {
        int MILLIS_TO_SECONDS = 1000;
        var text = reportPeriodTextField.getText();
        //TODO: Exception handling
        return Duration.ofMillis((long)(Double.valueOf(text) * MILLIS_TO_SECONDS));
    }
}
