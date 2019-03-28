package cps;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import cps.model.Math;
import cps.model.SignalChart;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class SignalWriter {

    public static void writeJSON(File file, SignalChart signalChart) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        //TODO: Other sceneario
        if (signalChart != null) {
            List<Double> newProbes = new ArrayList<>();
            for (Double d: signalChart.getProbes()) {
                newProbes.add(d = Math.round(d,2));
            }
            signalChart.setProbes(newProbes);

            String signalJson = gson.toJson(signalChart);
            try {
                Files.write(file.toPath(), signalJson.getBytes());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }  else {
            System.out.println("generatedSignal is null");
        }
    }

    public static void writeBinary(File file, SignalChart signalChart) {

        List<Double> newProbes = new ArrayList<>();
        for (Double d: signalChart.getProbes()) {
            newProbes.add(d = Math.round(d,2));
        }
        signalChart.setProbes(newProbes);
        
        try (FileOutputStream fos = new FileOutputStream(file);
                ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                oos.writeObject(signalChart);
                //TODO: Do we need this
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
