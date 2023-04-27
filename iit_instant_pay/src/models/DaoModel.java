package models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import utils.CommonUtilities;

/*
 * Database Access Object
 * Class to interact with database
 */
public class DaoModel {
	public static final String ACCOUNTS = "Accounts";
	public static final String TRANSACTIONS = "Transactions";
	public static final String PAYEES = "Payees";

	DBConnect db = null;

	public DaoModel() {
		db = new DBConnect();
	}

	// Private method that returns connection to the DB initialized in constructor
	private Connection getDbConn() throws SQLException {
		String protocol = "jdbc";
		String subprotocol = "mysql";
		String host = "localhost";
		short port = 3306;
		String schema = "iit_instant_pay";
		String connOptions = "autoReconnect=true&useSSL=false";

		String connUrl = host + ":" + port + "/" + schema + "?" + connOptions;

		// Database credentials
		String username = "root";
		String password = "password";

		return db.getConnection(protocol, subprotocol, connUrl, username, password);
	}

	public Integer validateCredentials(String email, String password) throws IllegalAccessException {
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

				return bankAccount;
			}
		} catch (SQLException se) {
			throw new IllegalStateException("Unable to fetch account details. " + se.getMessage());
		}
	}

	public BankAccount getBankAccountByEmail(String email) {
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

				return bankAccount;
			}
		} catch (SQLException se) {
			throw new IllegalStateException("Unable to fetch account details. " + se.getMessage());
		}
	}

	public List<RecentTransaction> getRecentTransactions(Integer accountId) {
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
			}

			return recentTransactions;
		} catch (SQLException se) {
			throw new IllegalStateException("Unable to fetch recent transactions. " + se.getMessage());
		}
	}

	public List<Payee> getPayees(Integer accountId) {
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
				System.out.println("No recent transactions found.");
			} else {
				while (results.next()) {
					Payee p = new Payee();

					p.setAccountId(results.getInt("AccountId"));
					p.setName(results.getString("Name"));
					p.setEmail(results.getString("Email"));

					payees.add(p);
				}
			}

			return payees;
		} catch (SQLException se) {
			throw new IllegalStateException("Unable to fetch payees. " + se.getMessage());
		}
	}

	public BankAccount createAccount(String name, String email, String password) {
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

			return this.getBankAccountByEmail(email);
		} catch (SQLException se) {
			throw new IllegalStateException("Unable to create account. " + se.getMessage());
		}
	}

	public void addPayee(Integer payerAccountId, String payeeEmail) {
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
		} catch (SQLException se) {
			throw new IllegalStateException("Unable to add payee. " + se.getMessage());
		}
	}

	public String executeTransfer(Integer fromAccountId, Integer toAccountId, Double amount, String remarks) {
		try (Connection conn = getDbConn()) {
			String sql = String.format("""
					CALL Transfer(?, ?, ?, ?);
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
			throw new IllegalStateException("Unable to add payee. " + se.getMessage());
		}
	}
	
	public void updateBankAccount(Integer accountId, String email, String name, Double balance) {
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
		} catch (SQLException se) {
			throw new IllegalStateException("Unable to update account. " + se.getMessage());
		}
	}
	
	/**
	 * Disable payee
	 */
	public void deletePayee(Integer payerAccountId, Integer payeeAccountId) {
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
		} catch (SQLException se) {
			throw new IllegalStateException("Unable to remove payee. " + se.getMessage());
		}
	}
}
