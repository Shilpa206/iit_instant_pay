package models;

import java.util.List;

public class BankAccount {
	Integer accountId;
	String name;
	String email;
	Double balance;
	List<RecentTransaction> recentTransactions;
	List<Payee> payees;
	
	public BankAccount() {
		super();
	}

	public BankAccount(Integer accountId, String name, String email, Double balance,
			List<RecentTransaction> recentTransactions, List<Payee> payees) {
		super();
		this.accountId = accountId;
		this.name = name;
		this.email = email;
		this.balance = balance;
		this.recentTransactions = recentTransactions;
		this.payees = payees;
	}

	public Integer getAccountId() {
		return accountId;
	}

	public void setAccountId(Integer accountId) {
		this.accountId = accountId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Double getBalance() {
		return balance;
	}

	public void setBalance(Double balance) {
		this.balance = balance;
	}

	public List<RecentTransaction> getRecentTransactions() {
		return recentTransactions;
	}

	public void setRecentTransactions(List<RecentTransaction> recentTransactions) {
		this.recentTransactions = recentTransactions;
	}

	public List<Payee> getPayees() {
		return payees;
	}

	public void setPayees(List<Payee> payees) {
		this.payees = payees;
	}
}
