package cps;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;


public class SignalParameter extends HBox {

    @FXML
    private AnchorPane myTestButton;

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