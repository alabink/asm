package com.example.todolistse06302.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.todolistse06302.RecurringExpense;

import java.util.ArrayList;
import java.util.List;


public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "concac";
    private static final int DATABASE_VERSION = 4;

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

    // Recurring Expense table
    private static final String TABLE_RECURRING = "RecurringExpenses";
    private static final String COLUMN_RECURRING_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_AMOUNTT = "amountt";
    private static final String COLUMN_START_DATE = "start_date";
    private static final String COLUMN_END_DATE = "end_date";

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

        String createTable = "CREATE TABLE " + TABLE_RECURRING + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_AMOUNTT + " REAL, " +
                COLUMN_START_DATE + " TEXT, " +
                COLUMN_END_DATE + " TEXT)";
        db.execSQL(createTable);
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
        ContentValues values = new ContentValues();
        values.put(COLUMN_AMOUNT, amount);
        values.put(COLUMN_CATEGORY, category);
        values.put(COLUMN_DATE, date);
        db.insert(TABLE_EXPENSES, null, values);
        db.close();
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
    public void addRecurringExpense(String name, double amount, String startDate, String endDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_AMOUNT, amount);
        values.put(COLUMN_START_DATE, startDate);
        values.put(COLUMN_END_DATE, endDate);
        db.insert(TABLE_RECURRING, null, values);
        db.close();
    }

    public List<RecurringExpense> getRecurringExpenses() {
        List<RecurringExpense> expenses = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_RECURRING, null);

        if (cursor.moveToFirst()) {
            do {
                RecurringExpense expense = new RecurringExpense(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getDouble(2),
                        cursor.getString(3),
                        cursor.getString(4)
                );
                expenses.add(expense);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return expenses;
    }
    public void editcost(int id, String name, double amount, String startdate, String enddate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("amount", amount);
        values.put("start_date", startdate);
        values.put("end_date", enddate);

        db.update("RecurringExpenses", values, "id=?", new String[]{String.valueOf(id)});
        db.close();
    }

    // Delete Expense
    public void deleteCost(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("RecurringExpenses", "id=?", new String[]{String.valueOf(id)}); // Xóa theo ID
        db.close();
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECURRING);
        onCreate(db);
    }
}

