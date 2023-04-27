package utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.stage.Stage;


public class CommonUtilities {
	public static final String RESOURCES_DIR_PATH = "RESOURCES_DIR_PATH";
	
	public static URL getViewUrl(String viewFileName) throws FileNotFoundException {
		String resourcesDirPath = System.getenv(RESOURCES_DIR_PATH);
		
		if(resourcesDirPath == null) {
			throw new FileNotFoundException("Resources directory path not found. Set env variable: "+ RESOURCES_DIR_PATH);
		}
		
		String viewFilePath = resourcesDirPath + "/views/" + viewFileName;
		
		File viewFile = new File(viewFilePath);
		
		System.out.println("View file path: " + viewFilePath);
		
		if(!viewFile.exists()) {
			throw new FileNotFoundException("View file not found: "+ viewFilePath);
		}
		else {
			try {
				return viewFile.toURI().normalize().toURL();	
			}
			catch (MalformedURLException e) {
				throw new FileNotFoundException("Unable to fetch view file: " + viewFilePath + " " + e.getMessage());	
			}
		}
	}
	
	public static URL getImageUrl(String imageFileName) throws FileNotFoundException {
		String resourcesDirPath = System.getenv(RESOURCES_DIR_PATH);
		
		if(resourcesDirPath == null) {
			throw new FileNotFoundException("Resources directory path not found. Set env variable: "+ RESOURCES_DIR_PATH);
		}
		
		String imageFilePath = resourcesDirPath + "/images/" + imageFileName;
		
		File imageFile = new File(imageFilePath);
		
		if(!imageFile.exists()) {
			throw new FileNotFoundException("Image file not found: "+ imageFilePath);
		}
		else {
			try {
				return imageFile.toURI().normalize().toURL();	
			}
			catch (MalformedURLException e) {
				throw new FileNotFoundException("Unable to fetch image file: " + imageFilePath + " " + e.getMessage());	
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
	
	public static String encryptPassword(final String password) {
	    try{
	        final MessageDigest digest = MessageDigest.getInstance("SHA-256");
	        final byte[] hash = digest.digest(password.getBytes("UTF-8"));
	        final StringBuilder hexString = new StringBuilder();
	        for (int i = 0; i < hash.length; i++) {
	            final String hex = Integer.toHexString(0xff & hash[i]);
	            if(hex.length() == 1) 
	              hexString.append('0');
	            hexString.append(hex);
	        }
	        return hexString.toString();
	    } catch(Exception ex){
	       throw new RuntimeException(ex);
	    }
	}
}
