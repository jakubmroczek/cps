package cps;

import cps.model.*;
import cps.model.Math;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;

import javafx.scene.chart.*;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.style.Styler;
import org.omg.PortableServer.SERVANT_RETENTION_POLICY_ID;

import java.time.Duration;
import java.util.*;
import java.util.stream.IntStream;

public class MainViewController {

    @FXML private LineChart<Number, Number> chart;

    @FXML
    NumberAxis xAxis;

    @FXML
    NumberAxis yAxis;

    @FXML ComboBox signalList;

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

            Duration _duration = Duration.ofMillis(Integer.parseInt(duration.getText()));

            long widthInPixels = (long) chart.getXAxis().getWidth();

            final Duration MAX_SAMPLING_RATE = _duration.dividedBy(widthInPixels);
       SignalChart sc = signal.createChart(_duration, MAX_SAMPLING_RATE);

            System.out.println(MAX_SAMPLING_RATE);
            System.out.println(sc.getProbes().size());



            chart.setAnimated(false);
            chart.setCreateSymbols(false);
            chart.getStyleClass().add("thick-chart");
            XYChart.Series series = new XYChart.Series();
            series.setName("sinusoida1");

            System.out.println("");
            for (int i =0; i < sc.getProbes().size(); i++) {
                double y = sc.getProbes().get(i);
//                double x = i * sc.getProbingPeriod().toMillis();
                System.out.println(i + " " + y);
//                double x = i / widthInPixels;

                //Mozliwosc przeklamania przez zmiane jednostke
                if (MAX_SAMPLING_RATE.toMillis() != 0) {
                    series.getData().add(new XYChart.Data(MAX_SAMPLING_RATE.multipliedBy(i).toMillis(), y));
                } else {
                    //HOW TO HANDLE THIS?
                    //NANOSECONDS
                    series.getData().add(new XYChart.Data(MAX_SAMPLING_RATE.multipliedBy(i).toNanos(), y));
                }

            }

            chart.getData().clear();

            chart.getData().add(series);

            Histogram histogram = new Histogram(sc, histogramBins);
            drawHistogram(histogram);

            System.out.println(chart.getXAxis().getWidth());

            // TODO: !!!!Pamietaj zeby odciac nadmiarowy czas

            double averageValue = Math.averageValue(signal, Duration.ZERO, _duration);
            averageValueTextField.setText(String.format("%.2f", averageValue));

            double averageAbsoulteValue = Math.averageAbsoluteValue(signal, Duration.ZERO, _duration);
            averageAbsoluteValueTextField.setText(String.format("%.2f", averageAbsoulteValue));

   }

   @FXML
   public void onSignalChoice() {
       signal = (String)signalList.getSelectionModel().getSelectedItem();
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

        SignalArgs args = new SignalArgs(_amplitude, _period,_initialTime, kw);

        String signalType = labelsToSignalsMap.get(signal);
        return SignalFactory.createSignal(signalType, args);
    }

    //Moze byc tylko wykonywane na watku GUI (wewnatrz metody z annotacja @FXML lub Platform.runLater), w przeciwnym razie crashe
    private void drawHistogram(Histogram histogram) {
//        histogcramChart.setCreateSymbols(false);

        histogramChart.setCategoryGap(0);
        histogramChart.setBarGap(0);
       histogramChart.setAnimated(false);
//        histogramChart.getXAxis().
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
