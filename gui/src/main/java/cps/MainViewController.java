package cps;

import cps.model.Signal;
import cps.model.SignalArgs;
import cps.model.SignalChart;
import cps.model.SignalFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;

import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class MainViewController {

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
