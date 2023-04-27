package models;

public class RecentTransaction {
	private String date; 
	private String type;
	private String description;
	private Double amount;
	
	
	
	public RecentTransaction() {
		super();
	}

	public RecentTransaction(String date, String type, String description, Double amount) {
		super();
		this.date = date;
		this.type = type;
		this.description = description;
		this.amount = amount;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}
	
	
}
