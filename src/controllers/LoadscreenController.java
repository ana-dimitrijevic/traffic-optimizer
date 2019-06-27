package controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import fxsimulator.Main;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;
import utilities.*;

/**
 *
 * @author ana
 */
public class LoadscreenController implements Initializable {

	@FXML
	public Button panel1Next, panel2Back;

	@FXML
	private AnchorPane panel1;

	@FXML
	private RadioButton rbBelgrade;

	private String filenameToLoad, extension;

	private static CanvasController canvasController;

	@Override
	public void initialize(URL url, ResourceBundle rb) {

		rbBelgrade.setSelected(false);
		panel1Next.setDisable(true);


		// Button Action listeners
		rbBelgrade.setOnAction(e -> {
			
			panel1Next.setDisable(false);
			panel1Next.setStyle("-fx-background-color : #487eb0;");
			
		});

		panel1Next.setOnAction((event) -> {

			filenameToLoad = "1.gml";
			String[] s = filenameToLoad.split("\\.");
			extension = s[1];

			FadeOut();

		});

	}

	void FadeOut() {
		FadeTransition ft = new FadeTransition();
		ft.setDuration(Duration.millis(1000));
		ft.setNode(panel1);
		ft.setFromValue(1);
		ft.setToValue(0);
		ft.setOnFinished(e -> {
			loadNextScene();
		});
		ft.play();
	}

	void loadNextScene() {
		try {

			FXMLLoader loader = new FXMLLoader(getClass().getResource("../fxml/Canvas.fxml"));
			Parent root = loader.load();
			Scene newScene = new Scene(root);
			setCanvasController(loader.getController());

			parseFile();

			newScene.getStylesheets().add(getClass().getResource("../css/Styling.css").toExternalForm());
			Main.primaryStage.setScene(newScene);
			Main.primaryStage.setFullScreen(true);

		} catch (IOException ex) {
			Logger.getLogger(LoadscreenController.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	void parseFile() {

		Parser parser;

		switch (extension) {

		case "gml": {
			parser = new GMLParser(getCanvasController());
			parser.parseFile(filenameToLoad);
			break;
		}

		default: {

			Alert a = new Alert(AlertType.ERROR);
			a.setContentText("Unsupported file type");
			a.show();

		}

		}

	}

	public static CanvasController getCanvasController() {
		return canvasController;
	}

	public static void setCanvasController(CanvasController canvasController) {
		LoadscreenController.canvasController = canvasController;
	}

}
