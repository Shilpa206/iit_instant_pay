package controllers;

import java.io.FileNotFoundException;
import java.io.IOException;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
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
	private TabPane tabPane;

	@FXML
	private Tab tabRecentTransactions;

	@FXML
	private Tab tabPayees;

	@FXML
	private Tab tabAnalytics;

	@FXML
	private Tab tabAdmin;

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
	
	@FXML
	private Button btnReload;

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
		System.out.println("Adding payee.");
		DaoModel dao = new DaoModel();
		String payeeEmail = tfAddPayeeEmail.getText();

		if (payeeEmail == null || payeeEmail.strip().equals("")) {
			Alert alert = CommonUtilities.getErrorWindow("Unable to add payee!",
					"Payee email cannot be null or empty.");
			alert.showAndWait();
		} else {
			try {
				dao.addPayee(this.bankAccount.getAccountId(), payeeEmail);
				System.out.println("Payee added.");
			} catch (IllegalStateException ise) {
				Alert alert = CommonUtilities.getErrorWindow("Unable to add payee!", ise.getMessage());
				alert.showAndWait();
			}

			// Re-populate the page with new date
			this.populateHome(this.bankAccount.getAccountId());
		}
	}

	// Analytics
	@FXML
	private TextField tfAnalyticsTotalCashReserve;

	@FXML
	private PieChart pieAnalyticsCashReserve;

	@FXML
    private BarChart<String, Integer> barChartMostCredits;
	
	@FXML
    private BarChart<String, Integer> barChartMostDebits;

	// Admin
	@FXML
	private TextField tfAdminSearchAccountId;

	@FXML
	private Button btnAdminSearchAccount;

	@FXML
	private Button btnAdminUpdateAccount;

	@FXML
	private TextField tfAdminAccountId;

	@FXML
	private TextField tfAdminName;

	@FXML
	private TextField tfAdminEmail;

	@FXML
	private TextField tfAdminBalance;

	@FXML
	private Button btnAdminDeletePayee;

	@FXML
	private TableView<Payee> tableAdminPayees;

	@FXML
	private TableColumn<Payee, String> tblAdminPayeesColPayeeAccoutNumber;

	@FXML
	private TableColumn<Payee, String> tblAdminPayeesColPayeeName;

	@FXML
	private TableColumn<Payee, String> tblAdminPayeesColPayeeEmail;

	@FXML
	void btnAdminSearchAccountOnClicked(ActionEvent event) {
		DaoModel dao = new DaoModel();

		Integer accountId = Integer.parseInt(tfAdminSearchAccountId.getText());
		System.out.println("Searching for account id:" + accountId);

		try {
			BankAccount bankAccount = dao.getBankAccountById(accountId);

			// Re-populate the page with new date
			this.populateAdmin(bankAccount);
		} catch (IllegalStateException ise) {
			Alert alert = CommonUtilities.getErrorWindow("Account not found!", ise.getMessage());
			alert.showAndWait();
		}
	}

	@FXML
	void btnAdminUpdateAccountOnClicked(ActionEvent event) {
		DaoModel dao = new DaoModel();

		try {
			dao.updateBankAccount(Integer.parseInt(tfAdminAccountId.getText()), tfAdminEmail.getText(),
					tfAdminName.getText(), Double.parseDouble(tfAdminBalance.getText()));

			Alert alert = CommonUtilities.getSuccessWindow("Success!", "Account successfully updated!");
			alert.showAndWait();

			BankAccount bankAccount = dao.getBankAccountById(Integer.parseInt(tfAdminAccountId.getText()));
			
			// Re-populate the page with new date
			this.populateAdmin(bankAccount);
		} catch (IllegalStateException ise) {
			Alert alert = CommonUtilities.getErrorWindow("Account not found!", ise.getMessage());
			alert.showAndWait();
		}
	}

	@FXML
	void btnAdminDeletePayeeOnClicked(ActionEvent event) {
		DaoModel dao = new DaoModel();

		try {
			Payee payee = this.tableAdminPayees.getSelectionModel().selectedItemProperty().getValue();

			dao.deletePayee(Integer.parseInt(tfAdminAccountId.getText()), payee.getAccountId());

			Alert alert = CommonUtilities.getSuccessWindow("Success!", "Payee successfully deleted!");
			alert.showAndWait();

			BankAccount bankAccount = dao.getBankAccountById(Integer.parseInt(tfAdminAccountId.getText()));

			// Re-populate the page with new date
			this.populateAdmin(bankAccount);
		} catch (IllegalStateException ise) {
			Alert alert = CommonUtilities.getErrorWindow("Unable to delete payee!", ise.getMessage());
			alert.showAndWait();
		}
	}

	@FXML
	void btnSignOutOnClicked(ActionEvent event) throws IOException {
		this.loadStart(event);
	}
	
	@FXML
	void btnReloadOnClicked(ActionEvent event) throws IOException {
		this.populateHome(this.bankAccount.getAccountId());
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

		if (message.toLowerCase().contains("success")) {
			Alert successWindow = CommonUtilities.getSuccessWindow("Transaction Successful!",
					"$" + amount + " successfully sent to " + payee.getName() + "<" + payee.getEmail() + ">");
			successWindow.showAndWait();
		} else {
			Alert successWindow = CommonUtilities.getErrorWindow("Transaction failed!", message);
			successWindow.showAndWait();
		}

		// Re-populate the page with new date
		this.populateHome(this.bankAccount.getAccountId());
	}

	/*
	 * Populates the values in home screen using information of given account id
	 */
	public void populateHome(Integer accountId) {
		DaoModel dao = new DaoModel();

		System.out.println("Populating home for account id: " + accountId);

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

		// Analytics tab
		this.populateAnalytics();
	}

	public void populateAnalytics() {
		System.out.println("Populating analytics tab.");
		DaoModel dao = new DaoModel();
		
		
		tfAnalyticsTotalCashReserve.setText(dao.getTotalCaseReserve().toString());
		
		ObservableList<PieChart.Data> pieValues = FXCollections.observableArrayList(dao.getCaseReserveShares());
		
		System.out.println("Populating pie chart.");
		this.pieAnalyticsCashReserve.getData().clear();
		this.pieAnalyticsCashReserve.getData().setAll(pieValues);
		this.pieAnalyticsCashReserve.getData().forEach(data -> {
			String percentage = String.format("%.2f%%", (data.getPieValue()));
			Tooltip toolTip = new Tooltip(percentage);
		    Tooltip.install(data.getNode(), toolTip);
		});
		this.pieAnalyticsCashReserve.setLabelLineLength(5);
		this.pieAnalyticsCashReserve.setLegendVisible(false);
		
		System.out.println("Populating most credits bar chart.");
		// Users with most credits 
		XYChart.Series<String, Integer> mostCreditsSeries = new XYChart.Series<>(); 
		mostCreditsSeries.getData().setAll(dao.getAccountsWithMostCredits());
		this.barChartMostCredits.getData().clear();
		this.barChartMostCredits.getData().add(mostCreditsSeries);
		this.barChartMostCredits.setLegendVisible(false);
		
		System.out.println("Populating most debits bar chart.");
		// Users with most debits
		XYChart.Series<String, Integer> mostDebitsSeries = new XYChart.Series<>(); 
		mostDebitsSeries.getData().setAll(dao.getAccountsWithMostDebits());
		this.barChartMostDebits.getData().clear();
		this.barChartMostDebits.getData().add(mostDebitsSeries);
		this.barChartMostDebits.setLegendVisible(false);
	}

	/*
	 * Populates the values in admin screen using information of given account
	 */
	public void populateAdmin(BankAccount bankAccount) {
		System.out.println("Populating admin screen for email: " + bankAccount.getEmail());

		this.tfAdminAccountId.setText(bankAccount.getAccountId().toString());
		this.tfAdminName.setText(bankAccount.getName());
		this.tfAdminEmail.setText(bankAccount.getEmail());
		this.tfAdminBalance.setText(bankAccount.getBalance().toString());

		// Populate payees table
		tblAdminPayeesColPayeeAccoutNumber.setCellValueFactory(new PropertyValueFactory<>("accountId"));
		tblAdminPayeesColPayeeName.setCellValueFactory(new PropertyValueFactory<>("name"));
		tblAdminPayeesColPayeeEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

		this.tableAdminPayees.getItems().setAll(bankAccount.getPayees());

		btnAdminUpdateAccount.setDisable(false);

		this.tableAdminPayees.getSelectionModel().selectedItemProperty()
				.addListener((obs, oldSelection, newSelection) -> {
					if (newSelection != null) {
						btnAdminDeletePayee.setDisable(false);
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

	public void showUserTabs() {
		tabPane.getTabs().remove(tabAnalytics);
		tabPane.getTabs().remove(tabAdmin);
	}

	public void showAdminTabs() {
		tabPane.getTabs().remove(tabRecentTransactions);
		tabPane.getTabs().remove(tabPayees);
	}
}
