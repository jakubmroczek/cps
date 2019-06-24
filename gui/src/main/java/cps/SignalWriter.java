package cps;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import cps.model.Signal;
import org.apache.commons.math3.complex.Complex;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static cps.model.Math.round;

public class SignalWriter {

    static void writeJSON(File file, Signal signal) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            prepareToSave(signal);
            String signalJson = gson.toJson(signal);
            Files.write(file.toPath(), signalJson.getBytes());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    static Signal readJSON(File file) throws IOException {
        Gson gson = new Gson();
        JsonReader reader = new JsonReader(new FileReader(file));
        return gson.fromJson(reader, Signal.class);
    }

    //Binary format:
    public static Signal readBinary(File file) {
                int i = 0;

                Duration duration = null, probingPeriod = null;
                float t1 = -1.0f;
                char type = ' ';
                List<Double> probes = new ArrayList<>();
                try {
                    FileInputStream fi = new FileInputStream(file);
                    DataInputStream dis = new DataInputStream(fi);
                    int count = fi.available();
                    byte[] bytes = new byte[count];
                    dis.read(bytes);
                    while (i < count) {
                        if (i == 0) {
                            t1 = getByteBuffer(Arrays.copyOfRange(bytes, i, i += Float.BYTES)).getFloat();
                        } else if (i == 4) {
                            probingPeriod = Duration.ofNanos(
                                    1000_000_000L / getByteBuffer(Arrays.copyOfRange(bytes, i, i += Integer.BYTES)).getInt());
                        } else if (i == 8) {
                            type = getByteBuffer(Arrays.copyOfRange(bytes, i, i += Character.BYTES)).getChar();
                        } else {
                            float sample = getByteBuffer(Arrays.copyOfRange(bytes, i, i += Float.BYTES)).getFloat();
                            double probe = (double) sample;
                            probes.add(probe);
                        }
                    }
                    duration = Duration.ofNanos(probingPeriod.toNanos() * probes.size());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Signal.Type resultType;

                long t1inNanos = (long) t1 * 1000_000_000L;
                if (type == Signal.Type.CONTINUOUS.toString().charAt(0)) {
                    resultType = Signal.Type.CONTINUOUS;
                } else {
                    resultType = Signal.Type.DISCRETE;
                }

                return new Signal(resultType, duration, probingPeriod, probes);
    }

    //t1 in s
    //samplingFreq
    //signal type C(ontinous) D(iscrete)
    //probes[]
    //
    public static void writeBinary(File file, float t1, long fq, Signal<Double> signal) {
        prepareToSave(signal);
        try (FileOutputStream fos = new FileOutputStream(file)) {

            DataOutputStream dos = new DataOutputStream(fos);
            dos.writeFloat(t1);
            dos.writeInt((int) fq);
            dos.writeChar(signal.getType().toString().charAt(0));
            for (double d : signal.getSamples()) {
                dos.writeFloat((float) d);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static ByteBuffer getByteBuffer(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(bytes.length);
        byteBuffer.put(bytes);
        byteBuffer.flip();
        return byteBuffer;
    }

    private static void prepareToSave(Signal<Double> signal) {
        //TODO: Do we really need this?
        for (int i = 0; i < signal.getSamples().size(); i++) {
            double rounded = signal.getSamples().get(i);
            rounded = round(rounded, 2);
            signal.getSamples().set(i, rounded);
        }
    }

    public static void writeComplexJSON(File file, Signal<Complex> signal) {
    }

    public static void writeComplexBinary(File file, float t1, long fq, Signal<Complex> signal) {
        try (FileOutputStream fos = new FileOutputStream(file)) {

            DataOutputStream dos = new DataOutputStream(fos);
            dos.writeFloat(t1);
            dos.writeInt((int) fq);
            dos.writeChar(signal.getType().toString().charAt(0));
            dos.writeChar('C');
            for (Complex d : signal.getSamples()) {

                dos.writeFloat((float) d.getReal());
                dos.writeFloat((float) d.getImaginary());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Signal<Complex> readComplexJSON(File file) {
        return null;
    }

    public static Signal<Complex> readComplexBinary(File file) {
        int i = 0;

        Duration duration = null, probingPeriod = null;
        float t1 = -1.0f;
        char type = ' ';
        char sampleType = ' ';
        List<Double> probes = new ArrayList<>();
        List<Complex> complexProbes = new ArrayList<>();
        try {
            FileInputStream fi = new FileInputStream(file);
            DataInputStream dis = new DataInputStream(fi);
            int count = fi.available();
            byte[] bytes = new byte[count];
            dis.read(bytes);
            while (i < count) {
                if (i == 0) {
                    t1 = getByteBuffer(Arrays.copyOfRange(bytes, i, i += Float.BYTES)).getFloat();

                } else if (i == 4) {
                    probingPeriod = Duration.ofNanos(
                            1000_000_000L / getByteBuffer(Arrays.copyOfRange(bytes, i, i += Integer.BYTES)).getInt());

                } else if (i == 8) {
                    type = getByteBuffer(Arrays.copyOfRange(bytes, i, i += Character.BYTES)).getChar();

                } else if (i == 10) {

                    sampleType = getByteBuffer(Arrays.copyOfRange(bytes, i, i += Character.BYTES)).getChar();
                }
                else {
                    if(sampleType == 'D'){
                        float sample = getByteBuffer(Arrays.copyOfRange(bytes, i, i += Float.BYTES)).getFloat();
                        double probe = (double) sample;
                        probes.add(probe);
                    } else {
                        float re = getByteBuffer(Arrays.copyOfRange(bytes, i, i += Float.BYTES)).getFloat();
                        float im = getByteBuffer(Arrays.copyOfRange(bytes, i, i += Float.BYTES)).getFloat();

                        Complex probe = new Complex(re, im);
                        complexProbes.add(probe);
                    }

                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Signal.Type resultType;

        long t1inNanos = (long) t1 * 1000_000_000L;
        if (type == Signal.Type.CONTINUOUS.toString().charAt(0)) {
            resultType = Signal.Type.CONTINUOUS;
        } else {
            resultType = Signal.Type.DISCRETE;
        }

        //TODO: Na sile wcisniety kod Piotrka, wczyscis
        if(sampleType == 'C'){
            duration = Duration.ofNanos(probingPeriod.toNanos() * complexProbes.size());
            //TUTAJ JAWNIE ZMIENIAMY KOLEJNOSC TRWANIA I PROBKOWANIA
            return new Signal<>(resultType, probingPeriod, duration, complexProbes);
        } else {
            //TODO: Nienajlepszy pomysl
           throw new IllegalArgumentException("Załadowany sygnał nie jest zespolony");
        }
    }
}
