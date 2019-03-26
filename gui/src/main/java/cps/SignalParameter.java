package cps;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import lombok.Getter;


public class SignalParameter extends HBox {

    @FXML
    @Getter
    private Label parameterName;

    @FXML
    @Getter
    private TextField parameterValue;

    public SignalParameter() {
        FXMLLoader fxmlLoader = new FXMLLoader(

                getClass().getResource("/fxml/SignalParameter.fxml"));


        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

}