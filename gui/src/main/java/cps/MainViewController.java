package cps;

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

import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class MainViewController {

    private Stage stage;
    Signal currentSignal;
    @FXML private LineChart<Number, Number> chart;

    @FXML
    NumberAxis xAxis;

    @FXML
    NumberAxis yAxis;

    @FXML ComboBox signalList;

    @FXML
    private TextField amplitude, period, initialTime, duration, kw;

   @FXML
    public void display() {
        Signal signal = createSignal();
        signal.setDuration(Double.valueOf(duration.getText()));
        signal.setSignalType(signalList.getSelectionModel().getSelectedItem().toString());
        currentSignal = signal;

        Duration _duration = Duration.ofMillis(Integer.parseInt(duration.getText()));
        SignalChart sc = signal.createChart(_duration, Duration.ofMillis(1));

        chart.setTitle("Wykres sinusoidalny");
        chart.setCreateSymbols(false);
        chart.getStyleClass().add("thick-chart");
        XYChart.Series series = new XYChart.Series();
        series.setName("sinusoida1");

//        for (int i =0; i < sc.getProbes().size(); i++) {
//            double y = sc.getProbes().get(i);
//            double x = i * sc.getProbingPeriod().toMillis();
//            series.getData().add(new XYChart.Data(x, y));
//        }
       signal.getSignalValues().forEach(
               (k, v) -> {
                   series.getData().add(new XYChart.Data<>(k, v));
               });

        chart.getData().clear();

        chart.getData().add(series);
    }

    public void drawChartBasedOnFile(Signal s) {

        chart.setTitle(s.getSignalType());
        chart.setCreateSymbols(false);
        chart.getStyleClass().add("thick-chart");
        XYChart.Series series = new XYChart.Series();
        series.setName("sinusoida1");

        s.getSignalValues().forEach(
                (k, v) -> {
                    series.getData().add(new XYChart.Data<>(k, v));
                });

        chart.getData().clear();

        chart.getData().add(series);
    }
    @FXML
    private void saveToFile(ActionEvent e) {
        FileChooser.ExtensionFilter fcExtension = new FileChooser.ExtensionFilter("JSON Files", "*.json");

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Zapisz sygnał");
        fileChooser.getExtensionFilters().add(fcExtension);
        File file = fileChooser.showSaveDialog(this.stage);
        if (file == null)
            return;
        Gson gson = new Gson();
        String signalJson = gson.toJson(currentSignal);

        try {
            Files.write(file.toPath(), signalJson.getBytes());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void loadFromFile(ActionEvent e) {
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
            Signal loadedSignal = gson.fromJson(reader, Signal.class);
            signalList.getSelectionModel().select(AVALIABLE_SIGNALS.indexOf(loadedSignal.getSignalType()));

            amplitude.setText(String.valueOf((int)loadedSignal.getAmplitude()));
            period.setText(String.valueOf((int)loadedSignal.getPeriod()));
            initialTime.setText(String.valueOf((int)loadedSignal.getInitialTime()));
            duration.setText(String.valueOf((int)loadedSignal.getDuration()));
            drawChartBasedOnFile(loadedSignal);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    @FXML
    private void addSignals(ActionEvent e) {

        Signal s1 = loadSignal("1");
        Signal s2 = loadSignal("2");
        Signal newSignal = addSignals(s1,s2);
        currentSignal = newSignal;
        saveToFile(null);
        drawChartBasedOnFile(newSignal);
    }
    @FXML
    private void subtractSignals(ActionEvent e) {

        Signal s1 = loadSignal("1");
        Signal s2 = loadSignal("2");
        Signal newSignal = subtractSignals(s1,s2);
        currentSignal = newSignal;
        saveToFile(null);
        drawChartBasedOnFile(newSignal);
    }
    @FXML
    private void multiplySignals(ActionEvent e) {

        Signal s1 = loadSignal("1");
        Signal s2 = loadSignal("2");
        Signal newSignal = multiplySignals(s1,s2);
        currentSignal = newSignal;
        saveToFile(null);
        drawChartBasedOnFile(newSignal);
    }
    @FXML
    private void divideSignals(ActionEvent e) {

        Signal s1 = loadSignal("1");
        Signal s2 = loadSignal("2");
        Signal newSignal = divideSignals(s1,s2);
        currentSignal = newSignal;
        saveToFile(null);
        drawChartBasedOnFile(newSignal);
    }
    private Signal loadSignal(String sygnal){
        FileChooser.ExtensionFilter fcExtension = new FileChooser.ExtensionFilter("JSON Files", "*.json");

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Wczytaj sygnał "+ sygnal);
        fileChooser.getExtensionFilters().add(fcExtension);
        File file = fileChooser.showOpenDialog(this.stage);
        if (file == null)
            return null;

        try {
            Gson gson = new Gson();
            JsonReader reader = new JsonReader(new FileReader(file));
            Signal s1 = gson.fromJson(reader, Signal.class);
            signalList.getSelectionModel().select(AVALIABLE_SIGNALS.indexOf(s1.getSignalType()));

            amplitude.setText(String.valueOf((int)s1.getAmplitude()));
            period.setText(String.valueOf((int)s1.getPeriod()));
            initialTime.setText(String.valueOf((int)s1.getInitialTime()));
            duration.setText(String.valueOf((int)s1.getDuration()));
            drawChartBasedOnFile(s1);
            return s1;
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return null;
    }

   private Signal createSignal() {
       //TODO: Error handling
       double _amplitude = Double.parseDouble(amplitude.getText());
       Duration _period = Duration.ofMillis(Integer.parseInt(period.getText()));
       Duration _initialTime = Duration.ofMillis(Integer.parseInt(initialTime.getText()));
       //TODO: connect to fxml object
       double kw = 0.5;

       SignalArgs args = new SignalArgs(_amplitude, _period,_initialTime, kw);

       String signalType = labelsToSignalsMap.get(signal);
       return SignalFactory.createSignal(signalType, args);
   }

   @FXML
   public void onSignalChoice() {
       signal = (String)signalList.getSelectionModel().getSelectedItem();
   }

   @FXML
   public void initialize() {
       signalList.getItems().addAll(AVALIABLE_SIGNALS);
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
        labelsToSignalsMap.put(AVALIABLE_SIGNALS.get(1), SignalFactory.GAUSSIAN_NOISE);
        labelsToSignalsMap.put(AVALIABLE_SIGNALS.get(2), SignalFactory.SINUSOIDAL);
        labelsToSignalsMap.put(AVALIABLE_SIGNALS.get(3), SignalFactory.HALF_STRAIGHT_SINUSOIDAL);
        labelsToSignalsMap.put(AVALIABLE_SIGNALS.get(4), SignalFactory.FULL_STRAIGHT_SINUSOIDAL);
        labelsToSignalsMap.put(AVALIABLE_SIGNALS.get(5), SignalFactory.RECTANGLE);
        labelsToSignalsMap.put(AVALIABLE_SIGNALS.get(6), SignalFactory.SYMETRIC_RECTANGLE);
        labelsToSignalsMap.put(AVALIABLE_SIGNALS.get(7), SignalFactory.TRIANGLE);
    }
   private String signal;

    private Signal addSignals(Signal s1, Signal s2) {
        TreeMap<Double, Double> map = new TreeMap<>();

        s1.getSignalValues().forEach(
                (k, v) -> {
                    map.put(k, v);
                }
        );
        s2.getSignalValues().forEach(
                (k, v) -> {
                    if (map.get(k) != null) {
                        map.put(k, v + map.get(k));
                    }
                    else map.put(k, v);
                }
        );

        String name = s1.getSignalType() + " + " + s2.getSignalType();
        Signal newSignal = new Signal(null);
        newSignal.setSignalType(name);
        newSignal.setArgs(s1.getAmplitude(),s1.getPeriod(),s1.getInitialTime());
        newSignal.setDuration(s1.getDuration());
        newSignal.setSignalValues(map);
        return newSignal;
    }
    private Signal subtractSignals(Signal s1, Signal s2) {
        TreeMap<Double, Double> map = new TreeMap<>();

        s1.getSignalValues().forEach(
                (k, v) -> {
                    map.put(k, v);
                }
        );
        s2.getSignalValues().forEach(
                (k, v) -> {
                    if (map.get(k) != null) {
                        map.put(k, v - map.get(k));
                    }
                    else map.put(k, v);
                }
        );

        String name = s1.getSignalType() + " - " + s2.getSignalType();
        Signal newSignal = new Signal(null);
        newSignal.setSignalType(name);
        newSignal.setArgs(s1.getAmplitude(),s1.getPeriod(),s1.getInitialTime());
        newSignal.setDuration(s1.getDuration());
        newSignal.setSignalValues(map);
        return newSignal;
    }
    private Signal multiplySignals(Signal s1, Signal s2) {
        TreeMap<Double, Double> map = new TreeMap<>();

        s1.getSignalValues().forEach(
                (k, v) -> {
                    map.put(k, v);
                }
        );
        s2.getSignalValues().forEach(
                (k, v) -> {
                    if (map.get(k) != null) {
                        map.put(k, v * map.get(k));
                    }
                    else map.put(k, v);
                }
        );

        String name = s1.getSignalType() + " * " + s2.getSignalType();
        Signal newSignal = new Signal(null);
        newSignal.setSignalType(name);
        newSignal.setArgs(s1.getAmplitude(),s1.getPeriod(),s1.getInitialTime());
        newSignal.setDuration(s1.getDuration());
        newSignal.setSignalValues(map);
        return newSignal;
    }
    private Signal divideSignals(Signal s1, Signal s2) {
        TreeMap<Double, Double> map = new TreeMap<>();

        s1.getSignalValues().forEach(
                (k, v) -> {
                    map.put(k, v);
                }
        );
        s2.getSignalValues().forEach(
                (k, v) -> {
                    if (map.get(k) != null) {
                        map.put(k, v / map.get(k));
                    }
                    else map.put(k, v);
                }
        );

        String name = s1.getSignalType() + " / " + s2.getSignalType();
        Signal newSignal = new Signal(null);
        newSignal.setSignalType(name);
        newSignal.setArgs(s1.getAmplitude(),s1.getPeriod(),s1.getInitialTime());
        newSignal.setDuration(s1.getDuration());
        newSignal.setSignalValues(map);
        return newSignal;
    }
}
