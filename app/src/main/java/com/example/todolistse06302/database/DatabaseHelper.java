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

    private static final String DATABASE_NAME = "concac";
    private static final int DATABASE_VERSION = 8;

    // User table constants
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "id";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_USER_ROLE = "role";
    public static final String ROLE_USER = "USER";
    public static final String ROLE_ADMIN = "ADMIN";

    // Expense table constants
    public static final String TABLE_EXPENSES = "expenses";
    public static final String COLUMN_EXPENSE_ID = "id";
    public static final String COLUMN_AMOUNT = "amount";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_EXPENSE_USER_ID = "user_id";

    // Budget table constants
    public static final String TABLE_BUDGETS = "budgets";
    public static final String COLUMN_BUDGET_ID = "id";
    public static final String COLUMN_TOTAL_BUDGET = "total_budget";
    public static final String COLUMN_REMAINING_BUDGET = "remaining_budget";
    public static final String COLUMN_BUDGET_CATEGORY = "category";
    public static final String COLUMN_BUDGET_USER_ID = "user_id";

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
                    + COLUMN_PASSWORD + " TEXT NOT NULL, "
                    + COLUMN_USER_ROLE + " TEXT NOT NULL DEFAULT '" + ROLE_USER + "')";
            db.execSQL(CREATE_USERS_TABLE);

            // Create expenses table
            String CREATE_EXPENSES_TABLE = "CREATE TABLE " + TABLE_EXPENSES + " ("
                    + COLUMN_EXPENSE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_AMOUNT + " REAL NOT NULL, "
                    + COLUMN_CATEGORY + " TEXT NOT NULL, "
                    + COLUMN_DATE + " TEXT NOT NULL, "
                    + COLUMN_EXPENSE_USER_ID + " INTEGER, "
                    + "FOREIGN KEY(" + COLUMN_EXPENSE_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + "))";
            db.execSQL(CREATE_EXPENSES_TABLE);

            // Create budgets table
            String CREATE_BUDGETS_TABLE = "CREATE TABLE " + TABLE_BUDGETS + " ("
                    + COLUMN_BUDGET_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_TOTAL_BUDGET + " REAL NOT NULL, "
                    + COLUMN_REMAINING_BUDGET + " REAL NOT NULL, "
                    + COLUMN_BUDGET_CATEGORY + " TEXT NOT NULL, "
                    + COLUMN_BUDGET_USER_ID + " INTEGER, "
                    + "FOREIGN KEY(" + COLUMN_BUDGET_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + "))";
            db.execSQL(CREATE_BUDGETS_TABLE);

            // Create default admin account
            ContentValues adminValues = new ContentValues();
            adminValues.put(COLUMN_EMAIL, "admin@admin.com");
            adminValues.put(COLUMN_PASSWORD, "admin123");
            adminValues.put(COLUMN_USER_ROLE, ROLE_ADMIN);
            db.insert(TABLE_USERS, null, adminValues);

            Log.d("DatabaseHelper", "All tables created successfully and admin account created");
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
    public void addBudget(double totalBudget, String category, int userId) throws Exception {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COLUMN_TOTAL_BUDGET, totalBudget);
            values.put(COLUMN_REMAINING_BUDGET, totalBudget);
            values.put(COLUMN_BUDGET_CATEGORY, category);
            values.put(COLUMN_BUDGET_USER_ID, userId);

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

    // Deduct from Budget
    public void deductFromBudget(String category, double amount, int userId) throws Exception {
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = this.getWritableDatabase();
            db.beginTransaction();

            // Get current remaining budget
            cursor = db.query(TABLE_BUDGETS,
                    new String[]{COLUMN_BUDGET_ID, COLUMN_REMAINING_BUDGET},
                    COLUMN_BUDGET_CATEGORY + "=? AND " + COLUMN_BUDGET_USER_ID + "=?",
                    new String[]{category, String.valueOf(userId)},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int budgetId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BUDGET_ID));
                double remainingBudget = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_REMAINING_BUDGET));

                if (remainingBudget < amount) {
                    throw new Exception("Insufficient budget");
                }

                // Update remaining budget
                ContentValues values = new ContentValues();
                values.put(COLUMN_REMAINING_BUDGET, remainingBudget - amount);
                
                int result = db.update(TABLE_BUDGETS, values,
                        COLUMN_BUDGET_ID + "=?",
                        new String[]{String.valueOf(budgetId)});
                
                if (result <= 0) {
                    throw new Exception("Failed to update budget");
                }
            } else {
                throw new Exception("No budget found for category: " + category);
            }

            db.setTransactionSuccessful();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.endTransaction();
                if (db.isOpen()) {
                    db.close();
                }
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

    // Get Budgets for specific user
    public List<Budget> getBudgetsForUser(int userId) {
        List<Budget> budgets = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = this.getReadableDatabase();
            cursor = db.query(TABLE_BUDGETS,
                    new String[]{COLUMN_BUDGET_ID, COLUMN_TOTAL_BUDGET, COLUMN_REMAINING_BUDGET, COLUMN_BUDGET_CATEGORY},
                    COLUMN_BUDGET_USER_ID + " = ?",
                    new String[]{String.valueOf(userId)},
                    null, null, null);

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
            Log.e("DatabaseHelper", "Error getting budgets for user", e);
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

    // Add User with default role
    public long addUser(String email, String password) throws Exception {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COLUMN_EMAIL, email);
            values.put(COLUMN_PASSWORD, password);
            values.put(COLUMN_USER_ROLE, ROLE_USER);

            long userId = db.insertOrThrow(TABLE_USERS, null, values);
            if (userId == -1) {
                throw new Exception("Failed to insert user");
            }
            return userId;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error adding user", e);
            throw e;
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    // New method to update user role
    public void updateUserRole(int userId, String newRole) throws Exception {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COLUMN_USER_ROLE, newRole);

            int rowsAffected = db.update(TABLE_USERS, values, COLUMN_USER_ID + " = ?",
                    new String[]{String.valueOf(userId)});
            if (rowsAffected != 1) {
                throw new Exception("Failed to update user role");
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error updating user role", e);
            throw e;
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    // New method to get user role
    public String getUserRole(int userId) throws Exception {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getReadableDatabase();
            cursor = db.query(TABLE_USERS,
                    new String[]{COLUMN_USER_ROLE},
                    COLUMN_USER_ID + " = ?",
                    new String[]{String.valueOf(userId)},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int roleColumnIndex = cursor.getColumnIndexOrThrow(COLUMN_USER_ROLE);
                return cursor.getString(roleColumnIndex);
            }
            throw new Exception("User not found");
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting user role", e);
            throw e;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }



//     Modified getExpenses to filter by userId
//    public Cursor getExpenses(int userId) {
//        SQLiteDatabase db = this.getReadableDatabase();
//        return db.query(TABLE_EXPENSES,
//                null,
//                COLUMN_EXPENSE_USER_ID + " = ?",
//                new String[]{String.valueOf(userId)},
//                null, null, COLUMN_DATE + " DESC");
//    }
public Cursor getExpenses(int userId) {
    SQLiteDatabase db = this.getReadableDatabase();
    return db.query(TABLE_EXPENSES,
            null,
            COLUMN_EXPENSE_USER_ID + " = ?",
            new String[]{String.valueOf(userId)},
            null, null, COLUMN_DATE + " DESC");
}



    // Modified addExpense to include userId
    public void addExpense(double amount, String category, String date, int userId) throws Exception {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COLUMN_AMOUNT, amount);
            values.put(COLUMN_CATEGORY, category);
            values.put(COLUMN_DATE, date);
            values.put(COLUMN_EXPENSE_USER_ID, userId);

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

    // Phương thức để trả lại số tiền budget về trạng thái ban đầu
    public void addBudgetAmountBack(String category, double amount, int userId) throws Exception {
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = this.getWritableDatabase();
            db.beginTransaction();

            // Tìm budget của category và user
            cursor = db.query(TABLE_BUDGETS,
                    new String[]{COLUMN_BUDGET_ID, COLUMN_REMAINING_BUDGET},
                    COLUMN_BUDGET_CATEGORY + "=? AND " + COLUMN_BUDGET_USER_ID + "=?",
                    new String[]{category, String.valueOf(userId)},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int budgetId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BUDGET_ID));
                double currentRemainingBudget = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_REMAINING_BUDGET));

                // Cập nhật lại số tiền budget
                ContentValues values = new ContentValues();
                values.put(COLUMN_REMAINING_BUDGET, currentRemainingBudget + amount);

                int result = db.update(TABLE_BUDGETS, values,
                        COLUMN_BUDGET_ID + "=?",
                        new String[]{String.valueOf(budgetId)});

                if (result <= 0) {
                    throw new Exception("Failed to update budget");
                }
            } else {
                throw new Exception("No budget found for category: " + category);
            }

            db.setTransactionSuccessful();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.endTransaction();
                if (db.isOpen()) {
                    db.close();
                }
            }
        }
    }
    // New method to check if user is admin
    public boolean isAdmin(int userId) throws Exception {
        String role = getUserRole(userId);
        return ROLE_ADMIN.equals(role);
    }

    // New method to get all users (for admin only)
    public Cursor getAllUsers() {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = new String[]{
            COLUMN_USER_ID,
            COLUMN_EMAIL,
            COLUMN_USER_ROLE
        };
        return db.query(TABLE_USERS,
                columns,
                null, null, null, null, null);
    }

    // New method to delete user (for admin only)
    public void deleteUser(int userId) throws Exception {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            db.beginTransaction();
            
            // Delete user's expenses first
            db.delete(TABLE_EXPENSES, COLUMN_EXPENSE_USER_ID + " = ?", 
                    new String[]{String.valueOf(userId)});
            
            // Delete the user
            int result = db.delete(TABLE_USERS, COLUMN_USER_ID + " = ?", 
                    new String[]{String.valueOf(userId)});
            
            if (result != 1) {
                throw new Exception("Failed to delete user");
            }
            
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error deleting user", e);
            throw e;
        } finally {
            if (db != null) {
                db.endTransaction();
                if (db.isOpen()) {
                    db.close();
                }
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

    // Get remaining budget for a specific category and user
    public double getRemainingBudgetByCategory(String category, int userId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        double remainingBudget = 0;

        try {
            db = this.getReadableDatabase();
            cursor = db.query(TABLE_BUDGETS,
                    new String[]{COLUMN_REMAINING_BUDGET},
                    COLUMN_BUDGET_CATEGORY + "=? AND " + COLUMN_BUDGET_USER_ID + "=?",
                    new String[]{category, String.valueOf(userId)},
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

    // Get expenses for specific user
    public List<Expense> getExpensesForUser(int userId) {
        List<Expense> expenses = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = this.getReadableDatabase();
            cursor = db.query(TABLE_EXPENSES,
                    null,
                    COLUMN_EXPENSE_USER_ID + "=?",
                    new String[]{String.valueOf(userId)},
                    null, null,
                    COLUMN_DATE + " DESC");

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int idColumnIndex = cursor.getColumnIndexOrThrow(COLUMN_EXPENSE_ID);
                    int amountColumnIndex = cursor.getColumnIndexOrThrow(COLUMN_AMOUNT);
                    int categoryColumnIndex = cursor.getColumnIndexOrThrow(COLUMN_CATEGORY);
                    int dateColumnIndex = cursor.getColumnIndexOrThrow(COLUMN_DATE);

                    Expense expense = new Expense(
                        cursor.getInt(idColumnIndex),
                        cursor.getDouble(amountColumnIndex),
                        cursor.getString(categoryColumnIndex),
                        cursor.getString(dateColumnIndex)
                    );
                    expenses.add(expense);
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

    // Get budget categories for a specific user
    public List<String> getBudgetCategoriesForUser(int userId) {
        List<String> categories = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = this.getReadableDatabase();
            cursor = db.query(true, // distinct
                    TABLE_BUDGETS,
                    new String[]{COLUMN_BUDGET_CATEGORY},
                    COLUMN_BUDGET_USER_ID + " = ?",
                    new String[]{String.valueOf(userId)},
                    null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    categories.add(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BUDGET_CATEGORY)));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting budget categories", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        return categories;
    }






//    public List<String[]> getExpenses(int userId) {
//        List<String[]> expensesList = new ArrayList<>();
//        SQLiteDatabase db = this.getReadableDatabase();
//
//        Cursor cursor = db.rawQuery("SELECT amount, date FROM expenses WHERE user_id = ?",
//                new String[]{String.valueOf(userId)});
//
//        Log.d("DEBUG", "Querying expenses for userID: " + userId);
//
//        if (cursor != null) {
//            while (cursor.moveToNext()) {
//                String amount = cursor.getString(0);
//                String date = cursor.getString(1);
//                expensesList.add(new String[]{amount, date});
//                Log.d("DEBUG", "Fetched expense: " + amount + " - " + date);
//            }
//            cursor.close();
//        }
//        db.close();
//        return expensesList;
//    }









}