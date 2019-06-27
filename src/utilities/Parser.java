package utilities;

import java.io.BufferedReader;
import java.io.FileReader;

import controllers.CanvasController;
import controllers.CanvasController.NodeFX;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
*
* @author ana
*/
public abstract class Parser {

	protected CanvasController canvasController;
	
	public Parser(CanvasController canvasController) {
		this.canvasController=canvasController;
	}
	
	public void parseFile(String filename) {
		
		try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
			String line;
			while ((line = br.readLine()) != null) {
				parseLine(line);
			}
		} catch (Exception e) {
			Alert a = new Alert(AlertType.ERROR);
			a.setContentText("File not found!");
            a.show(); 
		}
		
		
		
	}
	
	public abstract void parseLine(String line);
	
	protected NodeFX src,dst;
	protected String name, route, code;
	double x,y;
	
}
