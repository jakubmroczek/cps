package cps;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        currentSignal = signal;

        Duration _duration = Duration.ofMillis(Integer.parseInt(duration.getText()));
        SignalChart sc = signal.createChart(_duration, Duration.ofMillis(1));

        chart.setTitle("Wykres sinusoidalny");
        chart.setCreateSymbols(false);
        chart.getStyleClass().add("thick-chart");
        XYChart.Series series = new XYChart.Series();
        series.setName("sinusoida1");

        for (int i =0; i < sc.getProbes().size(); i++) {
            double y = sc.getProbes().get(i);
            double x = i * sc.getProbingPeriod().toMillis();
            series.getData().add(new XYChart.Data(x, y));
        }

        chart.getData().clear();

        chart.getData().add(series);
    }

    public void drawChartBasedOnFile(List<String> lines) {
        Signal signal = createSignal();
        currentSignal = signal;

        Duration _duration = Duration.ofMillis(Integer.parseInt(duration.getText()));
        SignalChart sc = signal.createChart(_duration, Duration.ofMillis(1));

        chart.setTitle("Wykres sinusoidalny");
        chart.setCreateSymbols(false);
        chart.getStyleClass().add("thick-chart");
        XYChart.Series series = new XYChart.Series();
        series.setName("sinusoida1");
        Duration time = Duration.ofNanos(0);
        Duration probing = Duration.ofMillis(1);

        for(String s: lines){

            double x = time.toMillis();
            series.getData().add(new XYChart.Data(x,Double.parseDouble(s)));
            time = time.plus(probing);
        }

        chart.getData().clear();

        chart.getData().add(series);
    }
    @FXML
    private void saveToFile(ActionEvent e) {
        FileChooser.ExtensionFilter fcExtension = new FileChooser.ExtensionFilter("Txt Files", "*.txt");

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Zapisz sygnał");
        fileChooser.getExtensionFilters().add(fcExtension);
        File file = fileChooser.showSaveDialog(this.stage);
        if (file == null)
            return;
        List<String> lines = new ArrayList<>();
        lines.add(String.valueOf(signalList.getSelectionModel().getSelectedIndex()));
        lines.add(amplitude.getText());
        lines.add(period.getText());
        lines.add(initialTime.getText());
        lines.add(duration.getText());
        lines.add(kw.getText());
        for (Double d: currentSignal.signalSamples) {
            lines.add(d.toString());
        }
        try {
            Files.write(file.toPath(),lines, Charset.forName("UTF-8"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void loadFromFile(ActionEvent e) {
        FileChooser.ExtensionFilter fcExtension = new FileChooser.ExtensionFilter("Txt Files", "*.txt");

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Wczytaj sygnał");
        fileChooser.getExtensionFilters().add(fcExtension);
        File file = fileChooser.showOpenDialog(this.stage);
        if (file == null)
            return;

        try {
            List<String> lines = Files.readAllLines(file.toPath());
            signalList.getSelectionModel().select(signalList.getItems().get(Integer.parseInt(lines.get(0))));
            amplitude.setText(lines.get(1));
            period.setText(lines.get(2));
            initialTime.setText(lines.get(3));
            duration.setText(lines.get(4));
            kw.setText(lines.get(5));
            drawChartBasedOnFile(lines.subList(6,lines.size()));

        } catch (IOException ex) {
            ex.printStackTrace();
        }
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
}
