package com.example.daithinh.prototypewebrtc.Adapter;

import android.content.Context;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.daithinh.prototypewebrtc.R;
import com.example.daithinh.prototypewebrtc.SQLiteConnection.User;

import java.util.ArrayList;

/**
 * Created by Dai Thinh on 11/4/2017.
 */

public class CustomAdapter extends ArrayAdapter {

    private ArrayList<User> list = new ArrayList<>();


    public CustomAdapter( Context context, int resource, ArrayList<User> objects) {
        super(context, resource, objects);
        this.list = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        convertView = inflater.inflate(R.layout.listitem , null);

        TextView textViewListItem = convertView.findViewById(R.id.textViewListItem);
        textViewListItem.setText(list.get(position).getUsername());


        return convertView;
    }
}
