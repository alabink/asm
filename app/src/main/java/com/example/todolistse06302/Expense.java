package com.example.todolistse06302;

public class Expense {
    private int id;
    private double amount;
    private String category;
    private String date;

    public Expense(int id, double amount, String category, String date) {
        this.id = id;
        this.amount = amount;
        this.category = category;
        this.date = date;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    // ✅ Thêm phương thức mới: Hiển thị thông tin chi tiêu
    public String getExpenseInfo() {
        return "ID: " + id + ", Số tiền: " + amount + " VND, Danh mục: " + category + ", Ngày: " + date;
    }

    // ✅ Thêm phương thức so sánh chi tiêu
    public boolean isExpensive(double threshold) {
        return this.amount > threshold;
    }
}
