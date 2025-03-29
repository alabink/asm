package com.example.todolistse06302;

public class Budget {
    private int id;
    private double totalBudget;
    private double remainingBudget;
    private String category;

    public Budget(int id, double totalBudget, double remainingBudget, String category) {
        this.id = id;
        this.totalBudget = totalBudget;
        this.remainingBudget = remainingBudget;
        this.category = category;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public double getTotalBudget() { return totalBudget; }
    public void setTotalBudget(double totalBudget) { this.totalBudget = totalBudget; }

    public double getRemainingBudget() { return remainingBudget; }
    public void setRemainingBudget(double remainingBudget) { this.remainingBudget = remainingBudget; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

}
//dbl code done

