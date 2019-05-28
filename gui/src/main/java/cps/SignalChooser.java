package cps;

import cps.model.FunctionFactory;
import cps.model.Signal;
import cps.model.SignalArgs;
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
import java.util.function.Function;

public class SignalChooser extends VBox {

    private static final ObservableList<String> AVAILABLE_SIGNALS = FXCollections.observableArrayList("Szum o rozkładzie jednostajnym",
            "Szum gaussowski", "Sygnał sinusoidalny", "Sygnał sinusoidalny wyprostowany jednopołówkowo",
            "Sygnał sinusoidalny wyprsotowany dwupołówkowo", "Sygnał prostokątny", "Sygnał prostokątny symetryczny", "Sygnał trójkątny",
            "Skok jednostkowy", "Impuls jednostkowy", "Szum impulsowy", "Funkcja liniowa");

    private static final Map<String, String> LABEL_TO_SIGNAL_MAP = new HashMap<>();

    static {
        LABEL_TO_SIGNAL_MAP.put(AVAILABLE_SIGNALS.get(0), FunctionFactory.LINEARLY_DISTRIBUTED_NOISE);
        LABEL_TO_SIGNAL_MAP.put(AVAILABLE_SIGNALS.get(1), FunctionFactory.GAUSSIAN_NOISE);
        LABEL_TO_SIGNAL_MAP.put(AVAILABLE_SIGNALS.get(2), FunctionFactory.SINUSOIDAL);
        LABEL_TO_SIGNAL_MAP.put(AVAILABLE_SIGNALS.get(3), FunctionFactory.HALF_STRAIGHT_SINUSOIDAL);
        LABEL_TO_SIGNAL_MAP.put(AVAILABLE_SIGNALS.get(4), FunctionFactory.FULL_STRAIGHT_SINUSOIDAL);
        LABEL_TO_SIGNAL_MAP.put(AVAILABLE_SIGNALS.get(5), FunctionFactory.RECTANGLE);
        LABEL_TO_SIGNAL_MAP.put(AVAILABLE_SIGNALS.get(6), FunctionFactory.SYMETRIC_RECTANGLE);
        LABEL_TO_SIGNAL_MAP.put(AVAILABLE_SIGNALS.get(7), FunctionFactory.TRIANGLE);
        LABEL_TO_SIGNAL_MAP.put(AVAILABLE_SIGNALS.get(8), FunctionFactory.UNIT_STEP);
        LABEL_TO_SIGNAL_MAP.put(AVAILABLE_SIGNALS.get(9), FunctionFactory.KRONECKER_DELTA);
        LABEL_TO_SIGNAL_MAP.put(AVAILABLE_SIGNALS.get(10), FunctionFactory.IMPULSE_NOISE);
        LABEL_TO_SIGNAL_MAP.put(AVAILABLE_SIGNALS.get(11), FunctionFactory.LINEAR);
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
    public Function<Double, Double> creatFunction() throws IllegalArgumentException {
        try {
            SignalArgs args = getSignalArgs();
            String selection = signalList.getSelectionModel().getSelectedItem();
            String function = LABEL_TO_SIGNAL_MAP.get(selection);
            return FunctionFactory.createFunction(function, args);
        } catch (Exception exception) {
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
                         .periodInNs(periodInNs.toNanos())
                         .initialTimeInNs(initialTimeInNs.toNanos())
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

    public Signal.Type getSignalType() {
        String name = signalList.getSelectionModel().getSelectedItem();
        name = LABEL_TO_SIGNAL_MAP.get(name);

        if (name.equals(FunctionFactory.KRONECKER_DELTA) || name.equals(FunctionFactory.IMPULSE_NOISE)) {
            return Signal.Type.DISCRETE;
        } else {
            return Signal.Type.CONTINUOUS;
        }
    }

    /**
     * Use signal constants from FunctionFactory.
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

    public void displaySignal(Signal signal, String title) {
        removeAllSignalParameters();
        getChildren().add(durationSignalParameter);
        getChildren().add(samplingFrequencySignalParameter);
        durationSignalParameter.getParameterValue().setText(signal.getDurationInNs().toString());
        samplingFrequencySignalParameter.getParameterValue().setText(signal.getSamplingPeriod().toString());
        signalList.setValue(title);
    }

    private void initializeSignalParameters() {
        amplitudeSignalParameter.getParameterName().setText("Amplituda: ");
        amplitudeSignalParameter.getParameterValue().setText("1.0");

        periodSignalParameter.getParameterName().setText("Okres: ");
        periodSignalParameter.getParameterValue().setText("0.05");

        t1SignalParameter.getParameterName().setText("t1: ");
        t1SignalParameter.getParameterValue().setText("0");

        durationSignalParameter.getParameterName().setText("Czas: ");
        durationSignalParameter.getParameterValue().setText("0.1");

        kwSignalParameter.getParameterName().setText("kw: ");
        kwSignalParameter.getParameterValue().setText("0.5");

        nsSignalParameter.getParameterName().setText("ns");
        nsSignalParameter.getParameterValue().setText("5");

        samplingFrequencySignalParameter.getParameterName().setText("Czs. prb [Hz]");
        samplingFrequencySignalParameter.getParameterValue().setText("1200");

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

        signalNameToSignalParametersLayoutMap.put(FunctionFactory.LINEARLY_DISTRIBUTED_NOISE, layoutRearrangement0);
        signalNameToSignalParametersLayoutMap.put(FunctionFactory.GAUSSIAN_NOISE, layoutRearrangement0);
        signalNameToSignalParametersLayoutMap.put(FunctionFactory.SINUSOIDAL, layoutRearrangement1);
        signalNameToSignalParametersLayoutMap.put(FunctionFactory.HALF_STRAIGHT_SINUSOIDAL, layoutRearrangement1);
        signalNameToSignalParametersLayoutMap.put(FunctionFactory.FULL_STRAIGHT_SINUSOIDAL, layoutRearrangement1);

        signalNameToSignalParametersLayoutMap.put(FunctionFactory.UNIT_STEP, () -> {
            removeAllSignalParameters();
            getChildren().add(amplitudeSignalParameter);
            getChildren().add(t1SignalParameter);
            getChildren().add(durationSignalParameter);
            getChildren().add(samplingFrequencySignalParameter);

        });

        signalNameToSignalParametersLayoutMap.put(FunctionFactory.RECTANGLE, layoutRearrangement2);
        signalNameToSignalParametersLayoutMap.put(FunctionFactory.SYMETRIC_RECTANGLE, layoutRearrangement2);
        signalNameToSignalParametersLayoutMap.put(FunctionFactory.TRIANGLE, layoutRearrangement2);

        signalNameToSignalParametersLayoutMap.put(FunctionFactory.KRONECKER_DELTA, layoutRearrangement3);

        signalNameToSignalParametersLayoutMap.put(FunctionFactory.IMPULSE_NOISE, layoutRearrangement4);
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
