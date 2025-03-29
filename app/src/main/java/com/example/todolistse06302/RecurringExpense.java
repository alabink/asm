package com.example.todolistse06302;

public class RecurringExpense {
    private int id;
    private String name;
    private double amount;
    private String startDate;
    private String endDate;

    public RecurringExpense(int id, String name, double amount, String startDate, String endDate) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() {return name; }
    public void setName(String name){this.name= name;}

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
}
