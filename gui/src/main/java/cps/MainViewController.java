package cps;

import cps.model.*;

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
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;

import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.*;

public class MainViewController {

    private Stage stage;
    private Signal currentSignal;

    private final Map<String, String> labelsToSignalsMap = new HashMap<>();

    private String signal;

    //TODO: Nullable?
    private SignalChart generatedSignalChart;
    private Histogram histogram;

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
    private TextField nsTextField, samplingFrequencyTextField, probabilityTextField;

    @FXML
    private TextField averageValueTextField, averageAbsoluteValueTextField;

    @FXML
    private BarChart<Number, Number> histogramChart;

    @FXML
    private Slider histogramBinsSlider;

    private int histogramBins = 10;

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

    @FXML
    public void display() {
        Signal signal = createSignal();
        currentSignal = signal;

        Duration _duration = Duration.ofMillis(Integer.parseInt(duration.getText()));
        long samplingFrequencyInHz = Long.parseLong(samplingFrequencyTextField.getText());

        final Duration SAMPLING_RATE = Duration.ofNanos((long)((1.0 / samplingFrequencyInHz) * 1_000_000_000));
        generatedSignalChart = signal.createChart(_duration, SAMPLING_RATE);

        if (signal.getType() == Signal.Type.CONTINUOUS) {
            plotContinuousSignal(signal, _duration);
        } else {
            plotDiscreteSignal(generatedSignalChart);
        }

        histogram = new Histogram(generatedSignalChart, histogramBins);
        drawHistogram(histogram);

        // TODO: !!!!Pamietaj zeby odciac nadmiarowy czas

//        double averageValue = Math.averageValue(signal, Duration.ZERO, _duration);
//        averageValueTextField.setText(String.format("%.2f", averageValue));
//
//        double averageAbsoulteValue = Math.averageAbsoluteValue(signal, Duration.ZERO, _duration);
//        averageAbsoluteValueTextField.setText(String.format("%.2f", averageAbsoulteValue));
    }

    private void plotDiscreteSignal(SignalChart signalChart) {
        //Miej na uwadze czestotliwosc probkowania sygnalu tak zeby byl w miare czytelny na wykresie
        chart.setCreateSymbols(true);
        chart.getStyleClass().remove("continuous-signal");
        chart.getStyleClass().add("discrete-signal");
        XYChart.Series series = new XYChart.Series();

        for (int i = 0; i < signalChart.getProbes().size(); i++) {
            double y = signalChart.getProbes().get(i);

            //Mozliwosc przeklamania przez zmiane jednostke
            if (signalChart.getProbingPeriod().toMillis() != 0) {
                series.getData().add(new XYChart.Data(signalChart.getProbingPeriod().multipliedBy(i).toMillis(), y));
            } else {
                //HOW TO HANDLE THIS?
                //NANOSECONDS
                series.getData().add(new XYChart.Data(signalChart.getProbingPeriod().multipliedBy(i).toNanos(), y));
            }

        }

        chart.getData().clear();
        chart.getData().add(series);
    }

    private void plotContinuousSignal(Signal signal, Duration duration) {
        //One point in one sample point
        long widthInPixels = (long) chart.getXAxis().getWidth();
        final Duration SAMPLING_RATE = duration.dividedBy(widthInPixels);

        SignalChart signalChart = signal.createChart(duration, SAMPLING_RATE);

        chart.setCreateSymbols(false);
        chart.getStyleClass().remove("discrete-signal");
        chart.getStyleClass().add("continuous-signal");
        XYChart.Series series = new XYChart.Series();

        for (int i = 0; i < signalChart.getProbes().size(); i++) {
            double y = signalChart.getProbes().get(i);

            //Mozliwosc przeklamania przez zmiane jednostke
            if (SAMPLING_RATE.toMillis() != 0) {
                series.getData().add(new XYChart.Data(SAMPLING_RATE.multipliedBy(i).toMillis(), y));
            } else {
                //HOW TO HANDLE THIS?
                //NANOSECONDS
                series.getData().add(new XYChart.Data(SAMPLING_RATE.multipliedBy(i).toNanos(), y));
            }

        }

        chart.getData().clear();
        chart.getData().add(series);
    }

    //TODO: Differ between discrete and continuous functions
    private void drawChart(SignalChart signalChart, Duration samplingRate) {
        chart.setCreateSymbols(false);
        chart.getStyleClass().remove("discrete-signal");
        chart.getStyleClass().add("continuous-signal");
        XYChart.Series series = new XYChart.Series();

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
        FileChooser.ExtensionFilter jsonExtension = new FileChooser.ExtensionFilter("JSON File", "*.json");
        FileChooser.ExtensionFilter binaryExtension = new FileChooser.ExtensionFilter("Binary file", "*.bin");

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Zapisz sygnał");
        fileChooser.getExtensionFilters().addAll(jsonExtension, binaryExtension);
        File file = fileChooser.showSaveDialog(this.stage);
        if (file == null)
            return;

        FileChooser.ExtensionFilter resultExtension = fileChooser.getSelectedExtensionFilter();

        if (resultExtension.equals(jsonExtension)) {
            SignalWriter.writeJSON(file, generatedSignalChart);
        } else if (resultExtension.equals(binaryExtension)) {
            SignalWriter.writeBinary(file, generatedSignalChart);
        } else {
            throw new UnsupportedOperationException("Signal can not be saved to the file with given extension: " + resultExtension.getExtensions());
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

        long widthInPixels = (long) chart.getXAxis().getWidth();
        final Duration MAX_SAMPLING_RATE = result.getDuration().dividedBy(widthInPixels);


        drawChart(result, MAX_SAMPLING_RATE);
    }

    @FXML
    private void multiplySignals() {
        SignalChart lhs = loadSignal("1");
        SignalChart rhs = loadSignal("2");

        SignalChart result = SignalOperations.multiply(lhs, rhs);

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
        //Combo box
        signalList.getItems().addAll(AVALIABLE_SIGNALS);

        //Mapper gui name to factory name
        labelsToSignalsMap.put(AVALIABLE_SIGNALS.get(0), SignalFactory.LINEARLY_DISTRIBUTED_NOISE);
        labelsToSignalsMap.put(AVALIABLE_SIGNALS.get(1), SignalFactory.GAUSSIAN_NOISE);
        labelsToSignalsMap.put(AVALIABLE_SIGNALS.get(2), SignalFactory.SINUSOIDAL);
        labelsToSignalsMap.put(AVALIABLE_SIGNALS.get(3), SignalFactory.HALF_STRAIGHT_SINUSOIDAL);
        labelsToSignalsMap.put(AVALIABLE_SIGNALS.get(4), SignalFactory.FULL_STRAIGHT_SINUSOIDAL);
        labelsToSignalsMap.put(AVALIABLE_SIGNALS.get(5), SignalFactory.RECTANGLE);
        labelsToSignalsMap.put(AVALIABLE_SIGNALS.get(6), SignalFactory.SYMETRIC_RECTANGLE);
        labelsToSignalsMap.put(AVALIABLE_SIGNALS.get(7), SignalFactory.TRIANGLE);
        labelsToSignalsMap.put(AVALIABLE_SIGNALS.get(8), SignalFactory.UNIT_STEP);
        labelsToSignalsMap.put(AVALIABLE_SIGNALS.get(9), SignalFactory.KRONECKER_DELTA);
        labelsToSignalsMap.put(AVALIABLE_SIGNALS.get(10), SignalFactory.IMPULSE_NOISE);

        chart.setAnimated(false);
    }

    private Signal createSignal() {
        //TODO: Error handling
        double _amplitude = Double.parseDouble(amplitude.getText());
        Duration _period = Duration.ofMillis(Integer.parseInt(period.getText()));
        Duration _initialTime = Duration.ofMillis(Integer.parseInt(initialTime.getText()));
        int ns = Integer.parseInt(nsTextField.getText());
        double probability = Double.parseDouble(probabilityTextField.getText());

        //TODO: connect to fxml object
        //Check if the value is in range
        double kw = Double.parseDouble(kwTextField.getText());

        SignalArgs args = SignalArgs.builder().amplitude(_amplitude).period(_period).initialTime(_initialTime).kw(kw).Ns(ns).probability(probability).build();

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

    @FXML
    public void onHistogramBinsChanged() {
        int newHistogramBins = (int) histogramBinsSlider.getValue();
        if (newHistogramBins != histogramBins) {
            histogramBins = newHistogramBins;
            //TODO: Can we be sure that is not null?
            if (generatedSignalChart != null) {
                histogram = new Histogram(generatedSignalChart, histogramBins);
                drawHistogram(histogram);
            }
        }
    }

}