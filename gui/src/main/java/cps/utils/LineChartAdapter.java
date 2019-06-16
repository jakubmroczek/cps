package cps.utils;

import cps.model.Signal;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import org.apache.commons.math3.complex.Complex;

import java.util.List;

import static cps.util.Conversions.toFrequency;
import static java.lang.Math.atan;
import static java.lang.Math.min;
import static java.util.stream.Collectors.toList;

public class LineChartAdapter {

    private final LineChart<Number, Number> chart;

    public LineChartAdapter(LineChart<Number, Number> chart) {
        this.chart = chart;
        this.chart.setAnimated(false);
        this.chart.setLegendVisible(false);
    }

    public void plot(Signal<Double> signal) {
        double singlePointDurationInSeconds = getXDistanceBetweenSignalPoints(signal);
        double signalPointsIndexStep = getSignalPointsIndexStep(signal);
        plot(signal.getSamples(), signalPointsIndexStep, singlePointDurationInSeconds);
    }

    public void plotRe(Signal<Complex> signal) {
        double xStep = getXStepInFrequencyDomain(signal);
        double signalPointsIndexStep = getSignalPointsIndexStep1(signal);
        List<Double> reParts = signal.getSamples().stream().map(Complex::getReal).collect(toList());
        plot(reParts, signalPointsIndexStep, xStep);
    }

    public void plotIm(Signal<Complex> signal) {
        double xStep = getXStepInFrequencyDomain(signal);
        double signalPointsIndexStep = getSignalPointsIndexStep1(signal);
        List<Double> imParts = signal.getSamples().stream().map(Complex::getImaginary).collect(toList());
        plot(imParts, signalPointsIndexStep, xStep);
    }

    public void plotModule(Signal<Complex> signal) {
        double xStep = getXStepInFrequencyDomain(signal);
        double signalPointsIndexStep = getSignalPointsIndexStep1(signal);
        List<Double> modules = signal.getSamples().stream().map(Complex::abs).collect(toList());
        plot(modules, signalPointsIndexStep, xStep);
    }

    public void plotArgument(Signal<Complex> signal) {
        //TODO: Test with zero cases
        //TODO: Wprowadzc threshold
        //TODO: Zobaczyc uzycie PI
        double xStep = getXStepInFrequencyDomain(signal);
        double signalPointsIndexStep = getSignalPointsIndexStep1(signal);
        List<Double> arguments = signal.getSamples()
                .stream()
                .map(c -> atan(c.getImaginary() / c.getReal()))
                .collect(toList());
        plot(arguments, signalPointsIndexStep, xStep);
    }

    public void clear() {
        chart.getData().clear();
    }

    public void setStyle(String filepath) {
        chart.getStyleClass().clear();
        chart.getStyleClass().add(filepath);
    }

    /**
     * @param points
     * @param pointStep indicates the index of next point that should be plotted
     * @param xStep     the distance between point on x axis
     */
    private void plot(List<Double> points, double pointStep, double xStep) {
        XYChart.Series series = new XYChart.Series();
        double current = 0.0;
        for (int j = 0; current < points.size(); current += pointStep, j++) {
            double y = points.get((int) current);
            series.getData().add(new XYChart.Data(xStep * j, y));
        }
        chart.getData().add(series);
    }

    private double getXDistanceBetweenSignalPoints(Signal<Double> signal) {
        final double NUMBER_OF_PIXELS_IN_CHART = chart.getXAxis().getWidth();
        double singlePointDurationInSeconds = signal.getDurationInNs().toNanos() / 1_000_000_000D;
        if (signal.getSamples().size() != 1) {
            singlePointDurationInSeconds /= min(NUMBER_OF_PIXELS_IN_CHART, signal.getSamples().size() - 1);
        }
        return singlePointDurationInSeconds;
    }

    private double getSignalPointsIndexStep(Signal<Double> signal) {
        final double NUMBER_OF_PIXELS_IN_CHART = chart.getXAxis().getWidth();
        double step = 1.0;
        if (signal.getSamples().size() > NUMBER_OF_PIXELS_IN_CHART) {
            step = signal.getSamples().size() / NUMBER_OF_PIXELS_IN_CHART;
        }
        return step;
    }

    private double getXStepInFrequencyDomain(Signal<Complex> signal) {
        return toFrequency(signal.getSamplingPeriod());
    }

    private double getXDistanceBetweenSignalPoints1(Signal<Complex> signal) {
        final double NUMBER_OF_PIXELS_IN_CHART = chart.getXAxis().getWidth();
        double singlePointDurationInSeconds = signal.getDurationInNs().toNanos() / 1_000_000_000D;
        if (signal.getSamples().size() != 1) {
            singlePointDurationInSeconds /= min(NUMBER_OF_PIXELS_IN_CHART, signal.getSamples().size() - 1);
        }
        return singlePointDurationInSeconds;
    }

    private double getSignalPointsIndexStep1(Signal<Complex> signal) {
        final double NUMBER_OF_PIXELS_IN_CHART = chart.getXAxis().getWidth();
        double step = 1.0;
        if (signal.getSamples().size() > NUMBER_OF_PIXELS_IN_CHART) {
            step = signal.getSamples().size() / NUMBER_OF_PIXELS_IN_CHART;
        }
        return step;
    }

}
