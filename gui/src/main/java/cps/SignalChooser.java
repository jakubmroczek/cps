package cps;

import cps.model.Signal;
import cps.model.SignalArgs;
import cps.model.SignalChart;
import cps.model.SignalFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SignalChooser extends VBox {

    private static final ObservableList<String> AVAILABLE_SIGNALS = FXCollections.observableArrayList("Szum o rozkładzie jednostajnym",
            "Szum gaussowski", "Sygnał sinusoidalny", "Sygnał sinusoidalny wyprostowany jednopołówkowo",
            "Sygnał sinusoidalny wyprsotowany dwupołówkowo", "Sygnał prostokątny", "Sygnał prostokątny symetryczny", "Sygnał trójkątny",
            "Skok jednostkowy", "Impuls jednostkowy", "Szum impulsowy");

    private static final Map<String, String> LABEL_TO_SIGNAL_MAP = new HashMap<>();

    static {
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

    private final Map<String, Runnable> signalNameToSignalParametersLayoutMap = new HashMap<>();

    @FXML private ComboBox<String> signalList;

    //TODO: Getter is vary bad, cause shows implementation details -> provide custom mechanism allowing ordering this fileds in a custom way.
    @FXML
    private SignalParameter amplitudeSignalParameter, periodSignalParameter, t1SignalParameter, durationSignalParameter, kwSignalParameter, nsSignalParameter, samplingFrequencySignalParameter, probabilitySignalParameter;

    public SignalChooser() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/SignalChooser.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        signalList.getItems().addAll(AVAILABLE_SIGNALS);

        initializeSignalParameters();
        initializeLayout();

        //Default value
        signalList.setValue(AVAILABLE_SIGNALS.get(3));

    }

    @FXML public void onSignalChosen() {
        String selection = signalList.getSelectionModel().getSelectedItem();
        String selectedSignal = LABEL_TO_SIGNAL_MAP.get(selection);
        Runnable layoutRearrangement = signalNameToSignalParametersLayoutMap.get(selectedSignal);
        if (layoutRearrangement != null) {
            layoutRearrangement.run();
        }
    }

    /**
     * Creates Signal assembled from user parameters.
     */
    public Signal getSignal() throws IllegalArgumentException {
        try {
            SignalArgs args = getSignalArgs();
            String selection = signalList.getSelectionModel().getSelectedItem();
            String signalType = LABEL_TO_SIGNAL_MAP.get(selection);
            return SignalFactory.createSignal(signalType, args);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(exception);
        }
    }

    public SignalArgs getSignalArgs() throws IllegalArgumentException {
        String name = signalList.getSelectionModel().getSelectedItem();

        double amplitude = Double.parseDouble(amplitudeSignalParameter.getParameterValue().getText());

        double periodInSeconds = Double.valueOf(periodSignalParameter.getParameterValue().getText());
        Duration periodInNs = Duration.ofNanos((long) (periodInSeconds * 1_000_000_000L));

        double initialTimeInSeconds = Double.valueOf(t1SignalParameter.getParameterValue().getText());
        Duration initialTimeInNs = Duration.ofNanos((long) (initialTimeInSeconds * 1_000_000_000L));

        double samplingFrequencyInHz = Double.valueOf(samplingFrequencySignalParameter.getParameterValue().getText());
        Duration samplingFrequencyInNs = Duration.ofNanos((long) ((1.0 / samplingFrequencyInHz) * 1_000_000_000));

        int ns = Integer.parseInt(nsSignalParameter.getParameterValue().getText());

        double probability = Double.parseDouble(probabilitySignalParameter.getParameterValue().getText());

        //Check if the value is in range
        double kw = Double.parseDouble(kwSignalParameter.getParameterValue().getText());

        return SignalArgs.builder()
                                    .signalName(name)
                                    .amplitude(amplitude)
                                    .period(periodInNs)
                                    .initialTime(initialTimeInNs)
                                    .kw(kw)
                                    .Ns(ns)
                                    .probability(probability)
                                    .samplingFrequency(samplingFrequencyInNs)
                                    .build();
    }

    public double getDurationInSeconds() throws IllegalArgumentException {
        try {
            return Double.valueOf(durationSignalParameter.getParameterValue().getText());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public long getSamplingFrequencyInHz() throws IllegalArgumentException {
        try {
            return Long.parseLong(samplingFrequencySignalParameter.getParameterValue().getText());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Use signal constants from SignalFactory.
     */
    public void setArrangement(String signal, Field... field) {
        if (signalNameToSignalParametersLayoutMap.containsKey(signal)) {
            signalNameToSignalParametersLayoutMap.remove(signal);
            Runnable layoutRearrangement = () -> {
                removeAllSignalParameters();
                Arrays.stream(field).forEach(x -> getChildren().add(map(x)));
            };
            signalNameToSignalParametersLayoutMap.put(signal, layoutRearrangement);
        }
    }

    public void setSignalChart(SignalChart signalChart) {
        signalList.getSelectionModel().select(AVAILABLE_SIGNALS.indexOf(signalChart.getArgs().getSignalName()));
        Runnable layoutRearrangement = signalNameToSignalParametersLayoutMap.get(signalChart.getArgs().getSignalName());
        if (layoutRearrangement != null) {
            layoutRearrangement.run();
        }
        amplitudeSignalParameter.getParameterValue().setText(String.valueOf(signalChart.getArgs().getAmplitude()));
        periodSignalParameter.getParameterValue().setText(String.valueOf(signalChart.getArgs().getPeriod().toMillis()));
        t1SignalParameter.getParameterValue().setText(String.valueOf(signalChart.getArgs().getInitialTime().toMillis()));
        durationSignalParameter.getParameterValue().setText(String.valueOf(signalChart.getDuration().toMillis()/1000));
        kwSignalParameter.getParameterValue().setText(String.valueOf(signalChart.getArgs().getKw()));
        nsSignalParameter.getParameterValue().setText(String.valueOf(signalChart.getArgs().getNs()));
        samplingFrequencySignalParameter.getParameterValue().setText(String.valueOf(signalChart.getArgs().getSamplingFrequency().toMillis()/10));
        probabilitySignalParameter.getParameterValue().setText(String.valueOf(signalChart.getArgs().getProbability()));
    }

    private void initializeSignalParameters() {
        amplitudeSignalParameter.getParameterName().setText("Amplituda: ");
        amplitudeSignalParameter.getParameterValue().setText("10.0");

        periodSignalParameter.getParameterName().setText("Okres: ");
        periodSignalParameter.getParameterValue().setText("1");

        t1SignalParameter.getParameterName().setText("t1: ");
        t1SignalParameter.getParameterValue().setText("0");

        durationSignalParameter.getParameterName().setText("Czas: ");
        durationSignalParameter.getParameterValue().setText("8");

        kwSignalParameter.getParameterName().setText("kw: ");
        kwSignalParameter.getParameterValue().setText("0.5");

        nsSignalParameter.getParameterName().setText("ns");
        nsSignalParameter.getParameterValue().setText("5");

        samplingFrequencySignalParameter.getParameterName().setText("Czs. prb [Hz]");
        samplingFrequencySignalParameter.getParameterValue().setText("25");

        probabilitySignalParameter.getParameterName().setText("Prawd.");
        probabilitySignalParameter.getParameterValue().setText("0.5");
    }

    private void initializeLayout() {
        Runnable layoutRearrangement0 = () -> {
            removeAllSignalParameters();
            getChildren().add(amplitudeSignalParameter);
            getChildren().add(durationSignalParameter);
            getChildren().add(samplingFrequencySignalParameter);
        };

        Runnable layoutRearrangement1 = () -> {
            removeAllSignalParameters();
            getChildren().add(amplitudeSignalParameter);
            getChildren().add(periodSignalParameter);
            getChildren().add(t1SignalParameter);
            getChildren().add(durationSignalParameter);
            getChildren().add(samplingFrequencySignalParameter);
        };

        Runnable layoutRearrangement2 = () -> {
            layoutRearrangement1.run();
            getChildren().add(kwSignalParameter);
        };

        Runnable layoutRearrangement3 = () -> {
            layoutRearrangement0.run();
            getChildren().add(nsSignalParameter);
        };

        Runnable layoutRearrangement4 = () -> {
            layoutRearrangement0.run();
            getChildren().add(probabilitySignalParameter);
        };

        signalNameToSignalParametersLayoutMap.put(SignalFactory.LINEARLY_DISTRIBUTED_NOISE, layoutRearrangement0);
        signalNameToSignalParametersLayoutMap.put(SignalFactory.GAUSSIAN_NOISE, layoutRearrangement0);
        signalNameToSignalParametersLayoutMap.put(SignalFactory.SINUSOIDAL, layoutRearrangement1);
        signalNameToSignalParametersLayoutMap.put(SignalFactory.HALF_STRAIGHT_SINUSOIDAL, layoutRearrangement1);
        signalNameToSignalParametersLayoutMap.put(SignalFactory.FULL_STRAIGHT_SINUSOIDAL, layoutRearrangement1);

        signalNameToSignalParametersLayoutMap.put(SignalFactory.UNIT_STEP, () -> {
            removeAllSignalParameters();
            getChildren().add(amplitudeSignalParameter);
            getChildren().add(t1SignalParameter);
            getChildren().add(durationSignalParameter);
            getChildren().add(samplingFrequencySignalParameter);

        });

        signalNameToSignalParametersLayoutMap.put(SignalFactory.RECTANGLE, layoutRearrangement2);
        signalNameToSignalParametersLayoutMap.put(SignalFactory.SYMETRIC_RECTANGLE, layoutRearrangement2);
        signalNameToSignalParametersLayoutMap.put(SignalFactory.TRIANGLE, layoutRearrangement2);

        signalNameToSignalParametersLayoutMap.put(SignalFactory.KRONECKER_DELTA, layoutRearrangement3);

        signalNameToSignalParametersLayoutMap.put(SignalFactory.IMPULSE_NOISE, layoutRearrangement4);
    }

    //TODO: Group it somehow usign java fx method and delete this workaround
    private void removeAllSignalParameters() {
        getChildren().removeAll(amplitudeSignalParameter, periodSignalParameter, t1SignalParameter, durationSignalParameter,
                kwSignalParameter, nsSignalParameter, samplingFrequencySignalParameter, probabilitySignalParameter);
    }

    public SignalParameter map(Field field) {
        switch (field) {
            case AMPLITUDE:
                return amplitudeSignalParameter;
            case PERIOD:
                return periodSignalParameter;
            case T1:
                return t1SignalParameter;
            case DURATION:
                return durationSignalParameter;
            case KW:
                return kwSignalParameter;
            case NS:
                return nsSignalParameter;
            case SAMPLING_FREQUENCY:
                return samplingFrequencySignalParameter;
            case PROBABILITY:
                return probabilitySignalParameter;

            default:
                throw new IllegalArgumentException("Unknwon field " + field);
        }
    }

    public enum Field {
        AMPLITUDE, PERIOD, T1, DURATION, KW, NS, SAMPLING_FREQUENCY, PROBABILITY
    }
}
