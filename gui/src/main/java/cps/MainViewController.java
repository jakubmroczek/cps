package cps;

import cps.model.*;
import cps.model.Math;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import cps.model.Signal;
import cps.model.SignalArgs;
import cps.model.SignalChart;
import cps.model.SignalFactory;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import javafx.scene.chart.*;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.*;

public class MainViewController {

    private Stage stage;
    private Signal currentSignal;
    //TODO: Nullable?
    private SignalChart generatedSignalChart;
    @FXML
    private LineChart<Number, Number> chart;

    @FXML
    NumberAxis xAxis;

    @FXML
    NumberAxis yAxis;

    @FXML
    ComboBox signalList;

    @FXML
    private TextField amplitude, period, initialTime, duration, kwTextField;

    @FXML
    private TextField averageValueTextField, averageAbsoluteValueTextField;

    @FXML
    private BarChart<Number, Number> histogramChart;

    private int histogramBins = 20;

    @FXML
    public void display() {
        Signal signal = createSignal();
        currentSignal = signal;

        Duration _duration = Duration.ofMillis(Integer.parseInt(duration.getText()));

        long widthInPixels = (long) chart.getXAxis().getWidth();
        final Duration MAX_SAMPLING_RATE = _duration.dividedBy(widthInPixels);

        SignalChart sc = signal.createChart(_duration, MAX_SAMPLING_RATE);
        generatedSignalChart = sc;

        System.out.println(MAX_SAMPLING_RATE);
        System.out.println(sc.getProbes().size());

        drawChart(sc, MAX_SAMPLING_RATE);

        Histogram histogram = new Histogram(sc, histogramBins);
        drawHistogram(histogram);

        System.out.println(chart.getXAxis().getWidth());

        // TODO: !!!!Pamietaj zeby odciac nadmiarowy czas

        double averageValue = Math.averageValue(signal, Duration.ZERO, _duration);
        averageValueTextField.setText(String.format("%.2f", averageValue));

        double averageAbsoulteValue = Math.averageAbsoluteValue(signal, Duration.ZERO, _duration);
        averageAbsoluteValueTextField.setText(String.format("%.2f", averageAbsoulteValue));
    }

    private void drawChart(SignalChart signalChart, Duration samplingRate) {
        chart.setAnimated(false);
        chart.setCreateSymbols(false);
        chart.getStyleClass().add("thick-chart");
        XYChart.Series series = new XYChart.Series();
        series.setName("sinusoida1");

        System.out.println("");
        for (int i = 0; i < signalChart.getProbes().size(); i++) {
            double y = signalChart.getProbes().get(i);
            System.out.println(i + " " + y);

            //Mozliwosc przeklamania przez zmiane jednostke
            if (samplingRate.toMillis() != 0) {
                series.getData().add(new XYChart.Data(samplingRate.multipliedBy(i).toMillis(), y));
            } else {
                //HOW TO HANDLE THIS?
                //NANOSECONDS
                series.getData().add(new XYChart.Data(samplingRate.multipliedBy(i).toNanos(), y));
            }

        }

        chart.getData().clear();
        chart.getData().add(series);
    }

    @FXML
    private void saveToFile() {
        FileChooser.ExtensionFilter fcExtension = new FileChooser.ExtensionFilter("JSON Files", "*.json");

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Zapisz sygnał");
        fileChooser.getExtensionFilters().add(fcExtension);
        File file = fileChooser.showSaveDialog(this.stage);
        if (file == null)
            return;
        Gson gson = new Gson();

        //TODO: Other sceneario
        if (generatedSignalChart != null) {
            String signalJson = gson.toJson(generatedSignalChart);
            try {
                Files.write(file.toPath(), signalJson.getBytes());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }  else {
            System.out.println("generatedSignal is null");
        }
    }

    @FXML
    private void loadFromFile() {
        FileChooser.ExtensionFilter fcExtension = new FileChooser.ExtensionFilter("JSON Files", "*.json");

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Wczytaj sygnał");
        fileChooser.getExtensionFilters().add(fcExtension);
        File file = fileChooser.showOpenDialog(this.stage);
        if (file == null)
            return;

        try {
            Gson gson = new Gson();
            JsonReader reader = new JsonReader(new FileReader(file));
            SignalChart loadedSignal = gson.fromJson(reader, SignalChart.class);

            signalList.getSelectionModel().select(AVALIABLE_SIGNALS.indexOf(loadedSignal.getSignalType()));

                //TODO: Change info of the labes
//            amplitude.setText(String.valueOf((int) loadedSignal.getArgs().getAmplitude()));
//            period.setText(loadedSignal.getProbingPeriod().toString());
//            initialTime.setText(loadedSignal.getArgs().getInitialTime().toString());
//            duration.setText(loadedSignal.getDuration().toString());

            long widthInPixels = (long) chart.getXAxis().getWidth();
            final Duration MAX_SAMPLING_RATE = loadedSignal.getDuration().dividedBy(widthInPixels);
            drawChart(loadedSignal, MAX_SAMPLING_RATE);
            Histogram histogram = new Histogram(loadedSignal, histogramBins);
            drawHistogram(histogram);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void addSignals() {
        SignalChart lhs = loadSignal("1");
        SignalChart rhs = loadSignal("2");
        SignalChart result = SignalOperations.add(lhs, rhs);

        //TODO: Informacja o nazwie wczytanego sygnali xd

//        saveToFile(null);

        long widthInPixels = (long) chart.getXAxis().getWidth();
        final Duration MAX_SAMPLING_RATE = result.getDuration().dividedBy(widthInPixels);

        drawChart(result, MAX_SAMPLING_RATE);
    }

    @FXML
    private void subtractSignals() {
        SignalChart lhs = loadSignal("1");
        SignalChart rhs = loadSignal("2");

        SignalChart result = SignalOperations.subtract(lhs, rhs);

//        saveToFile(null);

        long widthInPixels = (long) chart.getXAxis().getWidth();
        final Duration MAX_SAMPLING_RATE = result.getDuration().dividedBy(widthInPixels);


        drawChart(result, MAX_SAMPLING_RATE);
    }

    @FXML
    private void multiplySignals() {
        SignalChart lhs = loadSignal("1");
        SignalChart rhs = loadSignal("2");

        SignalChart result = SignalOperations.multiply(lhs, rhs);

//        saveToFile(null);

        long widthInPixels = (long) chart.getXAxis().getWidth();
        final Duration MAX_SAMPLING_RATE = result.getDuration().dividedBy(widthInPixels);


        drawChart(result, MAX_SAMPLING_RATE);
    }

    @FXML
    private void divideSignals() {
        SignalChart lhs = loadSignal("1");
        SignalChart rhs = loadSignal("2");

        SignalChart result = SignalOperations.divide(lhs, rhs);

//        saveToFile(null);

        long widthInPixels = (long) chart.getXAxis().getWidth();
        final Duration MAX_SAMPLING_RATE = result.getDuration().dividedBy(widthInPixels);


        drawChart(result, MAX_SAMPLING_RATE);
    }

    private SignalChart loadSignal(String sygnal) {
        FileChooser.ExtensionFilter fcExtension = new FileChooser.ExtensionFilter("JSON Files", "*.json");

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Wczytaj sygnał " + sygnal);
        fileChooser.getExtensionFilters().add(fcExtension);
        File file = fileChooser.showOpenDialog(this.stage);
        if (file == null)
            return null;

        try {
            Gson gson = new Gson();
            JsonReader reader = new JsonReader(new FileReader(file));
            SignalChart s1 = gson.fromJson(reader, SignalChart.class);
//            signalList.getSelectionModel().select(AVALIABLE_SIGNALS.indexOf(s1.getSignalType()));

            long widthInPixels = (long) chart.getXAxis().getWidth();
            final Duration MAX_SAMPLING_RATE = s1.getDuration().dividedBy(widthInPixels);

            drawChart(s1, MAX_SAMPLING_RATE);
            return s1;
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    @FXML
    public void onSignalChoice() {
        signal = (String) signalList.getSelectionModel().getSelectedItem();
    }

    @FXML
    public void initialize() {
        signalList.getItems().addAll(AVALIABLE_SIGNALS);
    }

    private Signal createSignal() {
        //TODO: Error handling
        double _amplitude = Double.parseDouble(amplitude.getText());
        Duration _period = Duration.ofMillis(Integer.parseInt(period.getText()));
        Duration _initialTime = Duration.ofMillis(Integer.parseInt(initialTime.getText()));
        //TODO: connect to fxml object

        //Check if the value is in range
        double kw = Double.parseDouble(kwTextField.getText());

        SignalArgs args = new SignalArgs(_amplitude, _period, _initialTime, kw);

        String signalType = labelsToSignalsMap.get(signal);
        return SignalFactory.createSignal(signalType, args);
    }

    //Moze byc tylko wykonywane na watku GUI (wewnatrz metody z annotacja @FXML lub Platform.runLater), w przeciwnym razie crashe
    private void drawHistogram(Histogram histogram) {

        histogramChart.setCategoryGap(0);
        histogramChart.setBarGap(0);
        histogramChart.setAnimated(false);
        XYChart.Series series1 = new XYChart.Series();
        series1.setName("Histogram");

        double currentRange = histogram.getMin();
        final double columnWidth = (histogram.getMax() - histogram.getMin()) / histogram.getBins();
        for (int i = 0; i < histogram.getBins(); i++) {
            //TODO: ADD HISTOGRAM COLUMN LENGTH TO THE HISTOGRAM CLASS
            String result = String.format("%.2f", currentRange);
            series1.getData().add(new XYChart.Data(result, histogram.getFrequencyList().get(i)));
            currentRange += columnWidth;
        }

        histogramChart.getData().clear();
        histogramChart.getData().add(series1);
    }

    private static final ObservableList<String> AVALIABLE_SIGNALS = FXCollections.observableArrayList(
            "Szum o rozkładzie jednostajnym",
            "Szum gaussowski",
            "Sygnał sinusoidalny",
            "Sygnał sinusoidalny wyprostowany jednopołówkowo",
            "Sygnał sinusoidalny wyprsotowany dwupołówkowo",
            "Sygnał prostokątny",
            "Sygnał prostokątny symetryczny",
            "Sygnał trójkątny",
            "Skok jednostkowy",
            "Impuls jednostkowy",
            "Szum impulsowy"
    );

    private final Map<String, String> labelsToSignalsMap = new HashMap<>();

    //Static initializer block
    {
        labelsToSignalsMap.put(AVALIABLE_SIGNALS.get(0), SignalFactory.LINEARLY_DISTRIBUTED_NOISE);
        labelsToSignalsMap.put(AVALIABLE_SIGNALS.get(1), SignalFactory.GAUSSIAN_NOISE);
        labelsToSignalsMap.put(AVALIABLE_SIGNALS.get(2), SignalFactory.SINUSOIDAL);
        labelsToSignalsMap.put(AVALIABLE_SIGNALS.get(3), SignalFactory.HALF_STRAIGHT_SINUSOIDAL);
        labelsToSignalsMap.put(AVALIABLE_SIGNALS.get(4), SignalFactory.FULL_STRAIGHT_SINUSOIDAL);
        labelsToSignalsMap.put(AVALIABLE_SIGNALS.get(5), SignalFactory.RECTANGLE);
        labelsToSignalsMap.put(AVALIABLE_SIGNALS.get(6), SignalFactory.SYMETRIC_RECTANGLE);
        labelsToSignalsMap.put(AVALIABLE_SIGNALS.get(7), SignalFactory.TRIANGLE);
    }

    private String signal;
}