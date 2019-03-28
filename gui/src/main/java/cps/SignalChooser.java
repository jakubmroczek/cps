package cps;

import cps.model.Signal;
import cps.model.SignalArgs;
import cps.model.SignalFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import javafx.util.Pair;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class SignalChooser extends VBox {

    private static final ObservableList<String> AVAILABLE_SIGNALS = FXCollections.observableArrayList(
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

    private static final Map<String, String> LABEL_TO_SIGNAL_MAP = new HashMap<>();
    static
    {
        LABEL_TO_SIGNAL_MAP.put(AVAILABLE_SIGNALS.get(0), SignalFactory.LINEARLY_DISTRIBUTED_NOISE);
        LABEL_TO_SIGNAL_MAP.put(AVAILABLE_SIGNALS.get(1), SignalFactory.GAUSSIAN_NOISE);
        LABEL_TO_SIGNAL_MAP.put(AVAILABLE_SIGNALS.get(2), SignalFactory.SINUSOIDAL);
        LABEL_TO_SIGNAL_MAP.put(AVAILABLE_SIGNALS.get(3), SignalFactory.HALF_STRAIGHT_SINUSOIDAL);
        LABEL_TO_SIGNAL_MAP.put(AVAILABLE_SIGNALS.get(4), SignalFactory.FULL_STRAIGHT_SINUSOIDAL);
        LABEL_TO_SIGNAL_MAP.put(AVAILABLE_SIGNALS.get(5), SignalFactory.RECTANGLE);
        LABEL_TO_SIGNAL_MAP.put(AVAILABLE_SIGNALS.get(6), SignalFactory.SYMETRIC_RECTANGLE);
        LABEL_TO_SIGNAL_MAP.put(AVAILABLE_SIGNALS.get(7), SignalFactory.TRIANGLE);
        LABEL_TO_SIGNAL_MAP.put(AVAILABLE_SIGNALS.get(8), SignalFactory.UNIT_STEP);
        LABEL_TO_SIGNAL_MAP.put(AVAILABLE_SIGNALS.get(9), SignalFactory.KRONECKER_DELTA);
        LABEL_TO_SIGNAL_MAP.put(AVAILABLE_SIGNALS.get(10), SignalFactory.IMPULSE_NOISE);
    }

    @FXML
    private ComboBox<String> signalLists;

    public SignalChooser() {
        signalLists.getItems().addAll(AVAILABLE_SIGNALS);
    }

    /**
     * Creates Signal assembled from user parameters.
     */
    public Signal getSignal() throws IllegalArgumentException {
//        try {
//            double amplitude = Double.parseDouble(amplitudeSignalParameter.getParameterValue().getText());
//            double periodInSeconds = Double.valueOf(periodSignalParameter.getParameterValue().getText());
//            Duration periodInNs = Duration.ofNanos((long)(periodInSeconds * 1_000_000_000L));
//            double initialTimeInSeconds = Double.valueOf(t1SignalParameter.getParameterValue().getText());
//            Duration initialTimeInNs = Duration.ofNanos((long)(initialTimeInSeconds * 1_000_000_000L));
//            int ns = Integer.parseInt(nsSignalParameter.getParameterValue().getText());
//            double probability = Double.parseDouble(probabilitySignalParameter.getParameterValue().getText());
//
//            //TODO: connect to fxml object
//            //Check if the value is in range
//            double kw = Double.parseDouble(kwSignalParameter.getParameterValue().getText());
//
//            SignalArgs args = SignalArgs.builder().amplitude(amplitude).period(periodInNs).initialTime(initialTimeInNs).kw(kw).Ns(ns).probability(probability).build();
//
//            String signalType = labelsToSignalsMap.get(signal);
//            return SignalFactory.createSignal(signalType, args);
//        } catch (NumberFormatException exception) {
//            throw exception;
//        }
        return null;
    }

    public double getDurationInSeconds() throws IllegalArgumentException {
        return 0.0;
    }

    public double getSamplingFrequencyInHz() throws IllegalArgumentException {
        return 0.0;
    }


}
