package cps;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.io.IOException;

public class SignalParameter extends HBox {
    @FXML
    private Label parameterName;

    @FXML
    private TextField parameterValue;

    public SignalParameter() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("signal_parameter.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }


}
