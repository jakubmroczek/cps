package cps;

import com.google.gson.Gson;
import cps.model.SignalChart;

import java.io.*;
import java.nio.file.Files;

public class SignalWriter {

    public static void writeJSON(File file, SignalChart signalChart) {
        Gson gson = new Gson();

        //TODO: Other sceneario
        if (signalChart != null) {
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
