package models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import utils.CommonUtilities;

/*
 * Database Access Object
 * Class to interact with database
 */
public class DaoModel {
	public static final String ACCOUNTS = "IP_Accounts";
	public static final String TRANSACTIONS = "IP_Transactions";
	public static final String PAYEES = "IP_Payees";

	DBConnect db = null;

	public DaoModel() {
		db = new DBConnect();
	}

	
	// Private method that returns connection to the DB initialized in constructor
	private Connection getDbConn() throws SQLException {
		DbCredentials dbCredentials;
		
		try {
			dbCredentials = DbCredentials.getCredentials();	
		}
		catch(Exception e) {
			throw new SQLException("Cannot load DB Credentials. " + e.getMessage());
		}
		
		String protocol = "jdbc";
		String subprotocol = "mysql";
		String host = dbCredentials.getHost();
		short port = dbCredentials.getPort();
		String schema = dbCredentials.getSchema();
		String connOptions = dbCredentials.getConnOptions();

		String connUrl = host + ":" + port + "/" + schema + "?" + connOptions;

		// Database credentials
		String username = dbCredentials.getUsername();
		String password = dbCredentials.getPassword();

		return db.getConnection(protocol, subprotocol, connUrl, username, password);
	}

	public Integer validateCredentials(String email, String password) throws IllegalAccessException {
		System.out.println("Validating credentials of: " + email);
		try (Connection conn = getDbConn()) {
			Statement selectStmt = conn.createStatement();
			String sql = String.format("SELECT AccountId, Name, Email, Password FROM %s WHERE Email = '%s';", ACCOUNTS,
					email, password);

			ResultSet results = selectStmt.executeQuery(sql);

			if (!results.isBeforeFirst()) {
				throw new IllegalAccessException(String.format("User (%s) not found.", email));
			}

			// Get to first record
			results.next();

			/* 
			 * Passwords are hashed (using SHA256) before storing them in DB 
			 * Because user enters the raw password, we will hash it before comparing with the password stored in DB
			 */
			String hashedPassword = CommonUtilities.encryptPassword(password);
			
			String actualPassword = results.getString("Password");

			if (!hashedPassword.equals(actualPassword)) {
				throw new IllegalAccessException(String.format("Incorrect Username / Password.", email));
			}

			System.out.println("Successfully validated credentials");
			return results.getInt("AccountId");
		} catch (SQLException se) {
			throw new IllegalAccessException("Unable to validate credentials. " + se.getMessage());
		}
	}

	/*
	 * Generate bank account object for given account id
	 */
	public BankAccount getBankAccountById(Integer accountId) {
		System.out.println("Getting bank account details of account id: " + accountId);
		BankAccount bankAccount = new BankAccount();
		bankAccount.setAccountId(accountId);

		try {
			try (Connection conn = getDbConn()) {
				Statement selectStmt = conn.createStatement();
				String sql = String.format("SELECT AccountId, Name, Email, Balance FROM %s WHERE AccountId = '%s';",
						ACCOUNTS, accountId);

				ResultSet results = selectStmt.executeQuery(sql);

				// Get to first record
				results.next();

				bankAccount.setName(results.getString("Name"));
				bankAccount.setEmail(results.getString("Email"));
				bankAccount.setBalance(results.getDouble("Balance"));

				List<Payee> payees = this.getPayees(accountId);

				List<RecentTransaction> recentTransactions = this.getRecentTransactions(accountId);

				bankAccount.setPayees(payees);
				bankAccount.setRecentTransactions(recentTransactions);

				System.out.println("Bank account details fetched.");
				return bankAccount;
			}
		} catch (SQLException se) {
			throw new IllegalStateException("Unable to fetch account details. " + se.getMessage());
		}
	}

	public BankAccount getBankAccountByEmail(String email) {
		System.out.println("Getting bank account details of email: " + email);
		BankAccount bankAccount = new BankAccount();

		try {
			try (Connection conn = getDbConn()) {
				Statement selectStmt = conn.createStatement();
				String sql = String.format("SELECT AccountId, Name, Email, Balance FROM %s WHERE Email = '%s';",
						ACCOUNTS, email);

				ResultSet results = selectStmt.executeQuery(sql);

				// Get to first record
				results.next();
				bankAccount.setAccountId(results.getInt("AccountId"));
				bankAccount.setName(results.getString("Name"));
				bankAccount.setEmail(results.getString("Email"));
				bankAccount.setBalance(results.getDouble("Balance"));

				List<Payee> payees = this.getPayees(bankAccount.getAccountId());

				List<RecentTransaction> recentTransactions = this.getRecentTransactions(bankAccount.getAccountId());

				bankAccount.setPayees(payees);
				bankAccount.setRecentTransactions(recentTransactions);

				System.out.println("Bank account details fetched.");
				return bankAccount;
			}
		} catch (SQLException se) {
			throw new IllegalStateException("Unable to fetch account details. " + se.getMessage());
		}
	}

	public List<RecentTransaction> getRecentTransactions(Integer accountId) {
		System.out.println("Getting recent transactions for: " + accountId);
		List<RecentTransaction> recentTransactions = new ArrayList<RecentTransaction>();

		try (Connection conn = getDbConn()) {
			Statement selectStmt = conn.createStatement();
			String sql = String.format("""
					SELECT *
					FROM
					(
						SELECT t.TransactionTime, 
							   'Credit' AS TransactionType,
							   t.Amount,
							   a.Name, 
							   a.Email, 
							   t.Remarks
						FROM %s t 
						LEFT OUTER JOIN %s a
						ON t.FromAccountId = a.AccountId
						WHERE ToAccountId = %s
						UNION 
						SELECT t.TransactionTime, 
							   'Debit' AS TransactionType,
							   -1 * t.Amount AS Amount,
							   a.Name, 
							   a.Email, 
							   t.Remarks
						FROM %s t 
						LEFT OUTER JOIN %s a
						ON t.ToAccountId = a.AccountId
						WHERE FromAccountId = %s
					) a
					ORDER BY TransactionTime DESC
					LIMIT 30;
					""", TRANSACTIONS, ACCOUNTS, accountId, TRANSACTIONS, ACCOUNTS, accountId);

			ResultSet results = selectStmt.executeQuery(sql);

			if (!results.isBeforeFirst()) {
				System.out.println("No recent transactions found for account id: " + accountId);
			} else {
				while (results.next()) {
					RecentTransaction t = new RecentTransaction();

					t.setDate(results.getString("TransactionTime"));
					t.setAmount(results.getDouble("Amount"));
					t.setType(results.getString("TransactionType"));
					t.setDescription(results.getString("Name") + "(" + results.getString("Email") + ")#"
							+ results.getString("Remarks"));

					recentTransactions.add(t);
				}
				System.out.println("Fetched recent transactions.");
			}
			return recentTransactions;
		} catch (SQLException se) {
			throw new IllegalStateException("Unable to fetch recent transactions. " + se.getMessage());
		}
	}

	public List<Payee> getPayees(Integer accountId) {
		System.out.println("Getting payees for: " + accountId);
		List<Payee> payees = new ArrayList<Payee>();

		try (Connection conn = getDbConn()) {
			Statement selectStmt = conn.createStatement();
			String sql = String.format("""
					SELECT a.AccountId, a.Name, a.Email
					FROM %s p
					LEFT OUTER JOIN %s a
					  ON p.PayeeAccountId = a.AccountId
					WHERE p.PayerAccountId = %s
					  AND Enabled = true;
					""", PAYEES, ACCOUNTS, accountId);

			ResultSet results = selectStmt.executeQuery(sql);

			if (!results.isBeforeFirst()) {
				System.out.println("No payees found for: " + accountId);
			} else {
				while (results.next()) {
					Payee p = new Payee();

					p.setAccountId(results.getInt("AccountId"));
					p.setName(results.getString("Name"));
					p.setEmail(results.getString("Email"));

					payees.add(p);
				}
				System.out.println("Fetched payees.");
			}

			return payees;
		} catch (SQLException se) {
			throw new IllegalStateException("Unable to fetch payees. " + se.getMessage());
		}
	}

	public BankAccount createAccount(String name, String email, String password) {
		System.out.println("Creating bank account for email: " + email);
		try (Connection conn = getDbConn()) {
			Double initialBalance = 1000.0;

			String sql = String.format("""
					INSERT INTO %s(Name, Email, Password, Balance)
					VALUES
					(?, ?, ?, ?);
					""", ACCOUNTS);

			PreparedStatement preparedStmt = conn.prepareStatement(sql);

			String encryptedPassword = CommonUtilities.encryptPassword(password);
			
			preparedStmt.setString(1, name);
			preparedStmt.setString(2, email);
			preparedStmt.setString(3, encryptedPassword);
			preparedStmt.setDouble(4, initialBalance);

			preparedStmt.executeUpdate();
			
			System.out.println("Bank account successfully created.");
			return this.getBankAccountByEmail(email);
		} catch (SQLException se) {
			throw new IllegalStateException("Unable to create account. " + se.getMessage());
		}
	}

	public void addPayee(Integer payerAccountId, String payeeEmail) {
		System.out.println("Adding payee: " + payeeEmail + " to: " + payerAccountId);
		BankAccount payeeBankAccount = this.getBankAccountByEmail(payeeEmail);

		try (Connection conn = getDbConn()) {
			String sql = String.format("""
					INSERT INTO %s(PayerAccountId, PayeeAccountId)
					VALUES
					(?, ?);
					""", PAYEES);

			PreparedStatement preparedStmt = conn.prepareStatement(sql);

			preparedStmt.setInt(1, payerAccountId);
			preparedStmt.setInt(2, payeeBankAccount.getAccountId());

			preparedStmt.executeUpdate();
			System.out.println("Payee successfully added.");
		} catch (SQLException se) {
			throw new IllegalStateException("Unable to add payee. " + se.getMessage());
		}
	}

	public String executeTransfer(Integer fromAccountId, Integer toAccountId, Double amount, String remarks) {
		System.out.println("Executing transfer from: " + fromAccountId + " to: " + toAccountId);
		try (Connection conn = getDbConn()) {
			String sql = String.format("""
					CALL IP_Transfer(?, ?, ?, ?);
					""", PAYEES);

			PreparedStatement preparedStmt = conn.prepareStatement(sql);

			preparedStmt.setInt(1, fromAccountId);
			preparedStmt.setInt(2, toAccountId);
			preparedStmt.setDouble(3, amount);
			preparedStmt.setString(4, remarks);

			ResultSet results = preparedStmt.executeQuery();
			
			results.next();
			
			return results.getString("Message");
		} catch (SQLException se) {
			throw new IllegalStateException("Unable to make payment. " + se.getMessage());
		}
	}
	
	public void updateBankAccount(Integer accountId, String email, String name, Double balance) {
		System.out.println("Updating account.");
		try (Connection conn = getDbConn()) {
			String sql = String.format("""
					UPDATE %s
					SET Email = ?, Name = ?, Balance = ?
					WHERE AccountId = ?;
					""", ACCOUNTS);

			PreparedStatement preparedStmt = conn.prepareStatement(sql);

			preparedStmt.setString(1, email);
			preparedStmt.setString(2, name);
			preparedStmt.setDouble(3, balance);
			preparedStmt.setInt(4, accountId);

			preparedStmt.executeUpdate();
			System.out.println("Account updated.");
		} catch (SQLException se) {
			throw new IllegalStateException("Unable to update account. " + se.getMessage());
		}
	}
	
	/**
	 * Disable payee
	 */
	public void deletePayee(Integer payerAccountId, Integer payeeAccountId) {
		System.out.println("Deleting payee: " + payeeAccountId + " from: " + payerAccountId);
		try (Connection conn = getDbConn()) {
			String sql = String.format("""
					UPDATE %s
					SET Enabled = false
					WHERE PayerAccountId = ? 
					  AND PayeeAccountId = ?;
					""", PAYEES);

			PreparedStatement preparedStmt = conn.prepareStatement(sql);

			preparedStmt.setInt(1, payerAccountId);
			preparedStmt.setInt(2, payeeAccountId);

			preparedStmt.executeUpdate();
			System.out.println("Payee deleted.");
		} catch (SQLException se) {
			throw new IllegalStateException("Unable to remove payee. " + se.getMessage());
		}
	}
	
	
	public List<PieChart.Data> getCaseReserveShares() {
		List<PieChart.Data> data = new ArrayList<>();
		try (Connection conn = getDbConn()) {
			Statement selectStmt = conn.createStatement();
			String sql = String.format("""
					SELECT a.Name, ROUND((a.Balance / t.TotalBalance) * 100, 1) AS ReserveShare 
					FROM %s a, (SELECT SUM(Balance) TotalBalance FROM %s WHERE AccountId != 999999) t 
					WHERE a.AccountId != 999999
					ORDER BY ROUND((a.Balance / t.TotalBalance) * 100, 1) DESC
					LIMIT 10;
					""", ACCOUNTS, ACCOUNTS);

			ResultSet results = selectStmt.executeQuery(sql);

			while (results.next()) {
				String name = results.getString("Name");
				Double balanceShare = results.getDouble("ReserveShare");

				data.add(new PieChart.Data(name, balanceShare));
			}
			
			return data;			
		} catch (SQLException se) {
			throw new IllegalStateException("Unable to get cash reserver shares. " + se.getMessage());
		}
	}
	
	public Long getTotalCaseReserve() {
		try (Connection conn = getDbConn()) {
			Statement selectStmt = conn.createStatement();
			String sql = String.format("""
					SELECT SUM(Balance) TotalBalance FROM %s WHERE AccountId != 999999
					""", ACCOUNTS);

			ResultSet results = selectStmt.executeQuery(sql);
			
			results.next();
			
			return results.getLong("TotalBalance");	
		} catch (SQLException se) {
			throw new IllegalStateException("Unable to get total cash reserve. " + se.getMessage());
		}
	}
	
	public List<XYChart.Data<String, Integer>> getAccountsWithMostCredits() {
		List<XYChart.Data<String, Integer>> data = new ArrayList<>();
		
		try (Connection conn = getDbConn()) {
			Statement selectStmt = conn.createStatement();
			String sql = String.format("""
					SELECT ta.Name, COUNT(1) NumberOfCredits
					FROM %s t 
					INNER JOIN %s ta ON t.ToAccountId = ta.AccountId
					GROUP BY ta.Name
					ORDER BY COUNT(1) DESC
					LIMIT 5;
					""", TRANSACTIONS, ACCOUNTS);

			ResultSet results = selectStmt.executeQuery(sql);

			while (results.next()) {
				String name = results.getString("Name");
				Integer numberOfCredits = results.getInt("NumberOfCredits");
					
				data.add(new XYChart.Data<String, Integer>(name, numberOfCredits));
			}
			
			return data;			
		} catch (SQLException se) {
			throw new IllegalStateException("Unable to get accounts with most credits. " + se.getMessage());
		}
	}
	
	public List<XYChart.Data<String, Integer>> getAccountsWithMostDebits() {
		List<XYChart.Data<String, Integer>> data = new ArrayList<>();
		
		try (Connection conn = getDbConn()) {
			Statement selectStmt = conn.createStatement();
			String sql = String.format("""
					SELECT fa.Name, COUNT(1) NumberOfDebits
					FROM %s t 
					INNER JOIN %s fa ON t.FromAccountId = fa.AccountId
					GROUP BY fa.Name
					ORDER BY COUNT(1) DESC
					LIMIT 5;
					""", TRANSACTIONS, ACCOUNTS);

			ResultSet results = selectStmt.executeQuery(sql);

			while (results.next()) {
				String name = results.getString("Name");
				Integer numberOfDebits = results.getInt("NumberOfDebits");
					
				data.add(new XYChart.Data<String, Integer>(name, numberOfDebits));
			}
			
			return data;			
		} catch (SQLException se) {
			throw new IllegalStateException("Unable to get accounts with most debits. " + se.getMessage());
		}
	}
}
