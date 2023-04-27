package application;

import javafx.fxml.FXML;

import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

public class MainSceneController {
	@FXML
	private TextField tfLoginUsername;

	// Event Listener on Button.onAction
	@FXML
	public void btnLoginOnClicked(ActionEvent event) {
		Stage mainWindow = (Stage) tfLoginUsername.getScene().getWindow();
		String title = tfLoginUsername.getText();
		mainWindow.setTitle(title);
	}
}
