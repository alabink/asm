package com.example.todolistse06302.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.todolistse06302.Budget;
import com.example.todolistse06302.Expense;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "expense_tracker";
    private static final int DATABASE_VERSION = 5;

    // User table constants
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "id";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD = "password";

    // Expense table constants
    public static final String TABLE_EXPENSES = "expenses";
    public static final String COLUMN_EXPENSE_ID = "id";
    public static final String COLUMN_AMOUNT = "amount";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_DATE = "date";

    // Budget table constants
    public static final String TABLE_BUDGETS = "budgets";
    public static final String COLUMN_BUDGET_ID = "id";
    public static final String COLUMN_TOTAL_BUDGET = "total_budget";
    public static final String COLUMN_REMAINING_BUDGET = "remaining_budget";
    public static final String COLUMN_BUDGET_CATEGORY = "category";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            // Create users table
            String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + " ("
                    + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_EMAIL + " TEXT NOT NULL, "
                    + COLUMN_PASSWORD + " TEXT NOT NULL)";
            db.execSQL(CREATE_USERS_TABLE);

            // Create expenses table
            String CREATE_EXPENSES_TABLE = "CREATE TABLE " + TABLE_EXPENSES + " ("
                    + COLUMN_EXPENSE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_AMOUNT + " REAL NOT NULL, "
                    + COLUMN_CATEGORY + " TEXT NOT NULL, "
                    + COLUMN_DATE + " TEXT NOT NULL)";
            db.execSQL(CREATE_EXPENSES_TABLE);

            // Create budgets table
            String CREATE_BUDGETS_TABLE = "CREATE TABLE " + TABLE_BUDGETS + " ("
                    + COLUMN_BUDGET_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_TOTAL_BUDGET + " REAL NOT NULL, "
                    + COLUMN_REMAINING_BUDGET + " REAL NOT NULL, "
                    + COLUMN_BUDGET_CATEGORY + " TEXT NOT NULL)";
            db.execSQL(CREATE_BUDGETS_TABLE);

            Log.d("DatabaseHelper", "All tables created successfully");
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error creating tables", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            // Drop existing tables
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUDGETS);

            // Recreate tables
            onCreate(db);
            Log.d("DatabaseHelper", "Database upgraded");
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error upgrading database", e);
        }
    }

    // Add Budget
    public void addBudget(double totalBudget, String category) throws Exception {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COLUMN_TOTAL_BUDGET, totalBudget);
            values.put(COLUMN_REMAINING_BUDGET, totalBudget);
            values.put(COLUMN_BUDGET_CATEGORY, category);

            long result = db.insertOrThrow(TABLE_BUDGETS, null, values);
            if (result == -1) {
                throw new Exception("Failed to insert budget");
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error adding budget", e);
            throw e;
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

//    // Update Budget
//    public void updateBudget(int id, double totalBudget, String category) throws Exception {
//        SQLiteDatabase db = null;
//        try {
//            db = this.getWritableDatabase();
//            ContentValues values = new ContentValues();
//            values.put(COLUMN_TOTAL_BUDGET, totalBudget);
//            values.put(COLUMN_REMAINING_BUDGET, totalBudget);
//            values.put(COLUMN_BUDGET_CATEGORY, category);
//
//            int rowsAffected = db.update(TABLE_BUDGETS, values,
//                    COLUMN_BUDGET_ID + "=?", new String[]{String.valueOf(id)});
//            if (rowsAffected == 0) {
//                throw new Exception("Budget not found");
//            }
//        } catch (Exception e) {
//            Log.e("DatabaseHelper", "Error updating budget", e);
//            throw e;
//        } finally {
//            if (db != null && db.isOpen()) {
//                db.close();
//            }
//        }
//    }

    // Deduct from Budget
    public void deductFromBudget(String category, double amount) throws Exception {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getWritableDatabase();
            cursor = db.query(TABLE_BUDGETS,
                    new String[]{COLUMN_BUDGET_ID, COLUMN_REMAINING_BUDGET},
                    COLUMN_BUDGET_CATEGORY + "=?",
                    new String[]{category},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int budgetId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BUDGET_ID));
                double remainingBudget = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_REMAINING_BUDGET));

                double newRemainingBudget = remainingBudget - amount;

                ContentValues values = new ContentValues();
                values.put(COLUMN_REMAINING_BUDGET, newRemainingBudget);

                int rowsAffected = db.update(TABLE_BUDGETS, values,
                        COLUMN_BUDGET_ID + "=?",
                        new String[]{String.valueOf(budgetId)});

                if (rowsAffected == 0) {
                    throw new Exception("Failed to update budget");
                }
            } else {
                throw new Exception("No budget found for category: " + category);
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error deducting from budget", e);
            throw e;
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    // Get Budgets
    public List<Budget> getBudgets() {
        List<Budget> budgets = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = this.getReadableDatabase();
            cursor = db.query(TABLE_BUDGETS,
                    new String[]{COLUMN_BUDGET_ID, COLUMN_TOTAL_BUDGET, COLUMN_REMAINING_BUDGET, COLUMN_BUDGET_CATEGORY},
                    null, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    budgets.add(new Budget(
                            cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BUDGET_ID)),
                            cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_TOTAL_BUDGET)),
                            cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_REMAINING_BUDGET)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BUDGET_CATEGORY))
                    ));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting budgets", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        return budgets;
    }

    // Delete Budget
    public void deleteBudget(int id) throws Exception {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            int rowsAffected = db.delete(TABLE_BUDGETS,
                    COLUMN_BUDGET_ID + "=?", new String[]{String.valueOf(id)});
            if (rowsAffected == 0) {
                throw new Exception("Budget not found");
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error deleting budget", e);
            throw e;
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }
    // Phương thức mới để lấy ngân sách còn lại theo category
    public double getRemainingBudgetByCategory(String category) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        double remainingBudget = 0;

        try {
            db = this.getReadableDatabase();
            cursor = db.query(TABLE_BUDGETS,
                    new String[]{COLUMN_REMAINING_BUDGET},
                    COLUMN_BUDGET_CATEGORY + "=?",
                    new String[]{category},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                remainingBudget = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_REMAINING_BUDGET));
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting remaining budget", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        return remainingBudget;
    }

    // Add User
    public void addUser(String email, String password) throws Exception {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COLUMN_EMAIL, email);
            values.put(COLUMN_PASSWORD, password);

            long result = db.insertOrThrow(TABLE_USERS, null, values);
            if (result == -1) {
                throw new Exception("Failed to insert user");
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error adding user", e);
            throw e;
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    // Get Users
    public List<String[]> getUsers() {
        List<String[]> users = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = this.getReadableDatabase();
            cursor = db.query(TABLE_USERS,
                    new String[]{COLUMN_EMAIL, COLUMN_PASSWORD},
                    null, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    users.add(new String[]{
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD))
                    });
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting users", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        return users;
    }

    // Add Expense
    public void addExpense(double amount, String category, String date) throws Exception {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COLUMN_AMOUNT, amount);
            values.put(COLUMN_CATEGORY, category);
            values.put(COLUMN_DATE, date);

            long result = db.insertOrThrow(TABLE_EXPENSES, null, values);
            if (result == -1) {
                throw new Exception("Failed to insert expense");
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error adding expense", e);
            throw e;
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    // Update Expense
    public void updateExpense(int id, double amount, String category, String date) throws Exception {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COLUMN_AMOUNT, amount);
            values.put(COLUMN_CATEGORY, category);
            values.put(COLUMN_DATE, date);

            int rowsAffected = db.update(TABLE_EXPENSES, values,
                    COLUMN_EXPENSE_ID + "=?", new String[]{String.valueOf(id)});
            if (rowsAffected == 0) {
                throw new Exception("Expense not found");
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error updating expense", e);
            throw e;
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    // Delete Expense
    public void deleteExpense(int id) throws Exception {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            int rowsAffected = db.delete(TABLE_EXPENSES,
                    COLUMN_EXPENSE_ID + "=?", new String[]{String.valueOf(id)});
            if (rowsAffected == 0) {
                throw new Exception("Expense not found");
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error deleting expense", e);
            throw e;
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    // Get Expenses
    public List<String[]> getExpenses() {
        List<String[]> expenses = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = this.getReadableDatabase();
            cursor = db.query(TABLE_EXPENSES,
                    new String[]{COLUMN_EXPENSE_ID, COLUMN_AMOUNT, COLUMN_CATEGORY, COLUMN_DATE},
                    null, null, null, null,
                    COLUMN_DATE + " DESC");

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    expenses.add(new String[]{
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXPENSE_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE))
                    });
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting expenses", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        return expenses;
    }
}