package cps;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import cps.model.Math;
import cps.model.Signal;
import cps.model.SignalArgs;
import cps.model.SignalChart;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SignalWriter {

    public static void writeJSON(File file, SignalChart signalChart) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        //TODO: Other sceneario
        if (signalChart != null) {
            List<Double> newProbes = new ArrayList<>();
            for (Double d : signalChart.getProbes()) {
                newProbes.add(d = Math.round(d, 2));
            }
            signalChart.setProbes(newProbes);

            String signalJson = gson.toJson(signalChart);
            try {
                Files.write(file.toPath(), signalJson.getBytes());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else {
            System.out.println("generatedSignal is null");
        }
    }

    //Binary format:
    public static SignalChart readBinary(File file) {
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
        SignalChart sc = new SignalChart(duration, probingPeriod, probes);
        long t1inNanos = (long) t1 * 1000_000_000L;
        SignalArgs sa = SignalArgs.builder().initialTime(Duration.ofNanos(t1inNanos)).build();
        sc.setArgs(sa);
        if (type == Signal.Type.CONTINUOUS.toString().charAt(0)) {
            sc.setSignalType(Signal.Type.CONTINUOUS);
        } else {
            sc.setSignalType(Signal.Type.DISCRETE);
        }

        return sc;
    }

    //t1 in s
    //samplingFreq
    //signal type C(ontinous) D(iscrete)
    //probes[]
    //
    public static void writeBinary(File file, float t1, long fq, SignalChart signalChart) {

        List<Double> newProbes = new ArrayList<>();
        for (Double d : signalChart.getProbes()) {
            newProbes.add(d = Math.round(d, 2));
        }
        signalChart.setProbes(newProbes);

        try (FileOutputStream fos = new FileOutputStream(file)) {

            DataOutputStream dos = new DataOutputStream(fos);
            dos.writeFloat(t1);
            dos.writeInt((int) fq);
            dos.writeChar(signalChart.getSignalType().toString().charAt(0));
            for (Double d : signalChart.getProbes()) {
                double d1 = Math.round(d, 2);
                float f = (float) d1;
                dos.writeFloat(f);
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
}
