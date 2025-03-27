package com.example.todolistse06302.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import android.widget.ListView;
import android.widget.Toast;


public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "concac";
    private static final int DATABASE_VERSION = 6;

    // User table
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD = "password";

    // Expense table
    private static final String TABLE_EXPENSES = "expenses";
    private static final String COLUMN_EXPENSE_ID = "id";
    private static final String COLUMN_AMOUNT = "amount";
    private static final String COLUMN_CATEGORY = "category";
    private static final String COLUMN_DATE = "date";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_EMAIL + " TEXT, "
                + COLUMN_PASSWORD + " TEXT)";
        db.execSQL(CREATE_USERS_TABLE);

        String CREATE_EXPENSES_TABLE = "CREATE TABLE " + TABLE_EXPENSES + " ("
                + COLUMN_EXPENSE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_AMOUNT + " REAL, "
                + COLUMN_CATEGORY + " TEXT, "
                + COLUMN_DATE + " TEXT)";
        db.execSQL(CREATE_EXPENSES_TABLE);

        // Thêm bảng budget để lưu ngân sách cho từng danh mục
        String CREATE_BUDGET_TABLE = "CREATE TABLE Budget (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "category TEXT UNIQUE, " +
                "amount REAL)";
        db.execSQL(CREATE_BUDGET_TABLE);

    }
    public void setBudget(String category, double amount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("category", category);
        values.put("amount", amount);

        long result = db.insertWithOnConflict("Budget", null, values, SQLiteDatabase.CONFLICT_REPLACE); // FIXED
        if (result == -1) {
            Log.e("DB_ERROR", "Failed to insert Budget");
        }
        db.close();
    }


    public Cursor getBudgetHistory() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT category, amount FROM Budget ORDER BY id DESC", null);
    }



    // Add User
    public void addUser(String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, password);
        db.insert(TABLE_USERS, null, values);
        db.close();
    }
    // Get Users
    public List<String[]> getUsers() {
        List<String[]> users = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS, null);
        if (cursor.moveToFirst()) {
            do {
                users.add(new String[]{cursor.getString(1), cursor.getString(2)});
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return users;
    }

    // Add Expense
    public void addExpense(double amount, String category, String date) {
        SQLiteDatabase db = this.getWritableDatabase();

        double currentBudget = getBudget(category);
        if (currentBudget > 0 && amount > currentBudget) {
            Log.d("BudgetWarning", "Bạn đã vượt quá ngân sách cho danh mục: " + category);
            return; // Ngăn không cho thêm nếu vượt quá ngân sách
        }

        ContentValues values = new ContentValues();
        values.put("amount", amount);
        values.put("category", category);
        values.put("date", date);

        db.insert("expenses", null, values);
        db.close();
    }
    public List<String[]> getAllBudgets() {
        List<String[]> budgets = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Budget", null);

        if (cursor.moveToFirst()) {
            do {
                String category = cursor.getString(cursor.getColumnIndexOrThrow("category"));
                String amount = cursor.getString(cursor.getColumnIndexOrThrow("amount"));
                budgets.add(new String[]{category, amount});
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return budgets;
    }


    public double getTotalSpent(String category) {
        SQLiteDatabase db = this.getReadableDatabase();
        double totalSpent = 0;

        Cursor cursor = db.rawQuery("SELECT SUM(amount) FROM expenses WHERE category = ?", new String[]{category});
        if (cursor.moveToFirst()) {
            totalSpent = cursor.getDouble(0);
        }
        cursor.close();
        db.close();

        return totalSpent;
    }

    // Update Expense
    public void updateExpense(int id, double amount, String category, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_AMOUNT, amount);
        values.put(COLUMN_CATEGORY, category);
        values.put(COLUMN_DATE, date);
        db.update(TABLE_EXPENSES, values, COLUMN_EXPENSE_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    // Delete Expense
    public void deleteExpense(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_EXPENSES, COLUMN_EXPENSE_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public List<String[]> getExpenses() {
        List<String[]> expenses = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery("SELECT * FROM " + TABLE_EXPENSES, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXPENSE_ID));
                    String amount = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT));
                    String category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY));
                    String date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE));
                    expenses.add(new String[]{id, amount, category, date});
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }

        return expenses;
    }
    public double getBudget(String category) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT amount FROM Budget WHERE category = ?", new String[]{category}); // FIXED
        if (cursor.moveToFirst()) {
            double budget = cursor.getDouble(0);
            cursor.close();
            return budget;
        }
        cursor.close();
        return 0; // Nếu chưa có ngân sách, trả về 0
    }
    public void clearAllBudgets() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM budget");
        db.close();
    }
    public void deleteBudget(String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("budget", "category=?", new String[]{category});
        db.close();
    }



    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSES);
        db.execSQL("DROP TABLE IF EXISTS Budget");
        db.execSQL("DROP TABLE IF EXISTS listViewExpenses");
        onCreate(db);
    }
}
