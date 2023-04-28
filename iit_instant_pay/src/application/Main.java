package application;
	
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import utils.CommonUtilities;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;


public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			System.out.println("Starting application.");
			Parent root = FXMLLoader.load(CommonUtilities.getViewUrl("Start.fxml"));
			Scene scene = new Scene(root);
			
			// Do not automatically focus on a text field
			Platform.runLater( () -> root.requestFocus());
			
			primaryStage.setTitle("IIT Instant Pay");
			primaryStage.getIcons().add(new Image(CommonUtilities.getImageUrl("dollar_icon.png").toString()));
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
