package com.bratuha.pwkeeper;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private EditText searchEditText;
    private ListView passwordListView;
    private PasswordAdapter passwordAdapter;
    private ArrayList<HashMap<String, String>> passwordList;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchEditText = findViewById(R.id.searchEditText);
        passwordListView = findViewById(R.id.passwordListView);
        passwordList = new ArrayList<>();

        passwordAdapter = new PasswordAdapter(this, passwordList);
        passwordListView.setAdapter(passwordAdapter);

        databaseHelper = new DatabaseHelper(this);

        displayPasswords(); // Display passwords initially

        // Handle search button click
        findViewById(R.id.searchButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchPasswords(searchEditText.getText().toString());
            }
        });

        // Handle add button click
        FloatingActionButton addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddPasswordDialog();
            }
        });

        // Handle item click for deleting an item
        passwordListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showDeleteDialog(position);
            }
        });
    }

    private void displayPasswords() {
        // Fetch passwords from the database and display them in the ListView
        passwordList.clear(); // Clear existing list
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM passwords ORDER BY title", null);

        if (cursor.moveToFirst()) {
            do {
                int titleIndex = cursor.getColumnIndex("title");
                int emailIndex = cursor.getColumnIndex("email");
                int passwordIndex = cursor.getColumnIndex("password");

                String title = titleIndex != -1 ? cursor.getString(titleIndex) : "";
                String email = emailIndex != -1 ? cursor.getString(emailIndex) : "";
                String password = passwordIndex != -1 ? cursor.getString(passwordIndex) : "";
                HashMap<String, String> map = new HashMap<>();
                map.put("title", title);
                map.put("details", "Email/User: " + (email.isEmpty() ? "N/A" : email) + "\nPassword: " + password);
                passwordList.add(map);
            } while (cursor.moveToNext());
        }
        cursor.close();
        passwordAdapter.notifyDataSetChanged(); // Notify adapter of data change
    }

    private void searchPasswords(String query) {
        // Search passwords based on title
        passwordList.clear(); // Clear existing list
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM passwords WHERE title LIKE ?", new String[]{"%" + query + "%"});

        if (cursor.moveToFirst()) {
            do {
                int titleIndex = cursor.getColumnIndex("title");
                int emailIndex = cursor.getColumnIndex("email");
                int passwordIndex = cursor.getColumnIndex("password");

                String title = titleIndex != -1 ? cursor.getString(titleIndex) : "";
                String email = emailIndex != -1 ? cursor.getString(emailIndex) : "";
                String password = passwordIndex != -1 ? cursor.getString(passwordIndex) : "";
                HashMap<String, String> map = new HashMap<>();
                map.put("title", title);
                map.put("details", "Email/User: " + (email.isEmpty() ? "N/A" : email) + "\nPassword: " + password);
                passwordList.add(map);
            } while (cursor.moveToNext());
        }
        cursor.close();
        passwordAdapter.notifyDataSetChanged(); // Notify adapter of data change
    }

    private void showAddPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_password, null);
        builder.setView(dialogView);

        final EditText titleEditText = dialogView.findViewById(R.id.titleEditText);
        final EditText emailEditText = dialogView.findViewById(R.id.emailEditText);
        final EditText passwordEditText = dialogView.findViewById(R.id.passwordEditText);
        Button addButton = dialogView.findViewById(R.id.addButton);

        final AlertDialog dialog = builder.create();
        dialog.show();

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = titleEditText.getText().toString().trim();
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (!title.isEmpty() && !password.isEmpty()) {
                    // Add the new password entry to the database
                    addPasswordToDatabase(title, email, password);
                    dialog.dismiss();
                } else {
                    Toast.makeText(MainActivity.this, "Please fill the title and password", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void addPasswordToDatabase(String title, String email, String password) {
        // Add new password entry to the database
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("email", email);
        values.put("password", password);
        db.insert("passwords", null, values);
        db.close();

        // Refresh the password list to reflect the new entry
        displayPasswords();
    }

    private void showDeleteDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Delete Item");
        builder.setMessage("Are you sure you want to delete this item?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteItem(position);
            }
        });
        builder.setNegativeButton("No", null);
        builder.show();
    }

    private void deleteItem(int position) {
        // Get the item to delete
        String item = passwordList.get(position).get("title");

        // Delete the item from the database
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.delete("passwords", "title = ?", new String[]{item});
        db.close();

        // Remove the item from the list
        passwordList.remove(position);

        // Notify the adapter of the change
        passwordAdapter.notifyDataSetChanged();
    }
}

