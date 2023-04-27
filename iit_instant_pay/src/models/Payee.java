package models;

public class Payee {
	Integer accountId;
	String name;
	String email;
	
	public Payee() {
		super();
	}

	public Payee(Integer accountId, String name, String email) {
		super();
		this.accountId = accountId;
		this.name = name;
		this.email = email;
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
	
	
}

