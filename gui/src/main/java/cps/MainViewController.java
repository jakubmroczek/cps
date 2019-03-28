package cps;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import cps.model.*;

import cps.model.Signal;
import cps.model.SignalArgs;
import cps.model.SignalChart;
import cps.model.SignalFactory;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import javafx.scene.chart.*;
import javafx.scene.control.*;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import sun.security.x509.AVA;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.Math;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.DoubleStream;

public class MainViewController {

    private Stage stage;

    //TODO: Nullable?
    private SignalChart generatedSignalChart;
    private Histogram histogram;

    @FXML
    private LineChart<Number, Number> chart;

    @FXML
    private ComboBox signalOperationList;

    @FXML
    private TextField averageValueTextField, averageAbsoluteValueTextField;

    @FXML
    private BarChart<Number, Number> histogramChart;

    @FXML
    private Slider histogramBinsSlider;

    @FXML
    private SignalChooser basicSignalChooser, extraSignalChooser;

    private int histogramBins = 10;

    public static final ObservableList<String> AVAILABLE_SIGNAL_OPERATIONS = FXCollections.observableArrayList(
      "+", "-", "*", "/"
    );

    @FXML
    public void display() {
        Signal signal;
        Duration durationInNs;
        long samplingFrequencyInHz;

        try {
            signal = basicSignalChooser.getSignal();

            double durationInSeconds = basicSignalChooser.getDurationInSeconds();
            durationInNs = Duration.ofNanos((long)(durationInSeconds * 1_000_000_000L));
            samplingFrequencyInHz = basicSignalChooser.getSamplingFrequencyInHz();

        } catch (IllegalArgumentException exception) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setHeaderText("Input not valid");
            errorAlert.setContentText(exception.getMessage() + "\n" + exception.getCause());
            errorAlert.showAndWait();
            //TODO: Restore default state of the contolls after the crush
            return;
        }

        final Duration SAMPLING_RATE = Duration.ofNanos((long)((1.0 / samplingFrequencyInHz) * 1_000_000_000));
        generatedSignalChart = signal.createChart(durationInNs, SAMPLING_RATE);

        if (signal.getType() == Signal.Type.CONTINUOUS) {
            plotContinuousSignal(signal, durationInNs);
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

        long widthInPixels = (long) chart.getXAxis().getWidth();
        double stepInSeconds = signalChart.getDuration().toNanos() / 1_000_000_000D;
        //TODO: Dzielenie przez zero!!
        stepInSeconds /= Math.min(widthInPixels, signalChart.getProbes().size() - 1);

        for (int i = 0; i < signalChart.getProbes().size(); i++) {
            double y = signalChart.getProbes().get(i);
            series.getData().add(new XYChart.Data(stepInSeconds*i, y));
        }

        chart.getData().clear();
        chart.getData().add(series);
    }

    private void plotContinuousSignal(Signal signal, Duration duration) {
        //One point in one sample point
        long widthInPixels = (long) chart.getXAxis().getWidth();
        double stepInSeconds = duration.toNanos() / 1_000_000_000D;
        stepInSeconds /=widthInPixels;

        final Duration SAMPLING_RATE = duration.dividedBy(widthInPixels);
        SignalChart signalChart = signal.createChart(duration, SAMPLING_RATE);

        chart.setCreateSymbols(false);
        chart.getStyleClass().remove("discrete-signal");
        chart.getStyleClass().add("continuous-signal");
        XYChart.Series series = new XYChart.Series();

        for (int i = 0; i < signalChart.getProbes().size(); i++) {
            double y = signalChart.getProbes().get(i);
            series.getData().add(new XYChart.Data(stepInSeconds*i, y));
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
    public void onExecuteButton() {
        String operation = (String) signalOperationList.getSelectionModel().getSelectedItem();

        BiFunction<SignalChart, SignalChart, SignalChart> operator;
        //TODO: Extra map
        switch (operation)
        {
            case "+":
                operator = SignalOperations::add;
                break;

            case "-":
                operator = SignalOperations::subtract;
                break;

            case "*":
                operator = SignalOperations::multiply;
                break;

            case "/":
                operator = SignalOperations::divide;
                break;

            default:
                throw new UnsupportedOperationException("unkown operatoin type in combo list.");
        }

        Signal lhs = null, rhs = null;
        Duration durationInNs = null;
        long samplingFrequencyInHz = 0;
        try {
            lhs = basicSignalChooser.getSignal();
            rhs = extraSignalChooser.getSignal();

            double durationInSeconds = Double.valueOf(basicSignalChooser.getDurationInSeconds());
            durationInNs = Duration.ofNanos((long)(durationInSeconds * 1_000_000_000L));

            samplingFrequencyInHz = basicSignalChooser.getSamplingFrequencyInHz();

        } catch (NumberFormatException exception) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setHeaderText("Input not valid");
            errorAlert.setContentText(exception.getMessage() + "\n" + exception.getCause());
            errorAlert.showAndWait();
            //TODO: Restore default state of the contolls after the crush
            return;
        }

        final Duration USER_SAMPLING_RATE = Duration.ofNanos((long)((1.0 / samplingFrequencyInHz) * 1_000_000_000));


        SignalChart sc1 = lhs.createChart(durationInNs, USER_SAMPLING_RATE);
        SignalChart sc2 = rhs.createChart(durationInNs, USER_SAMPLING_RATE);
        SignalChart result = operator.apply(sc1, sc2);

        if (lhs.getType() == Signal.Type.CONTINUOUS) {
            //One point in one sample point
            long widthInPixels = (long) chart.getXAxis().getWidth();
            double stepInSeconds = durationInNs.toNanos() / 1_000_000_000D;
            stepInSeconds /=widthInPixels;

            final Duration SAMPLING_RATE = durationInNs.dividedBy(widthInPixels);
            SignalChart signalChart = operator.apply(sc1, sc2);

            chart.setCreateSymbols(false);
            chart.getStyleClass().remove("discrete-signal");
            chart.getStyleClass().add("continuous-signal");
            XYChart.Series series = new XYChart.Series();

            for (int i = 0; i < signalChart.getProbes().size(); i++) {
                double y = signalChart.getProbes().get(i);
                series.getData().add(new XYChart.Data(stepInSeconds*i, y));
            }

            chart.getData().clear();
            chart.getData().add(series);
        } else {
//            plotDiscreteSignal(generatedSignalChart);
        }

        histogram = new Histogram(result, histogramBins);
        drawHistogram(histogram);
    }

    @FXML
    public void initialize() {
        //Combo box
        signalOperationList.getItems().addAll(AVAILABLE_SIGNAL_OPERATIONS);

        chart.setAnimated(false);
        chart.setLegendVisible(false);
        histogramChart.setLegendVisible(false);
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
