package controllers;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.BankAccount;
import models.DaoModel;
import utils.CommonUtilities;

public class StartController {
	private final Integer ADMIN_ACCOUNT_ID = 999999;
	
	private Stage stage;
	private Scene scene;

	@FXML
	private PasswordField tfRegisterPassword;

	@FXML
	private TextField tfRegisterName;

	@FXML
	private Button btnLogin;

	@FXML
	private PasswordField tfRegisterConfirmPassword;

	@FXML
	private PasswordField tfLoginPassword;

	@FXML
	private TextField tfLoginEmail;

	@FXML
	private TextField tfRegisterEmail;

	@FXML
	private Button btnRegister;

	@FXML
	void btnLoginOnClicked(ActionEvent event) throws IOException {
		DaoModel dao = new DaoModel();

		Integer accountId = -9999;

		boolean areCredentialsValid = false;
		try {
			accountId = dao.validateCredentials(tfLoginEmail.getText(), tfLoginPassword.getText());
			areCredentialsValid = true;
		} catch (IllegalAccessException e) {
			System.out.println("Credential validation failed.");
			Alert alert = CommonUtilities.getErrorWindow("Login failed!", e.getMessage());
			alert.showAndWait();
		}

		if (areCredentialsValid) {
			loadHome(event, accountId);
		}
	}

	@FXML
	void btnRegisterOnClicked(ActionEvent event) throws IOException {
		DaoModel dao = new DaoModel();

		if (!tfRegisterPassword.getText().equals(tfRegisterConfirmPassword.getText())) {
			Alert alert = CommonUtilities.getErrorWindow("Registration failed!", "Passwords did not match.");
			alert.showAndWait();
		}
		else {
			BankAccount bankAccount = dao.createAccount(tfRegisterName.getText(), tfRegisterEmail.getText(),
					tfRegisterPassword.getText());		
			loadHome(event, bankAccount.getAccountId());
		}
	}
	
	void loadHome(Event event, Integer accountId) throws IOException {
		System.out.println("Loading home page.");
		FXMLLoader loader = new FXMLLoader(CommonUtilities.getViewUrl("Home.fxml"));
		Parent root = loader.load();

		HomeController homeController = loader.getController();
		homeController.populateHome(accountId);

		if(accountId.intValue() == ADMIN_ACCOUNT_ID.intValue()) {
			System.out.println("Detected admin user. Loading admin view.");
			homeController.showAdminTabs();
		}
		else {
			System.out.println("Detected regular user. Loading client view.");
			homeController.showUserTabs();
		}
				
		stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
		scene = new Scene(root);
		stage.setScene(scene);
		stage.show();
	}
}
