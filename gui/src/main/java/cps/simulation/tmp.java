package cps.simulation;

import cps.model.FunctionFactory;
import cps.model.SignalArgs;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import lombok.Setter;

import java.time.Duration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.IntStream;

class DistanceSimulatiosn {

    // Needed for rendering
    ConcurrentLinkedQueue<Double> transmittedSignalChartQueue = new ConcurrentLinkedQueue<>();

    @FXML
    private Pane transmittedSignalChartPane, receivedSignalChartPane;

    @FXML
    private TextField timeUnitTextField;

    private LineChart<Number, Number> transmittedSignalChart, receivedSignalChart;

    private Timer timer = new Timer();
    private Transmitter transmitter;

    private AnimationTimer animationTimer;

    private Duration timeUnit;
    ExecutorService executor = Executors.newCachedThreadPool(r -> {
        Thread thread = new Thread(r);
        thread.setDaemon(true);
        return thread;
    });

    private Duration getTimeUnit() {
        int MILLIS_TO_SECONDS = 1000;
        var text = timeUnitTextField.getText();
        //TODO: Exception handling
        return Duration.ofMillis((long) (Double.valueOf(text) * MILLIS_TO_SECONDS));
    }

    private void startTransmittingSignal(Function<Duration, Double> function) {
        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                //TODO: Find better name
                draw();
            }
        };

        timeUnit = getTimeUnit();

        //Adding empty data
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        final int NUMBER_OF_PIXELS_IN_CHART = (int) transmittedSignalChart.getXAxis().getWidth();
        IntStream.range(0, NUMBER_OF_PIXELS_IN_CHART).forEach(x -> series.getData().
                add(new XYChart.Data<>(timeUnit.multipliedBy(x).toMillis(), 0)));
        transmittedSignalChart.getData().add(series);

        XYChart.Series<Number, Number> series2 = new XYChart.Series<>();
        final int NUMBER_OF_PIXELS_IN_CHART2 = (int) receivedSignalChart.getXAxis().getWidth();
        IntStream.range(0, NUMBER_OF_PIXELS_IN_CHART2).forEach(x -> series2.getData().
                add(new XYChart.Data<>(timeUnit.multipliedBy(x).toMillis(), 0)));
        receivedSignalChart.getData().add(series2);

        transmitter = new Transmitter(function, this::updateChart, timeUnit);

//        timer.scheduleAtFixedRate(transmitter, 0, timeUnit.toMillis());
        executor.execute(transmitter);
        animationTimer.start();
    }

    private int i = 0;

    private void draw() {
        ObservableList<XYChart.Data<Number, Number>> data = FXCollections.observableArrayList();
        while (!transmittedSignalChartQueue.isEmpty()) {
            System.out.println("rysujemy");

            for (int i = 0; i < data.size() - 1; i++) {
                data.add(new XYChart.Data(timeUnit.multipliedBy(i).toMillis(), data.get(i + 1).getYValue()));
            }
            data.add(new XYChart.Data<>(timeUnit.multipliedBy(data.size() - 1).toMillis(), transmittedSignalChartQueue.remove()));

            Platform.runLater(() -> {
                System.out.println("co jest kurwa");
                var size = transmittedSignalChart.getData().get(0).getData().size();
                transmittedSignalChart.getData().get(0).getData().add(new XYChart.Data<>(
                        timeUnit.multipliedBy(size).toMillis(),
                        size
                ));
            });
        }
    }

    private void stopTransmittingSignal() {

    }

    private void initializeCharts() {
        final NumberAxis xAxis = new NumberAxis(0, 100, 0.1);
        final NumberAxis yAxis = new NumberAxis(-2, 2, 10);

        transmittedSignalChart = new LineChart<>(xAxis, yAxis) {
            @Override
            protected void dataItemAdded(Series<Number, Number> series, int itemIndex, Data<Number, Number> item) {
            }
        };

        transmittedSignalChart.setAnimated(false);
        transmittedSignalChart.setLegendVisible(false);

        transmittedSignalChartPane.getChildren().add(transmittedSignalChart);

        receivedSignalChart = new LineChart<>(xAxis, yAxis) {
            @Override
            protected void dataItemAdded(Series<Number, Number> series, int itemIndex, Data<Number, Number> item) {
            }
        };

        receivedSignalChart.setAnimated(false);
        receivedSignalChart.setLegendVisible(false);

        receivedSignalChartPane.getChildren().add(receivedSignalChart);
    }

    //TODO: Sprawdzic czy nie ma buga z czasem
    private Void updateChart(Duration duration, Double value) {
        System.out.println("nowy element");
        transmittedSignalChartQueue.add(value);
        return null;
    }

    @FXML
    public void start() {
        loadCSS();

        var args = SignalArgs.builder().amplitude(1).initialTimeInNs(0).periodInNs(500_000).initialTimeInNs(0).build();
        var sineFunction = FunctionFactory.createFunction(FunctionFactory.SINUSOIDAL, args);

        Function<Duration, Double> wrapper = (Duration x) -> sineFunction.apply((double) x.toNanos());

        startTransmittingSignal(wrapper);
    }
    //Cannot be done in initialize method, cause chart's do not have associated scene
    //Where it can be moved?

    private void loadCSS() {
//        final String style = "/styles/DistanceSimulation.css";
//
//        var scene = transmittedSignalChart.getScene();
//        scene.getStylesheets().add(style);
    }

    @FXML
    public void initialize() {
        initializeCharts();
    }

    class Transmitter extends TimerTask {

        private Function<Duration, Double> function;
        private Duration transmmisionPeriod;

        @Setter
        private BiFunction<Duration, Double, Void> callback;

        private Duration time = Duration.ZERO;

        private long sleepTimeInMs;

        Transmitter(Function<Duration, Double> function, BiFunction<Duration, Double, Void> callback, Duration transmmisionPeriod) {
            this.function = function;
            this.callback = callback;
            this.transmmisionPeriod = transmmisionPeriod;
            this.sleepTimeInMs = transmmisionPeriod.toMillis();
        }

        @Override
        public void run() {
            var emittedValue = function.apply(time);
            callback.apply(time, emittedValue);
            time = time.plus(transmmisionPeriod);

            try {
                Thread.sleep(sleepTimeInMs);
                executor.execute(this);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}


public class tmp {
}
