package com.bratuha.pwkeeper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class PasswordAdapter extends ArrayAdapter<HashMap<String, String>> {
    private Context context;
    private ArrayList<HashMap<String, String>> passwords;

    public PasswordAdapter(Context context, ArrayList<HashMap<String, String>> passwords) {
        super(context, 0, passwords);
        this.context = context;
        this.passwords = passwords;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_password, parent, false);
        }

        HashMap<String, String> passwordItem = passwords.get(position);

        TextView titleTextView = convertView.findViewById(R.id.titleTextView);
        TextView detailsTextView = convertView.findViewById(R.id.detailsTextView);

        titleTextView.setText(passwordItem.get("title"));
        detailsTextView.setText(passwordItem.get("details"));

        return convertView;
    }
}

