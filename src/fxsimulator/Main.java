package fxsimulator;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
* Application that visualizes and optimizes city transport
* Project assignment for my OOP course
* 
* @author  ana
* @version 1.0
*/
public class Main extends Application {

    public static Stage primaryStage;
    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        Parent root = FXMLLoader.load(getClass().getResource("../fxml/Loadscreen.fxml"));        
        Scene scene = new Scene(root);
        
        primaryStage.setScene(scene);
        primaryStage.setFullScreen(true);
        primaryStage.show();
        
    }

    public static void main(String[] args) {
        launch(args);
    }
    
}
