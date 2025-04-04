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
import com.example.todolistse06302.RecurringExpense;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;


public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "concac";
    private static final int DATABASE_VERSION = 20;

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
    //Recurring table
    public static final String TABLE_RECURRING = "RecurringExpenses";
    public static final String COLUMN_RECURRING_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_AMOUNTT = "amountt";
    public static final String COLUMN_START_DATE = "start_date";
    public static final String COLUMN_END_DATE = "end_date";
    public static final String COLUMN_RECURRING_USER_ID = "user_id";

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
            //Create recurring table
            String createTable = "CREATE TABLE " + TABLE_RECURRING + " (" +
                    COLUMN_RECURRING_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME + " TEXT NOT NULL, " +
                    COLUMN_AMOUNTT + " REAL NOT NULL, " +
                    COLUMN_START_DATE + " TEXT NOT NULL, " +
                    COLUMN_END_DATE + " TEXT NOT NULL,"
                    + COLUMN_RECURRING_USER_ID + " INTEGER, "
                    + "FOREIGN KEY(" + COLUMN_RECURRING_USER_ID + ") REFERENCES " + TABLE_RECURRING + "(" + COLUMN_RECURRING_ID + "))";
            db.execSQL(createTable);

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
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECURRING);

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
    public List<String> getAllUserEmails() {
        List<String> users = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT email FROM users", null);

        if (cursor.moveToFirst()) {
            do {
                users.add(cursor.getString(0)); // Lấy email
            } while (cursor.moveToNext());
        }

        cursor.close();
        return users;
    }

    public int getUserIdByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM users WHERE email = ?", new String[]{email});

        if (cursor.moveToFirst()) {
            return cursor.getInt(0); // Trả về ID
        }

        cursor.close();
        return -1;
    }

    public List<String> getExpenseSummaryForUser(int userId) {
        List<String> summaryList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT e.category, SUM(e.amount) AS totalSpent, " +
                        "(b.total_budget - SUM(e.amount)) AS remainingBudget " +
                        "FROM expenses e " +
                        "JOIN budgets b ON e.category = b.category AND e.user_id = b.user_id " +
                        "WHERE e.user_id = ? " +
                        "GROUP BY e.category, b.total_budget",
                new String[]{String.valueOf(userId)}
        );

        if (cursor.moveToFirst()) {
            do {
                String category = cursor.getString(0);
                double totalSpent = cursor.getDouble(1);
                double remainingBudget = cursor.getDouble(2);

                summaryList.add(category + ": Spent $" + totalSpent + " | Budget Left: $" + remainingBudget);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return summaryList;
    }
    public HashMap<String, Double> getTotalExpenseAndBudget(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        HashMap<String, Double> result = new HashMap<>();

        // Tính tổng số tiền đã chi tiêu
        Cursor cursor = db.rawQuery(
                "SELECT IFNULL(SUM(amount), 0) FROM expenses WHERE user_id = ?",
                new String[]{String.valueOf(userId)}
        );

        if (cursor.moveToFirst()) {
            result.put("totalSpent", cursor.getDouble(0));
        }
        cursor.close();

        // Tính tổng ngân sách còn lại
        cursor = db.rawQuery(
                "SELECT IFNULL(SUM(total_budget - IFNULL(e.amount, 0)), 0) " +
                        "FROM budgets b " +
                        "LEFT JOIN expenses e ON b.category = e.category AND b.user_id = e.user_id " +
                        "WHERE b.user_id = ?",
                new String[]{String.valueOf(userId)}
        );

        if (cursor.moveToFirst()) {
            result.put("remainingBudget", cursor.getDouble(0));
        }
        cursor.close();

        return result;
    }
    // ham barchart
    public List<String> getExpenseCategories() {
        List<String> categories = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT DISTINCT category FROM expenses", null);

        if (cursor.moveToFirst()) {
            do {
                categories.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return categories;
    }

    public float getTotalExpenseForCategory(int userId, String category) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(amount) FROM expenses WHERE user_id = ? AND category = ?",
                new String[]{String.valueOf(userId), category});

        float total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getFloat(0);
        }
        cursor.close();
        return total;
    }
    public List<String> getExpenseSummaryForUserByCategory(int userId, String category) {
        List<String> summaryList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT e.category, SUM(e.amount) AS totalSpent, " +
                        "(b.total_budget - SUM(e.amount)) AS remainingBudget " +
                        "FROM expenses e " +
                        "JOIN budgets b ON e.category = b.category AND e.user_id = b.user_id " +
                        "WHERE e.user_id = ? AND e.category = ? " +
                        "GROUP BY e.category, b.total_budget",
                new String[]{String.valueOf(userId), category}
        );

        if (cursor.moveToFirst()) {
            do {
                double totalSpent = cursor.getDouble(1);
                double remainingBudget = cursor.getDouble(2);
                summaryList.add(category + ": Spent $" + totalSpent + " | Budget Left: $" + remainingBudget);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return summaryList;
    }
    public List<String> getExpenseCategoriesForUser(int userId) {
        List<String> categories = new ArrayList<>();

        // Truy vấn các danh mục chi tiêu mà người dùng đã chi tiền cho
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT DISTINCT category FROM expenses WHERE user_id = ?",
                new String[]{String.valueOf(userId)}
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String category = cursor.getString(0); // Lấy danh mục chi tiêu
                categories.add(category);
            } while (cursor.moveToNext());

            cursor.close();
        }

        return categories;
    }
    // recurring
    public void addRecurringExpense(String name, double amount, String startDate, String endDate, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_AMOUNTT, amount);
        values.put(COLUMN_START_DATE, startDate);
        values.put(COLUMN_END_DATE, endDate);
        values.put(COLUMN_RECURRING_USER_ID, userId);

        long result = db.insert(TABLE_RECURRING, null, values);
        if (result == -1) {
            Log.e("ERROR", "Failed to insert expense for user ID: " + userId);
        } else {
            Log.d("DEBUG", "Inserted expense ID: " + result);
        }
        db.close();
    }

    public List<RecurringExpense> getRecurringExpenses(int userId) {
        List<RecurringExpense> expenses = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_RECURRING, null, COLUMN_RECURRING_USER_ID + "=?", new String[]{String.valueOf(userId)}, null, null, null);

        if (cursor != null) {
            Log.d("DEBUG", "Found " + cursor.getCount() + " expenses for user ID: " + userId);
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RECURRING_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_AMOUNTT));
                String startDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_START_DATE));
                String endDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_END_DATE));


                expenses.add(new RecurringExpense(id, name, amount, startDate, endDate));
            }
            cursor.close();
        } else {
            Log.e("ERROR", "Failed to load expenses for user ID: " + userId);
        }

        db.close();
        return expenses;
    }


    public void editcost(int expenseId, String name, double amount, String startDate, String endDate, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_AMOUNTT, amount);
        values.put(COLUMN_START_DATE, startDate);
        values.put(COLUMN_END_DATE, endDate);

        // Sử dụng tên cột đúng
        db.update(TABLE_RECURRING, values, COLUMN_RECURRING_ID + " = ? AND " + COLUMN_RECURRING_USER_ID + " = ?", new String[]{String.valueOf(expenseId), String.valueOf(userId)});
        db.close();
    }
    public void deleteCost(int expenseId, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_RECURRING, COLUMN_RECURRING_ID + " = ? AND " + COLUMN_RECURRING_USER_ID + " = ?", new String[]{String.valueOf(expenseId), String.valueOf(userId)});
        db.close();
    }










}