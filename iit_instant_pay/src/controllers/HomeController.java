package controllers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import models.BankAccount;
import models.DaoModel;
import models.Payee;
import models.RecentTransaction;
import utils.CommonUtilities;

public class HomeController {
	private Stage stage;
	private Scene scene;

	private BankAccount bankAccount;

	@FXML
	private Tab tabPane;

	// Home
	@FXML
	private Label lblWelcomeMessage;

	@FXML
	private TableView<RecentTransaction> tableRecentTransactions;

	@FXML
	private TableColumn<RecentTransaction, String> tblColDate;

	@FXML
	private TableColumn<RecentTransaction, String> tblColType;

	@FXML
	private TableColumn<RecentTransaction, String> tblColDescription;

	@FXML
	private TableColumn<RecentTransaction, Double> tblColAmount;

	@FXML
	private TextField tfAccountNumber;

	@FXML
	private TextField tfAvailableBalance;

	@FXML
	private Button btnSignOut;

	// Payee
	@FXML
	private TextField tfAddPayeeEmail;

	@FXML
	private Button btnAddPayee;

	@FXML
	private TableView<Payee> tablePayees;

	@FXML
	private TableColumn<Payee, String> tblColPayeeAccoutNumber;

	@FXML
	private TableColumn<Payee, String> tblColPayeeName;

	@FXML
	private TableColumn<Payee, String> tblColPayeeEmail;

	@FXML
	private Button btnSendMoney;

	@FXML
	void btnAddPayeeOnClicked(ActionEvent event) {
		DaoModel dao = new DaoModel();
		String payeeEmail = tfAddPayeeEmail.getText();

		if (payeeEmail == null || payeeEmail.strip().equals("")) {
			Alert alert = CommonUtilities.getErrorWindow("Unable to add payee!",
					"Payee email cannot be null or empty.");
			alert.showAndWait();
		} else {
			dao.addPayee(this.bankAccount.getAccountId(), payeeEmail);

			// Re-populate the page with new date
			this.populateHome(this.bankAccount.getAccountId());
		}
	}

	@FXML
	void btnSignOutOnClicked(ActionEvent event) throws IOException {
		this.loadStart(event);
	}

	@FXML
	void btnSendMoneyOnClicked(ActionEvent event) {
		DaoModel dao = new DaoModel();
		
		Payee payee = this.tablePayees.getSelectionModel().selectedItemProperty().getValue();

		TextInputDialog tdAmount = new TextInputDialog();
		tdAmount.setHeaderText("Please enter the amount to transfer.");

		Stage stageAmount = (Stage) tdAmount.getDialogPane().getScene().getWindow();

		try {
			stageAmount.getIcons().add(new Image(CommonUtilities.getImageUrl("dollar_icon.png").toString()));
		} catch (FileNotFoundException e) {
			System.out.println("Unable to load icon.");
		}

		tdAmount.showAndWait();

		Double amount = Double.parseDouble(tdAmount.getEditor().getText());

		TextInputDialog tdRemarks = new TextInputDialog();
		tdRemarks.setHeaderText("Please enter a comment for transaction.");

		Stage stageRemarks = (Stage) tdRemarks.getDialogPane().getScene().getWindow();

		try {
			stageRemarks.getIcons().add(new Image(CommonUtilities.getImageUrl("dollar_icon.png").toString()));
		} catch (FileNotFoundException e) {
			System.out.println("Unable to load icon.");
		}

		stageRemarks.showAndWait();

		String remarks = tdRemarks.getEditor().getText();
		
		String message = dao.executeTransfer(bankAccount.getAccountId(), payee.getAccountId(), amount, remarks);
		
		if(message.toLowerCase().contains("success")) {
			Alert successWindow = CommonUtilities.getSuccessWindow("Transaction Successful!",
					"$" + amount + " successfully sent to " + payee.getName() + "<" + payee.getEmail() + ">");
			successWindow.showAndWait();
		}
		else {
			Alert successWindow = CommonUtilities.getErrorWindow("Transaction failed!", message);
			successWindow.showAndWait();
		}
		
		// Re-populate the page with new date
		this.populateHome(this.bankAccount.getAccountId());
	}

	/*
	 * Populates the values in home screen using information of given user
	 */
	public void populateHome(Integer accountId) {
		DaoModel dao = new DaoModel();

		System.out.println("Populating home for user id: " + accountId);

		BankAccount bankAccount = dao.getBankAccountById(accountId);

		this.bankAccount = bankAccount;

		this.lblWelcomeMessage.setText("Welcome " + bankAccount.getName() + "!");
		this.tfAvailableBalance.setText(bankAccount.getBalance().toString());
		this.tfAccountNumber.setText(bankAccount.getAccountId().toString());

		// Populate recent transactions
		tblColDate.setCellValueFactory(new PropertyValueFactory<>("date"));
		tblColType.setCellValueFactory(new PropertyValueFactory<>("type"));
		tblColDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
		tblColAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));

		this.tableRecentTransactions.getItems().setAll(bankAccount.getRecentTransactions());

		// Populate payees table
		tblColPayeeAccoutNumber.setCellValueFactory(new PropertyValueFactory<>("accountId"));
		tblColPayeeName.setCellValueFactory(new PropertyValueFactory<>("name"));
		tblColPayeeEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

		this.tablePayees.getItems().setAll(bankAccount.getPayees());

		this.tablePayees.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
			if (newSelection != null) {
				btnSendMoney.setDisable(false);
			}
		});
	}

	void loadStart(Event event) throws IOException {
		FXMLLoader loader = new FXMLLoader(CommonUtilities.getViewUrl("Start.fxml"));
		Parent root = loader.load();
		stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
		scene = new Scene(root);

		// Do not automatically focus on a text field
		Platform.runLater(() -> root.requestFocus());

		stage.setScene(scene);
		stage.show();
	}
}
