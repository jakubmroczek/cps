package cps;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.sun.tools.javac.util.Pair;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
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
import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.DoubleStream;

public class MainViewController {

    private Stage stage;

    private final Map<String, String> labelsToSignalsMap = new HashMap<>();

    // Changes SignalParameters layout
    private final Map<String, Runnable> signalNameToSignalParametersLayoutMap = new HashMap<>();

    private String signal;

    //TODO: Nullable?
    private SignalChart generatedSignalChart;
    private Histogram histogram;

    @FXML
    private LineChart<Number, Number> chart;

    @FXML
    private ComboBox signalList,
                    signalOperationList,
                    extraSignalList;

    @FXML
    private Label averageValueLabel, averageAbsoluteValueLabel,
                        averagePowerValueLabel, varianceValueLabel, effectivePowerValueLabel;

    @FXML
    private BarChart<Number, Number> histogramChart;

    @FXML
    private Slider histogramBinsSlider;

    @FXML
    private VBox signalParameterVBox;

    //TODO: Should be moved to fxml
    @FXML
    private Rectangle emptyRectangle;

    //TODO: Should be moved to fxml
    @FXML
    private HBox displayButtonContainer;


    @FXML
    private Button executeButton;

    @FXML
    private SignalParameter amplitudeSignalParameter,
                            periodSignalParameter,
                            t1SignalParameter,
                            durationSignalParameter,
                            kwSignalParameter,
                            nsSignalParameter,
                            samplingFrequencySignalParameter,
                            probabilitySignalParameter;

    @FXML
    private SignalParameter extraAmplitudeSignalParameter,
            extraPeriodSignalParameter,
            extraT1SignalParameter,
            extraKwSignalParameter,
            extraNsSignalParameter,
            extraProbabilitySignalParameter;

    private int histogramBins = 10;

    public static final ObservableList<String> AVAILABLE_SIGNAL_OPERATIONS = FXCollections.observableArrayList(
      "+", "-", "*", "/"
    );

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
        Signal signal = null;
        Duration durationInNs = null;
        long samplingFrequencyInHz = 0;
        try {
            signal = createSignal();
            double durationdInSeconds = Double.valueOf(durationSignalParameter.getParameterValue().getText());
            durationInNs = Duration.ofNanos((long)(durationdInSeconds * 1_000_000_000L));
            samplingFrequencyInHz = Long.parseLong(samplingFrequencySignalParameter.getParameterValue().getText());

        } catch (NumberFormatException exception) {
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
            generatedSignalChart.setSignalType(Signal.Type.CONTINUOUS);
        } else {
            plotDiscreteSignal(generatedSignalChart);
            generatedSignalChart.setSignalType(Signal.Type.DISCRETE);
        }
        histogram = new Histogram(generatedSignalChart, histogramBins);
        drawHistogram(histogram);

        // TODO: !!!!Pamietaj zeby odciac nadmiarowy czas
        Duration t1 = Duration.ofMillis(Integer.parseInt(t1SignalParameter.getParameterValue().getText()));

        double averageValue = cps.model.Math.averageValue(signal, t1, duration);
        averageValueLabel.setText(String.format("%.2f", averageValue));

        double averageAbsoulteValue = cps.model.Math.averageAbsoluteValue(signal, t1, duration);
        averageAbsoluteValueLabel.setText(String.format("%.2f", averageAbsoulteValue));

        double averagePowerValue = cps.model.Math.averagePower(signal, t1, duration);
        averagePowerValueLabel.setText(String.format("%.2f", averagePowerValue));

        double varianceValue = cps.model.Math.variance(signal, t1, duration);
        varianceValueLabel.setText(String.format("%.2f", varianceValue));

        double effectivePowerValue = cps.model.Math.effectivePower(signal, t1, duration);
        effectivePowerValueLabel.setText(String.format("%.2f", effectivePowerValue));

        SignalArgs signalArgs = SignalArgs.builder()
                                            .signalName(signalList.getSelectionModel().getSelectedItem().toString())
                                            .amplitude(Double.parseDouble(amplitudeSignalParameter.getParameterValue().getText()))
                                            .period(Duration.ofMillis(Integer.parseInt(periodSignalParameter.getParameterValue().getText())))
                                            .initialTime(Duration.ofMillis(Integer.parseInt(t1SignalParameter.getParameterValue().getText())))
                                            .kw(Double.parseDouble(kwSignalParameter.getParameterValue().getText()))
                                            .Ns(Integer.parseInt(nsSignalParameter.getParameterValue().getText()))
                                            .probability(Double.parseDouble(probabilitySignalParameter.getParameterValue().getText()))
                                            .averageValue(Math.round(averageValue,2))
                                            .averageAbsoulteValue(Math.round(averageAbsoulteValue,2))
                                            .averagePowerValue(Math.round(averagePowerValue,2))
                                            .varianceValue(Math.round(varianceValue,2))
                                            .effectivePowerValue(Math.round(effectivePowerValue,2))
                                            .samplingFrequency(SAMPLING_RATE)
                                            .build();

        generatedSignalChart.setArgs(signalArgs);
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
        FileChooser.ExtensionFilter jsonExtension = new FileChooser.ExtensionFilter("JSON Files", "*.json");
        FileChooser.ExtensionFilter binaryExtension = new FileChooser.ExtensionFilter("Binary file", "*.bin");

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Wczytaj sygnał");
        fileChooser.getExtensionFilters().addAll(jsonExtension, binaryExtension);
        File file = fileChooser.showOpenDialog(this.stage);
        if (file == null)
            return;

        FileChooser.ExtensionFilter resultExtension = fileChooser.getSelectedExtensionFilter();
        try {
            if (resultExtension.equals(jsonExtension)) {
                Gson gson = new Gson();
                JsonReader reader = new JsonReader(new FileReader(file));
                SignalChart loadedSignal = gson.fromJson(reader, SignalChart.class);

                drawChartAndHistogram(loadedSignal);
            } else if (resultExtension.equals(binaryExtension)) {
                FileInputStream fis = new FileInputStream(file);
                ObjectInputStream ois = new ObjectInputStream(fis);
                SignalChart loadedSignal = (SignalChart) ois.readObject();
                drawChartAndHistogram(loadedSignal);
            } else {
                throw new UnsupportedOperationException("Signal can not be saved to the file with given extension: " + resultExtension.getExtensions());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void addSignals() {
        SignalChart lhs = loadSignal("1");
        SignalChart rhs = loadSignal("2");
        SignalChart result = SignalOperations.add(lhs, rhs);

        //TODO: Informacja o nazwie wczytanego sygnali xd

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
        String str = labelsToSignalsMap.get(signal);
        Runnable layoutRearrangement = signalNameToSignalParametersLayoutMap.get(str);
        if (layoutRearrangement != null) {
            layoutRearrangement.run();
        }
        signalParameterVBox.getChildren().addAll(emptyRectangle, displayButtonContainer);
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
            lhs = createSignal();
            rhs = createExtraSignal();

            double durationdInSeconds = Double.valueOf(durationSignalParameter.getParameterValue().getText());
            durationInNs = Duration.ofNanos((long)(durationdInSeconds * 1_000_000_000L));
            samplingFrequencyInHz = Long.parseLong(samplingFrequencySignalParameter.getParameterValue().getText());

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
        signalList.getItems().addAll(AVALIABLE_SIGNALS);
        extraSignalList.getItems().addAll(AVALIABLE_SIGNALS);


        //Combo box
        signalOperationList.getItems().addAll(AVAILABLE_SIGNAL_OPERATIONS);

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

        Runnable layoutRearrangement0 = () -> {
            signalParameterVBox.getChildren().clear();
            signalParameterVBox.getChildren().add(amplitudeSignalParameter);
            signalParameterVBox.getChildren().add(durationSignalParameter);
            signalParameterVBox.getChildren().add(samplingFrequencySignalParameter);
        };

        Runnable layoutRearrangement1 = () -> {
            signalParameterVBox.getChildren().clear();
            signalParameterVBox.getChildren().add(amplitudeSignalParameter);
            signalParameterVBox.getChildren().add(periodSignalParameter);
            signalParameterVBox.getChildren().add(t1SignalParameter);
            signalParameterVBox.getChildren().add(durationSignalParameter);
            signalParameterVBox.getChildren().add(samplingFrequencySignalParameter);
        };

        Runnable layoutRearrangement2 = () -> {
            layoutRearrangement1.run();
            signalParameterVBox.getChildren().add(kwSignalParameter);
        };

        Runnable layoutRearrangement3 = () -> {
            layoutRearrangement0.run();
            signalParameterVBox.getChildren().add(nsSignalParameter);
        };

        Runnable layoutRearrangement4 = () -> {
            layoutRearrangement0.run();
            signalParameterVBox.getChildren().add(probabilitySignalParameter);
        };

        signalNameToSignalParametersLayoutMap.put(SignalFactory.LINEARLY_DISTRIBUTED_NOISE, layoutRearrangement0);
        signalNameToSignalParametersLayoutMap.put(SignalFactory.GAUSSIAN_NOISE, layoutRearrangement0);
        signalNameToSignalParametersLayoutMap.put(SignalFactory.SINUSOIDAL, layoutRearrangement1);
        signalNameToSignalParametersLayoutMap.put(SignalFactory.HALF_STRAIGHT_SINUSOIDAL, layoutRearrangement1);
        signalNameToSignalParametersLayoutMap.put(SignalFactory.FULL_STRAIGHT_SINUSOIDAL, layoutRearrangement1);

        signalNameToSignalParametersLayoutMap.put(SignalFactory.UNIT_STEP, () -> {
            signalParameterVBox.getChildren().clear();
            signalParameterVBox.getChildren().add(amplitudeSignalParameter);
            signalParameterVBox.getChildren().add(t1SignalParameter);
            signalParameterVBox.getChildren().add(durationSignalParameter);
            signalParameterVBox.getChildren().add(samplingFrequencySignalParameter);

        });

        signalNameToSignalParametersLayoutMap.put(SignalFactory.RECTANGLE, layoutRearrangement2);
        signalNameToSignalParametersLayoutMap.put(SignalFactory.SYMETRIC_RECTANGLE, layoutRearrangement2);
        signalNameToSignalParametersLayoutMap.put(SignalFactory.TRIANGLE, layoutRearrangement2);

        signalNameToSignalParametersLayoutMap.put(SignalFactory.KRONECKER_DELTA, layoutRearrangement3);

        signalNameToSignalParametersLayoutMap.put(SignalFactory.IMPULSE_NOISE, layoutRearrangement4);

        amplitudeSignalParameter.getParameterName().setText("Amplituda: ");
        amplitudeSignalParameter.getParameterValue().setText("10.0");

        periodSignalParameter.getParameterName().setText("Okres: ");
        periodSignalParameter.getParameterValue().setText("100");

        t1SignalParameter.getParameterName().setText("t1: ");
        t1SignalParameter.getParameterValue().setText("0");

        durationSignalParameter.getParameterName().setText("Czas: ");
        durationSignalParameter.getParameterValue().setText("800");

        kwSignalParameter.getParameterName().setText("kw: ");
        kwSignalParameter.getParameterValue().setText("0.5");

        nsSignalParameter.getParameterName().setText("ns");
        nsSignalParameter.getParameterValue().setText("5");

        samplingFrequencySignalParameter.getParameterName().setText("Czs. prb [Hz]");
        samplingFrequencySignalParameter.getParameterValue().setText("1000");

        probabilitySignalParameter.getParameterName().setText("Prawd.");
        probabilitySignalParameter.getParameterValue().setText("0.5");

        //Jakis wzorzec albo zakapsulkowanie tego zachowania, code duplication
        extraAmplitudeSignalParameter.getParameterName().setText("Amplituda: ");
        extraAmplitudeSignalParameter.getParameterValue().setText("10.0");

        extraPeriodSignalParameter.getParameterName().setText("Okres: ");
        extraPeriodSignalParameter.getParameterValue().setText("100");

        extraT1SignalParameter.getParameterName().setText("t1: ");
        extraT1SignalParameter.getParameterValue().setText("0");

        extraKwSignalParameter.getParameterName().setText("kw: ");
        extraKwSignalParameter.getParameterValue().setText("0.5");

        extraNsSignalParameter.getParameterName().setText("ns");
        extraNsSignalParameter.getParameterValue().setText("5");

        extraProbabilitySignalParameter.getParameterName().setText("Prawd.");
        extraProbabilitySignalParameter.getParameterValue().setText("0.5");

        chart.setAnimated(false);
        chart.setLegendVisible(false);
        histogramChart.setLegendVisible(false);
    }

    private Signal createSignal() throws NumberFormatException {
       try {
           //TODO: Error handling
           double amplitude = Double.parseDouble(amplitudeSignalParameter.getParameterValue().getText());
           //TODO: Assert that unit is not smaller than 1 nanoseconds and that input is seconds
           double periodInSeconds = Double.valueOf(periodSignalParameter.getParameterValue().getText());
           Duration periodInNs = Duration.ofNanos((long)(periodInSeconds * 1_000_000_000L));
           double initialTimeInSeconds = Double.valueOf(t1SignalParameter.getParameterValue().getText());
           Duration initialTimeInNs = Duration.ofNanos((long)(initialTimeInSeconds * 1_000_000_000L));
           int ns = Integer.parseInt(nsSignalParameter.getParameterValue().getText());
           double probability = Double.parseDouble(probabilitySignalParameter.getParameterValue().getText());

           //TODO: connect to fxml object
           //Check if the value is in range
           double kw = Double.parseDouble(kwSignalParameter.getParameterValue().getText());

           SignalArgs args = SignalArgs.builder().amplitude(amplitude).period(periodInNs).initialTime(initialTimeInNs).kw(kw).Ns(ns).probability(probability).build();

           String signalType = labelsToSignalsMap.get(signal);
           return SignalFactory.createSignal(signalType, args);
       } catch (NumberFormatException exception) {
            throw exception;
       }
    }

    private Signal createExtraSignal() throws NumberFormatException {
        try {
            //TODO: Error handling
            double amplitude = Double.parseDouble(extraAmplitudeSignalParameter.getParameterValue().getText());
            //TODO: Assert that unit is not smaller than 1 nanoseconds and that input is seconds
            double periodInSeconds = Double.valueOf(extraPeriodSignalParameter.getParameterValue().getText());
            Duration periodInNs = Duration.ofNanos((long)(periodInSeconds * 1_000_000_000L));
            double initialTimeInSeconds = Double.valueOf(extraT1SignalParameter.getParameterValue().getText());
            Duration initialTimeInNs = Duration.ofNanos((long)(initialTimeInSeconds * 1_000_000_000L));
            int ns = Integer.parseInt(extraNsSignalParameter.getParameterValue().getText());
            double probability = Double.parseDouble(extraProbabilitySignalParameter.getParameterValue().getText());

            //TODO: connect to fxml object
            //Check if the value is in range
            double kw = Double.parseDouble(extraKwSignalParameter.getParameterValue().getText());

            SignalArgs args = SignalArgs.builder().amplitude(amplitude).period(periodInNs).initialTime(initialTimeInNs).kw(kw).Ns(ns).probability(probability).build();

            String key = (String) extraSignalList.getSelectionModel().getSelectedItem();
            String extraSignalType = labelsToSignalsMap.get(key);
            return SignalFactory.createSignal(extraSignalType, args);
        } catch (NumberFormatException exception) {
            throw exception;
        }
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
    private void drawChartAndHistogram(SignalChart loadedSignal){
        setTextFields(loadedSignal);
        long widthInPixels = (long) chart.getXAxis().getWidth();
        final Duration MAX_SAMPLING_RATE = loadedSignal.getDuration().dividedBy(widthInPixels);

        drawChart(loadedSignal, MAX_SAMPLING_RATE);

        Histogram histogram = new Histogram(loadedSignal, histogramBins);
        drawHistogram(histogram);
    }

    private void setTextFields(SignalChart sc) {

        signalList.getSelectionModel().select(AVALIABLE_SIGNALS.indexOf(sc.getArgs().getSignalName()));
        Runnable layoutRearrangement = signalNameToSignalParametersLayoutMap.get(sc.getArgs().getSignalName());
        if (layoutRearrangement != null) {
            layoutRearrangement.run();
        }
        amplitudeSignalParameter.getParameterValue().setText(String.valueOf(sc.getArgs().getAmplitude()));
        periodSignalParameter.getParameterValue().setText(String.valueOf(sc.getArgs().getPeriod().toMillis()));
        t1SignalParameter.getParameterValue().setText(String.valueOf(sc.getArgs().getInitialTime().toMillis()));
        durationSignalParameter.getParameterValue().setText(String.valueOf(sc.getDuration().toMillis()));
        kwSignalParameter.getParameterValue().setText(String.valueOf(sc.getArgs().getKw()));
        nsSignalParameter.getParameterValue().setText(String.valueOf(sc.getArgs().getNs()));
        samplingFrequencySignalParameter.getParameterValue().setText(String.valueOf(sc.getArgs().getSamplingFrequency().toMillis() * 1000));
        probabilitySignalParameter.getParameterValue().setText(String.valueOf(sc.getArgs().getProbability()));

        averageValueLabel.setText(String.valueOf(sc.getArgs().getAverageValue()));
        averageAbsoluteValueLabel.setText(String.valueOf(sc.getArgs().getAverageAbsoulteValue()));
        averagePowerValueLabel.setText(String.valueOf(sc.getArgs().getAveragePowerValue()));
        varianceValueLabel.setText(String.valueOf(sc.getArgs().getVarianceValue()));
        effectivePowerValueLabel.setText(String.valueOf(sc.getArgs().getEffectivePowerValue()));
    }
}
