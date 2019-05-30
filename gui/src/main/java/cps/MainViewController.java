package cps;

import cps.conversion.Quantizer;
import cps.conversion.Reconstructor;
import cps.conversion.Error;
import cps.filter.ImpulseResponseController;
import cps.filtering.Filters;
import cps.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.lang.Math.decrementExact;
import static java.lang.Math.min;

import cps.filtering.*;

public class MainViewController {

    public static final ObservableList<String> AVAILABLE_SIGNAL_OPERATIONS = FXCollections.observableArrayList("+",
            "-",
            "*",
            "/",
            "Splot"
            , "Korelacja",
            "Korelacja przez splot");

    public static final ObservableList<String> FILTER_TYPES = FXCollections.observableArrayList("Low pas",
            "Band pass",
            "High pass");

    public static final ObservableList<String> WINDOW_TYPES = FXCollections.observableArrayList(
            "Rectangle",
            "Hamming",
            "Hanning",
            "Blackman"
    );

    public static final Map<String, String> WINDOW_TYPES_TO_FACTORY_MAP = new HashMap<>();

    private Stage stage;

    private Signal
            signal,
            sampledSignal,
            quantizedSignal,
            interpolatedSignal,
            reconstructedSignal,
            filterHResponse;

    private Histogram histogram;

    @FXML
    private LineChart<Number, Number> chart;
    @FXML
    private ComboBox signalOperationList, filterTypeComboBox, windowTypeComboBox;
    @FXML
    private Label averageValueLabel, averageAbsoluteValueLabel, averagePowerValueLabel, varianceValueLabel, effectivePowerValueLabel;
    @FXML
    private TextField samplingValue,
            bitsValue,
            interpolationFrequencyTextField,
            probesValue,
            sincFq,
            mTextField,
            foTextField;
    @FXML
    private Label mseLabel, snrLabel, psnrLabel, mdLabel, enobLabel;
    @FXML
    private BarChart<Number, Number> histogramChart;
    @FXML
    private Slider histogramBinsSlider;
    @FXML
    private SignalChooser basicSignalChooser, extraSignalChooser;

    private int histogramBins = 10;
    private Signal filteredSignal;

    @FXML
    public void filter() {
        try {
            setCssFiltering(chart.getScene());

            final int filterM = getFilterM();
            final double filterFrequency =  getFilterFrequency();
            final  WindowFunction filterWindowFunction = getFilterWindowFunction();
            final Signal signal = getFilteredSignal();

            FIRFilter filter = createFIRFilter();
            var signals = filter.filter(signal, filterM, filterFrequency, filterWindowFunction);

            filterHResponse = signals.get(0);
            filteredSignal = signals.get(1);

            //TODO: Maybe it shoudl be a different function?
            //TODO: Maybe more charts wil be plotted
            // Checking if was previously generated
//            if (chart.getData().size() == 2) {
//                chart.getData().clear();
//                plotSignal(signal, false);
//            }

            plotSignal(filteredSignal, true);

        } catch (IllegalArgumentException e ) {
            //TODO: Generalize this method, it handles all exception not only thrown during signal creation
            onSignalCreationException(e);
        }
    }

    private FIRFilter createFIRFilter() throws IllegalArgumentException {
        //TODO: Maybe add map for an ellegance, because right now i am too lay to do that
        String type = (String) filterTypeComboBox.getSelectionModel().getSelectedItem();
        FIRFilter firFilter = null;

        if (type.equals(FILTER_TYPES.get(0))) {
            firFilter = new LowPassFilter();
        } else if (type.equals(FILTER_TYPES.get(1))) {
            firFilter = new BandpassFilter();
        } else if (type.equals(FILTER_TYPES.get(2))) {
            firFilter = new HighPassFilter();
        } else {
            throw new IllegalArgumentException("Provided FIRFilter type is not supported " + type);
        }

        return firFilter;
    }

    private Signal getFilteredSignal() {
        return signal;
    }

    private WindowFunction getFilterWindowFunction() {
        String type = (String) windowTypeComboBox.getSelectionModel().getSelectedItem();
        String correspondingNameInFactory = WINDOW_TYPES_TO_FACTORY_MAP.get(type);
        return WindowFunctionFactory.create(correspondingNameInFactory);
    }

    private double getFilterFrequency() throws IllegalArgumentException {
        var frequency = foTextField.getText();
        return Double.parseDouble(frequency);
    }

    private int getFilterM() throws IllegalArgumentException{
        var m = mTextField.getText();
        return Integer.parseInt(m);
    }

    @FXML
    public void sample() {
        double samplingFrequencyInHz = Double.valueOf(samplingValue.getText());
        Duration samplingPeriodInNs = Duration.ofNanos((long) ((1.0 / samplingFrequencyInHz) * 1_000_000_000));

        Function<Double, Double> function = basicSignalChooser.creatFunction();
        //TODO: Wrap is somewhere
        Duration durationInNs = Duration.ofNanos((long) (basicSignalChooser.getDurationInSeconds() * 1_000_000_000));

        interpolatedSignal = quantizedSignal = sampledSignal = Signal.createContinousSignal(function, durationInNs, samplingPeriodInNs);

        setCssSamplingSignal(bitsValue.getScene());
        plotSignal(sampledSignal, true);
        drawHistogram(sampledSignal);

        clearSignalMeasurements();
    }

    @FXML
    public void quantize() {
        int bits = Integer.valueOf(bitsValue.getText());

        quantizedSignal = Quantizer.quantize(sampledSignal, bits);

        setCssSamplingSignal(bitsValue.getScene());

        chart.getData().clear();
        plotSignal(quantizedSignal, false);

        drawHistogram(quantizedSignal);
        clearSignalMeasurements();
        displaySignalsError(quantizedSignal, sampledSignal);
    }

    @FXML
    public void interpolate() {
        double interpolationFrequencyInHz = Double.valueOf(interpolationFrequencyTextField.getText());
        Duration interpolationPeriodInNs = Duration.ofNanos((long) ((1.0 / interpolationFrequencyInHz) * 1_000_000_000));

        // Byl problem np z czzestoliwosciami 5 i 15 bo jak sie dzieli je na okres,
        // to jest blad przy zaokraglaniu

        setInterpolationCss(bitsValue.getScene());

        interpolatedSignal = Reconstructor.firstHoldInterpolation(quantizedSignal, interpolationPeriodInNs);

        chart.getData().clear();
        plotSignal(interpolatedSignal, false);
        clearSignalMeasurements();
        drawHistogram(interpolatedSignal);
    }

    @FXML
    public void sinc() {
        int probes = Integer.valueOf(probesValue.getText());

        double freqInHz = Double.valueOf(sincFq.getText());

        Duration frequencyInNs = Duration.ofNanos((long) ((1.0 / freqInHz) * 1_000_000_000));

        reconstructedSignal = Reconstructor.reconstruct(quantizedSignal, frequencyInNs, probes);

        chart.getData().clear();
        setCssLineSignals(bitsValue.getScene());
        plotSignal(reconstructedSignal, false);
        drawHistogram(reconstructedSignal);
        clearSignalMeasurements();
    }

    @FXML
    public void display() {
        try {
            setCssSingleSignal(bitsValue.getScene());
            Function<Double, Double> function = basicSignalChooser.creatFunction();
            SignalArgs args = basicSignalChooser.getSignalArgs();

            Duration durationInNs = Duration.ofNanos((long) (basicSignalChooser.getDurationInSeconds() * 1_000_000_000));

            //TODO: Sprawdz sampling frequency
            signal = Signal.create(basicSignalChooser.getSignalType(), function, durationInNs, args.getSamplingFrequency());

            if (signal.getType() == Signal.Type.CONTINUOUS) {
                setCssContinous(bitsValue.getScene());
            } else {
                setCssDiscrete(bitsValue.getScene());
            }

            plotSignal(signal, true);
            drawHistogram(signal);
            clearSignalMeasurements();
            SignalMeasurement signalMeasurement = measure(signal, function, durationInNs);
            displaySignalMeasurement(signalMeasurement);

        } catch (IllegalArgumentException exception) {
            onSignalCreationException(exception);
        }
    }

    //TODO: Open in the same stage/window
    @FXML
    public void loadDistanceSimulation() {
        final FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/DistanceSimulation.fxml"));
        Parent root = null;
        try {
            root = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Scene nscene = new Scene(root);
        Stage tStatge = new Stage();
        tStatge.setTitle("Symulacja korelacyjnego czujnika odległości");
        tStatge.setScene(nscene);
        tStatge.show();
    }

    SignalMeasurement measure(Signal signal, Function<Double, Double> function, Duration durationInNs) {
        if (signal.getType() == Signal.Type.CONTINUOUS) {
            return SignalMeasurement.measure(function, 0, durationInNs.toNanos());
        } else {
            return SignalMeasurement.measure(signal);
        }
    }

    private void plotSignal(Signal signal, boolean clearChart) {
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

        if (clearChart) chart.getData().clear();
        chart.getData().add(series);
    }

    private void plotSignal(InterpolatedSignal signal) {
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

    @FXML
    private void saveToFile() {
        FileChooser.ExtensionFilter jsonExtension = new FileChooser.ExtensionFilter("JSON File", "*.json");
        FileChooser.ExtensionFilter binaryExtension = new FileChooser.ExtensionFilter("Binary file", "*.bin");

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Zapisz sygnał");
        fileChooser.getExtensionFilters().addAll(binaryExtension, jsonExtension);
        File file = fileChooser.showSaveDialog(this.stage);

        if (file == null || signal == null) {
            return;
        }

        FileChooser.ExtensionFilter resultExtension = fileChooser.getSelectedExtensionFilter();

        if (resultExtension.equals(jsonExtension)) {
            SignalWriter.writeJSON(file, signal);
        } else {
            Float f = Float.parseFloat(basicSignalChooser.map(SignalChooser.Field.T1).getParameterValue().getText());
            SignalWriter.writeBinary(file, f, basicSignalChooser.getSamplingFrequencyInHz(), signal);
        }
    }

    @FXML
    public void loadSignal() {
        try {
            signal = loadFromFile();

            if (signal.getType() == Signal.Type.CONTINUOUS) {
                setCssContinous(bitsValue.getScene());
            } else {
                setCssDiscrete(bitsValue.getScene());
            }

            plotSignal(signal, true);
            drawHistogram(signal);
            //TODO: We do not have info about function so we must use for the discrete signal or maybe
            //TODO: Or functions can be merged together
            SignalMeasurement signalMeasurement = SignalMeasurement.measure(signal);
            displaySignalMeasurement(signalMeasurement);

            basicSignalChooser.displaySignal(signal, "Zaladowany z pliku");
        } catch (IOException e) {
            onSignalCreationException(e);
        }
    }

    private Signal loadFromFile() throws IOException {
        FileChooser.ExtensionFilter jsonExtension = new FileChooser.ExtensionFilter("JSON Files", "*.json");
        FileChooser.ExtensionFilter binaryExtension = new FileChooser.ExtensionFilter("Binary file", "*.bin");

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Wczytaj sygnał");
        fileChooser.getExtensionFilters().addAll(jsonExtension, binaryExtension);

        File file = fileChooser.showOpenDialog(this.stage);

        if (file == null) {
            throw new IOException("Unable to open provided file");
        }

        FileChooser.ExtensionFilter resultExtension = fileChooser.getSelectedExtensionFilter();

        if (resultExtension.equals(jsonExtension)) {
            return SignalWriter.readJSON(file);
        } else {
            return SignalWriter.readBinary(file);
        }
    }

    @FXML
    void add() {
        loadSignalsAndApplyOperator(SignalOperations::add);
    }

    @FXML
    void subtract() {
        loadSignalsAndApplyOperator(SignalOperations::subtract);
    }

    @FXML
    void multiply() {
        loadSignalsAndApplyOperator(SignalOperations::multiply);
    }

    @FXML
    void divide() {
        loadSignalsAndApplyOperator(SignalOperations::divide);
    }

    private void loadSignalsAndApplyOperator(BiFunction<Signal, Signal, Signal> operator) {
        try {
            Signal lhs = loadFromFile();
            Signal rhs = loadFromFile();
            signal = operator.apply(lhs, rhs);

            if (signal.getType() == Signal.Type.CONTINUOUS) {
                setCssContinous(bitsValue.getScene());
            } else {
                setCssDiscrete(bitsValue.getScene());
            }

            plotSignal(signal, true);
            drawHistogram(signal);
            //TODO: We do not have info about function so we must use for the discrete signal or maybe
            //TODO: Or functions can be merged together
            SignalMeasurement signalMeasurement = SignalMeasurement.measure(signal);
            displaySignalMeasurement(signalMeasurement);

            basicSignalChooser.displaySignal(signal, "Zaladowane z pliku");
        } catch (IOException e) {
            onSignalCreationException(e);
        }
    }

    @FXML
    public void onExecuteButton() {
        String operation = (String) signalOperationList.getSelectionModel().getSelectedItem();
        BiFunction<Signal, Signal, Signal> operator;

        switch (operation) {
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

            case "Splot":
                operator = Filters::convolute;
                break;

            case "Korelacja":
                operator = Filters::correlate;
                break;

            case "Korelacja przez splot":
                operator = Filters::correlateByConvolution;
                break;

            default:
                throw new UnsupportedOperationException("Unknown operation type in combo list.");
        }

        try {
            Function lhsFunction = basicSignalChooser.creatFunction();
            Function rhsFunction = extraSignalChooser.creatFunction();

            double durationInSeconds = Double.valueOf(basicSignalChooser.getDurationInSeconds());
            Duration durationInNs = Duration.ofNanos((long) (durationInSeconds * 1_000_000_000L));

            long samplingFrequencyInHz = basicSignalChooser.getSamplingFrequencyInHz();
            final Duration USER_SAMPLING_RATE = Duration.ofNanos((long) ((1.0 / samplingFrequencyInHz) * 1_000_000_000));

            Signal.Type type = basicSignalChooser.getSignalType();

            Signal lhs = Signal.create(type, lhsFunction, durationInNs, USER_SAMPLING_RATE);
            Signal rhs = Signal.create(type, rhsFunction, durationInNs, USER_SAMPLING_RATE);

            signal = operator.apply(lhs, rhs);

            if (signal.getType() == Signal.Type.CONTINUOUS) {
                setCssContinous(bitsValue.getScene());
            } else {
                setCssDiscrete(bitsValue.getScene());
            }

            plotSignal(signal, true);
            drawHistogram(signal);
            //TODO: We do not have info about function so we must use for the discrete signal or maybe
            //TODO: Or functions can be merged together
            SignalMeasurement signalMeasurement = SignalMeasurement.measure(signal);
            displaySignalMeasurement(signalMeasurement);

        } catch (NumberFormatException exception) {
            onSignalCreationException(exception);
        }
    }

    @FXML
    public void initialize() {
        initializeAllComboBox();

        // We need to hide duration and sampling frequency fields in extraSignalChooser from the client,
        // because those values will be provided by querying basicSignalChooser.
        removeDurationAndSamplingFrequencyFieldsFromExtraSignalChooser();

        chart.setAnimated(false);
        chart.setLegendVisible(false);
        histogramChart.setCategoryGap(0);

        histogramChart.setBarGap(0);
        histogramChart.setAnimated(false);
        histogramChart.setLegendVisible(false);

        //Reareane layout
        basicSignalChooser.onSignalChosen();
        extraSignalChooser.onSignalChosen();

        // Maps
        WINDOW_TYPES_TO_FACTORY_MAP.put(WINDOW_TYPES.get(0), WindowFunctionFactory.RECTANGUIAR_WINDOW);
        WINDOW_TYPES_TO_FACTORY_MAP.put(WINDOW_TYPES.get(1), WindowFunctionFactory.HAMMING_WINDOW);
        WINDOW_TYPES_TO_FACTORY_MAP.put(WINDOW_TYPES.get(2), WindowFunctionFactory.HANNING_WINDOW);
        WINDOW_TYPES_TO_FACTORY_MAP.put(WINDOW_TYPES.get(3), WindowFunctionFactory.BLACKMAN_WINDOW);
    }

    private void initializeAllComboBox() {
        initializeComboBox(signalOperationList, AVAILABLE_SIGNAL_OPERATIONS);
        initializeComboBox(filterTypeComboBox, FILTER_TYPES);
        initializeComboBox(windowTypeComboBox, WINDOW_TYPES);
    }

    private void initializeComboBox(ComboBox comboBox, ObservableList<String> content) {
        comboBox.getItems().addAll(content);
        comboBox.setValue(content.get(0));
    }

    //Moze byc tylko wykonywane na watku GUI (wewnatrz metody z annotacja @FXML lub Platform.runLater), w przeciwnym razie crashe
    private void drawHistogram(Signal signal) {
        histogram = new Histogram(signal, histogramBins);

        XYChart.Series series1 = new XYChart.Series();

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
    public void showH() {
        if (filterHResponse != null) {
            final FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/ImpulseResponse.fxml"));
            Parent root = null;
            try {
                root = fxmlLoader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Scene nscene = new Scene(root);
            Stage tStatge = new Stage();
            tStatge.setTitle("Odpowiedź impulsowa");
            tStatge.setScene(nscene);
            tStatge.show();

            var controller = (ImpulseResponseController)fxmlLoader.getController();
            controller.plot(filterHResponse);
        }
    }

    @FXML
    public void onHistogramBinsChanged() {
        int newHistogramBins = (int) histogramBinsSlider.getValue();
        if (newHistogramBins != histogramBins) {
            histogramBins = newHistogramBins;
            //TODO: Can we be sure that is not null?
            if (signal != null) {
                drawHistogram(signal);
            }
        }
    }

    private void onSignalCreationException(Exception e) {
        //TODO: Restore default state of the controlls after the crush
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setHeaderText("Invalid input");
        errorAlert.setContentText(e.getMessage() + "\n" + e.getCause());
        errorAlert.showAndWait();
    }

    private void removeDurationAndSamplingFrequencyFieldsFromExtraSignalChooser() {
        extraSignalChooser.setArrangement(FunctionFactory.LINEARLY_DISTRIBUTED_NOISE, SignalChooser.Field.AMPLITUDE);
        extraSignalChooser.setArrangement(FunctionFactory.GAUSSIAN_NOISE, SignalChooser.Field.AMPLITUDE);
        extraSignalChooser.setArrangement(FunctionFactory.SINUSOIDAL, SignalChooser.Field.AMPLITUDE, SignalChooser.Field.PERIOD,
                SignalChooser.Field.T1);
        extraSignalChooser.setArrangement(FunctionFactory.HALF_STRAIGHT_SINUSOIDAL, SignalChooser.Field.AMPLITUDE,
                SignalChooser.Field.PERIOD, SignalChooser.Field.T1);
        extraSignalChooser.setArrangement(FunctionFactory.FULL_STRAIGHT_SINUSOIDAL, SignalChooser.Field.AMPLITUDE,
                SignalChooser.Field.PERIOD, SignalChooser.Field.T1);
        extraSignalChooser.setArrangement(FunctionFactory.UNIT_STEP, SignalChooser.Field.AMPLITUDE, SignalChooser.Field.T1);
        extraSignalChooser.setArrangement(FunctionFactory.RECTANGLE, SignalChooser.Field.AMPLITUDE, SignalChooser.Field.PERIOD,
                SignalChooser.Field.T1, SignalChooser.Field.KW);
        extraSignalChooser.setArrangement(FunctionFactory.SYMETRIC_RECTANGLE, SignalChooser.Field.AMPLITUDE, SignalChooser.Field.PERIOD,
                SignalChooser.Field.T1, SignalChooser.Field.KW);
        extraSignalChooser.setArrangement(FunctionFactory.TRIANGLE, SignalChooser.Field.AMPLITUDE, SignalChooser.Field.PERIOD,
                SignalChooser.Field.T1, SignalChooser.Field.KW);
        extraSignalChooser.setArrangement(FunctionFactory.KRONECKER_DELTA, SignalChooser.Field.AMPLITUDE, SignalChooser.Field.NS);
        extraSignalChooser.setArrangement(FunctionFactory.IMPULSE_NOISE, SignalChooser.Field.AMPLITUDE, SignalChooser.Field.PROBABILITY);
    }

    private void displaySignalMeasurement(SignalMeasurement signalMeasurement) {
        averageValueLabel.setText(String.format("%.2f", signalMeasurement.getAverage()));
        averageAbsoluteValueLabel.setText(String.format("%.2f", signalMeasurement.getAbsoluteAverage()));
        averagePowerValueLabel.setText(String.format("%.2f", signalMeasurement.getAveragePower()));
        varianceValueLabel.setText(String.format("%.2f", signalMeasurement.getVariance()));
        effectivePowerValueLabel.setText(String.format("%.2f", signalMeasurement.getEffectivePower()));
    }

    private void displaySignalsError(Signal s1, Signal s2) {
        mseLabel.setText(String.format("%.2f", Error.mse(s1, s2)));
        snrLabel.setText(String.format("%.2f", Error.snr(s1, s2)));
        psnrLabel.setText(String.format("%.2f", Error.psnr(s1, s2)));
        mdLabel.setText(String.format("%.2f", Error.md(s1, s2)));
        enobLabel.setText(String.format("%.2f", Error.enob(s1, s2)));
    }

    private final String cssSingleSignal = "/styles/chartSingleSignal.css";
    private final String cssSamplingSignal = "/styles/chartSampling.css";
    private final String cssLineSignals = "/styles/chartLineSignals.css";
    private final String cssInterpolation = "/styles/interpolation.css";
    private final String cssDiscrete = "/styles/discrete.css";
    private final String cssContinous = "/styles/continous.css";
    private final String cssFiltering = "/styles/FilterSignal.css";

    private void setCssSingleSignal(Scene scene) {
        removeChartCss(scene);
        scene.getStylesheets().add(cssSingleSignal);
    }

    private void setCssSamplingSignal(Scene scene) {
        removeChartCss(scene);
        scene.getStylesheets().add(cssSamplingSignal);
    }

    private void setCssLineSignals(Scene scene) {
        removeChartCss(scene);
        scene.getStylesheets().add(cssLineSignals);
    }

    private void setInterpolationCss(Scene scene) {
        removeChartCss(scene);
        scene.getStylesheets().add(cssInterpolation);
    }

    private void setCssContinous(Scene scene) {
        removeChartCss(scene);
        scene.getStylesheets().add(cssContinous);
    }

    private void setCssDiscrete(Scene scene) {
        removeChartCss(scene);
        scene.getStylesheets().add(cssDiscrete);
    }

    private void setCssFiltering(Scene scene) {
        removeChartCss(scene);
        scene.getStylesheets().addAll(cssFiltering);
    }

    private void removeChartCss(Scene scene) {
        scene.getStylesheets().remove(cssSingleSignal);
        scene.getStylesheets().remove(cssSamplingSignal);
        scene.getStylesheets().remove(cssLineSignals);
        scene.getStylesheets().remove(cssInterpolation);
        scene.getStylesheets().remove(cssContinous);
        scene.getStylesheets().remove(cssInterpolation);
        scene.getStylesheets().remove(cssFiltering);
    }

    private void clearSignalMeasurements() {
        averageValueLabel.setText("");
        averageAbsoluteValueLabel.setText("");
        averagePowerValueLabel.setText("");
        varianceValueLabel.setText("");
        effectivePowerValueLabel.setText("");

        mseLabel.setText("");
        snrLabel.setText("");
        psnrLabel.setText("");
        mdLabel.setText("");
        enobLabel.setText("");
    }

}
