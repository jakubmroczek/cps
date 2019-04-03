package cps;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import cps.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.Math;
import java.time.Duration;
import java.util.function.BiFunction;

import static java.lang.Math.min;

public class MainViewController {

    public static final ObservableList<String> AVAILABLE_SIGNAL_OPERATIONS = FXCollections.observableArrayList("+", "-", "*", "/");
    private Stage stage;
    //TODO: Nullable?
    private SignalChart generatedSignalChart;
    private Histogram histogram;
    @FXML private LineChart<Number, Number> chart;
    @FXML private ComboBox signalOperationList;
    @FXML private Label averageValueLabel, averageAbsoluteValueLabel, averagePowerValueLabel, varianceValueLabel, effectivePowerValueLabel;
    @FXML private BarChart<Number, Number> histogramChart;
    @FXML private Slider histogramBinsSlider;
    @FXML private SignalChooser basicSignalChooser, extraSignalChooser;
    private int histogramBins = 10;

    @FXML public void display() {
        Signal signal;
        Duration durationInNs;
        long samplingFrequencyInHz;
        SignalArgs signalArgs;

        try {
            signal = basicSignalChooser.getSignal();

            double durationInSeconds = basicSignalChooser.getDurationInSeconds();
            durationInNs = Duration.ofNanos((long) (durationInSeconds * 1_000_000_000L));
            samplingFrequencyInHz = basicSignalChooser.getSamplingFrequencyInHz();
            signalArgs = basicSignalChooser.getSignalArgs();

        } catch (IllegalArgumentException exception) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setHeaderText("Input not valid");
            errorAlert.setContentText(exception.getMessage() + "\n" + exception.getCause());
            errorAlert.showAndWait();
            //TODO: Restore default state of the contolls after the crush
            return;
        }

        final Duration SAMPLING_RATE = Duration.ofNanos((long) ((1.0 / samplingFrequencyInHz) * 1_000_000_000));
        generatedSignalChart = signal.createChart(durationInNs, SAMPLING_RATE);

        generatedSignalChart.setSignalType(signal.getType());
        plotSignal(generatedSignalChart);

        //TODO: Usun nadmiarowy czas
        histogram = new Histogram(generatedSignalChart, histogramBins);
        drawHistogram(histogram);

        //TODO: Usun nadmiarowy czas
        double averageValue = cps.model.Math.averageValue(signal, Duration.ZERO, durationInNs);
        averageValueLabel.setText(String.format("%.2f", averageValue));

        double averageAbsoulteValue = cps.model.Math.averageAbsoluteValue(signal, Duration.ZERO, durationInNs);
        averageAbsoluteValueLabel.setText(String.format("%.2f", averageAbsoulteValue));

        double averagePowerValue = cps.model.Math.averagePower(signal, Duration.ZERO, durationInNs);
        averagePowerValueLabel.setText(String.format("%.2f", averagePowerValue));

        double varianceValue = cps.model.Math.variance(signal, Duration.ZERO, durationInNs);
        varianceValueLabel.setText(String.format("%.2f", varianceValue));

        double effectivePowerValue = cps.model.Math.effectivePower(signal, Duration.ZERO, durationInNs);
        effectivePowerValueLabel.setText(String.format("%.2f", effectivePowerValue));

        signalArgs.setAverageValue(averageValue);
        signalArgs.setAverageAbsoulteValue(averageAbsoulteValue);
        signalArgs.setAveragePowerValue(averagePowerValue);
        signalArgs.setVarianceValue(varianceValue);
        signalArgs.setEffectivePowerValue(effectivePowerValue);
        generatedSignalChart.setArgs(signalArgs);
    }

    private void plotSignal(SignalChart signal) {
        if (signal.getSignalType() == Signal.Type.CONTINUOUS)
        prepareChartToDisplayContinousSignal();
        else
            prepareChartToDisplayDiscreteSignal();

         XYChart.Series series = new XYChart.Series();

        final double NUMBER_OF_PIXELS_IN_CHART = chart.getXAxis().getWidth();
        double singlePointDurationInSeconds = signal.getDuration().toNanos() / 1_000_000_000D;
        if (signal.getProbes().size() != 1) {
            singlePointDurationInSeconds /= min(NUMBER_OF_PIXELS_IN_CHART, signal.getProbes().size() - 1);
        }

        long step = 1;
        if (signal.getProbes().size() > NUMBER_OF_PIXELS_IN_CHART)
             step = (long) (signal.getProbes().size() / NUMBER_OF_PIXELS_IN_CHART);

       for (int i = 0, j = 0; i < signal.getProbes().size(); i += step, j++)  {
                   double y = signal.getProbes().get(i);
           series.getData().add(new XYChart.Data(singlePointDurationInSeconds * j, y));
       }

        chart.getData().clear();
        chart.getData().add(series);
    }

    private void prepareChartToDisplayContinousSignal() {
        chart.setCreateSymbols(false);
        chart.getStyleClass().remove("discrete-signal");
        chart.getStyleClass().add("continuous-signal");
    }

    private void prepareChartToDisplayDiscreteSignal() {
        chart.setCreateSymbols(true);
        chart.getStyleClass().remove("continuous-signal");
        chart.getStyleClass().add("discrete-signal");
    }

    //TODO: Differ between discrete and continuous functions
    private void drawChart(SignalChart signalChart) {
        chart.setCreateSymbols(false);
        chart.getStyleClass().remove("discrete-signal");
        chart.getStyleClass().add("continuous-signal");
        XYChart.Series series = new XYChart.Series();

        double stepInSeconds = signalChart.getDuration().toNanos() / 1_000_000_000D / signalChart.getProbes().size();
        System.out.println("STEP IN SEC: " + stepInSeconds);
        for (int i = 0; i < signalChart.getProbes().size(); i++) {
            double y = signalChart.getProbes().get(i);
            series.getData().add(new XYChart.Data(stepInSeconds * i, y));
        }

        chart.getData().clear();
        chart.getData().add(series);
    }

    @FXML private void saveToFile() {
        FileChooser.ExtensionFilter jsonExtension = new FileChooser.ExtensionFilter("JSON File", "*.json");
        FileChooser.ExtensionFilter binaryExtension = new FileChooser.ExtensionFilter("Binary file", "*.bin");

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Zapisz sygnał");
        fileChooser.getExtensionFilters().addAll(binaryExtension, jsonExtension);
        File file = fileChooser.showSaveDialog(this.stage);
        if (file == null)
            return;

        FileChooser.ExtensionFilter resultExtension = fileChooser.getSelectedExtensionFilter();

        if (resultExtension.equals(jsonExtension)) {
            SignalWriter.writeJSON(file, generatedSignalChart);
        } else if (resultExtension.equals(binaryExtension)) {
            Float f = Float.parseFloat(basicSignalChooser.map(SignalChooser.Field.T1).getParameterValue().getText());

            SignalWriter.writeBinary(file, f, basicSignalChooser.getSamplingFrequencyInHz(), generatedSignalChart);
        } else {
            throw new UnsupportedOperationException(
                    "Signal can not be saved to the file with given extension: " + resultExtension.getExtensions());
        }
    }

    @FXML private void loadFromFile() {
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
                SignalChart loadedSignal = SignalWriter.readBinary(file);
                 drawChartAndHistogram(loadedSignal);
            } else {
                throw new UnsupportedOperationException(
                        "Signal can not be saved to the file with given extension: " + resultExtension.getExtensions());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML public void onExecuteButton() {
        String operation = (String) signalOperationList.getSelectionModel().getSelectedItem();

        BiFunction<SignalChart, SignalChart, SignalChart> operator;
        //TODO: Extra map
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
            durationInNs = Duration.ofNanos((long) (durationInSeconds * 1_000_000_000L));

            samplingFrequencyInHz = basicSignalChooser.getSamplingFrequencyInHz();

        } catch (NumberFormatException exception) {
            //TODO: Code duplication
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setHeaderText("Input not valid");
            errorAlert.setContentText(exception.getMessage() + "\n" + exception.getCause());
            errorAlert.showAndWait();
            //TODO: Restore default state of the contolls after the crush
            return;
        }

        final Duration USER_SAMPLING_RATE = Duration.ofNanos((long) ((1.0 / samplingFrequencyInHz) * 1_000_000_000));

        SignalChart sc1 = lhs.createChart(durationInNs, USER_SAMPLING_RATE);
        SignalChart sc2 = rhs.createChart(durationInNs, USER_SAMPLING_RATE);
        SignalChart result = operator.apply(sc1, sc2);

        if (lhs.getType() == Signal.Type.CONTINUOUS) {
            //One point in one sample point
            long widthInPixels = (long) chart.getXAxis().getWidth();
            double stepInSeconds = durationInNs.toNanos() / 1_000_000_000D;
            stepInSeconds /= widthInPixels;

            final Duration SAMPLING_RATE = durationInNs.dividedBy(widthInPixels);
            SignalChart signalChart = operator.apply(sc1, sc2);

            chart.setCreateSymbols(false);
            chart.getStyleClass().remove("discrete-signal");
            chart.getStyleClass().add("continuous-signal");
            XYChart.Series series = new XYChart.Series();

            for (int i = 0; i < signalChart.getProbes().size(); i++) {
                double y = signalChart.getProbes().get(i);
                series.getData().add(new XYChart.Data(stepInSeconds * i, y));
            }

            chart.getData().clear();
            chart.getData().add(series);
        } else {
            //            plotDiscreteSignal(generatedSignalChart);
        }

        histogram = new Histogram(result, histogramBins);
        drawHistogram(histogram);
    }

    @FXML public void initialize() {
        //Combo box
        signalOperationList.getItems().addAll(AVAILABLE_SIGNAL_OPERATIONS);

        // We need to hide duration and sampling frequency fields in extraSignalChooser from the client,
        // because those values will be provided by querying basicSignalChooser.
        removeDurationAndSamplingFrequencyFieldsFromExtraSignalChooser();

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

    @FXML public void onHistogramBinsChanged() {
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

    private void drawChartAndHistogram(SignalChart loadedSignal) {
        setTextFields(loadedSignal);
        drawChart(loadedSignal);

        chart.setCreateSymbols(false);
                chart.getStyleClass().remove("discrete-signal");
                chart.getStyleClass().add("continuous-signal");
                XYChart.Series series = new XYChart.Series();

                double step = loadedSignal.getArgs().getSamplingFrequency().toMillis()/1000;
                for (int i = 0; i < loadedSignal.getProbes().size(); i++) {
                    double y = loadedSignal.getProbes().get(i);
                    series.getData().add(new XYChart.Data(step*i, y));
                }


                chart.getData().clear();
                chart.getData().add(series);
                Histogram histogram = new Histogram(loadedSignal, histogramBins);
                drawHistogram(histogram);
                generatedSignalChart = loadedSignal;
//                plotDiscreteSignal(loadedSignal);
                if(loadedSignal.getSignalType().equals(Signal.Type.CONTINUOUS)){
                    chart.setCreateSymbols(false);
                    chart.getStyleClass().remove("discrete-signal");
                    chart.getStyleClass().add("continuous-signal");
                }
                histogram = new Histogram(loadedSignal, histogramBins);
                drawHistogram(histogram);
    }

    private void removeDurationAndSamplingFrequencyFieldsFromExtraSignalChooser() {
        extraSignalChooser.setArrangement(SignalFactory.LINEARLY_DISTRIBUTED_NOISE, SignalChooser.Field.AMPLITUDE);
        extraSignalChooser.setArrangement(SignalFactory.GAUSSIAN_NOISE, SignalChooser.Field.AMPLITUDE);
        extraSignalChooser.setArrangement(SignalFactory.SINUSOIDAL, SignalChooser.Field.AMPLITUDE, SignalChooser.Field.PERIOD,
                SignalChooser.Field.T1);
        extraSignalChooser.setArrangement(SignalFactory.HALF_STRAIGHT_SINUSOIDAL, SignalChooser.Field.AMPLITUDE, SignalChooser.Field.PERIOD,
                SignalChooser.Field.T1);
        extraSignalChooser.setArrangement(SignalFactory.FULL_STRAIGHT_SINUSOIDAL, SignalChooser.Field.AMPLITUDE, SignalChooser.Field.PERIOD,
                SignalChooser.Field.T1);
        extraSignalChooser.setArrangement(SignalFactory.UNIT_STEP, SignalChooser.Field.AMPLITUDE, SignalChooser.Field.T1);
        extraSignalChooser.setArrangement(SignalFactory.RECTANGLE, SignalChooser.Field.AMPLITUDE, SignalChooser.Field.PERIOD,
                SignalChooser.Field.T1, SignalChooser.Field.KW);
        extraSignalChooser.setArrangement(SignalFactory.SYMETRIC_RECTANGLE, SignalChooser.Field.AMPLITUDE, SignalChooser.Field.PERIOD,
                SignalChooser.Field.T1, SignalChooser.Field.KW);
        extraSignalChooser.setArrangement(SignalFactory.TRIANGLE, SignalChooser.Field.AMPLITUDE, SignalChooser.Field.PERIOD,
                SignalChooser.Field.T1, SignalChooser.Field.KW);
        extraSignalChooser.setArrangement(SignalFactory.KRONECKER_DELTA, SignalChooser.Field.AMPLITUDE, SignalChooser.Field.NS);
        extraSignalChooser.setArrangement(SignalFactory.IMPULSE_NOISE, SignalChooser.Field.AMPLITUDE, SignalChooser.Field.PROBABILITY);
    }

    private void setTextFields(SignalChart sc) {
                basicSignalChooser.setSignalChart(sc);
                averageValueLabel.setText(String.format("%.2f", sc.getArgs().getAverageValue()));
                averageAbsoluteValueLabel.setText(String.format("%.2f", sc.getArgs().getAverageAbsoulteValue()));
                averagePowerValueLabel.setText(String.format("%.2f", sc.getArgs().getAveragePowerValue()));
                varianceValueLabel.setText(String.format("%.2f", sc.getArgs().getVarianceValue()));
                effectivePowerValueLabel.setText(String.format("%.2f", sc.getArgs().getEffectivePowerValue()));
    }

}
