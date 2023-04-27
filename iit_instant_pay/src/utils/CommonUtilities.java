package utils;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.stage.Stage;


public class CommonUtilities {
	public static URL getViewUrl(String viewFileName) throws FileNotFoundException {
		
		URL fileUrl = CommonUtilities.class.getResource("../views/" + viewFileName);
		
		if(fileUrl == null) {
			throw new FileNotFoundException("View file not found: "+ viewFileName);
		}
		else {
			try {
				return fileUrl.toURI().normalize().toURL();	
			}
			catch (MalformedURLException | URISyntaxException e) {
				throw new FileNotFoundException("Unable to fetch view file: " + viewFileName + " " + e.getMessage());	
			}
		}
	}	
	
	public static URL getImageUrl(String imageFileName) throws FileNotFoundException {
		
		URL fileUrl = CommonUtilities.class.getResource("../resources/images/" + imageFileName);
		
		if(fileUrl == null) {
			throw new FileNotFoundException("Image file not found: "+ imageFileName);
		}
		else {
			try {
				return fileUrl.toURI().normalize().toURL();	
			}
			catch (MalformedURLException | URISyntaxException e) {
				throw new FileNotFoundException("Unable to fetch image file: " + imageFileName + " " + e.getMessage());	
			}
		}
	}	
	
	public static Alert getErrorWindow(String title, String message) {
    	Alert alert = new Alert(AlertType.ERROR);
    	Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
    	
    	try {
    		stage.getIcons().add(new Image(CommonUtilities.getImageUrl("dollar_icon.png").toString()));
    	}
    	catch(FileNotFoundException e) {
    		System.out.println("Unable to load icon.");
    	}
    	
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		return alert;
	}
	
	public static Alert getSuccessWindow(String title, String message) {
    	Alert alert = new Alert(AlertType.INFORMATION);
    	Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
    	
    	try {
    		stage.getIcons().add(new Image(CommonUtilities.getImageUrl("dollar_icon.png").toString()));
    	}
    	catch(FileNotFoundException e) {
    		System.out.println("Unable to load icon.");
    	}
    	
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		return alert;
	}
}
