package com.example.daithinh.prototypewebrtc.Adapter;

import android.content.Context;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.daithinh.prototypewebrtc.R;

import java.util.ArrayList;

/**
 * Created by Dai Thinh on 11/4/2017.
 */

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.RecyclerViewHolder> {

    private IViewHolderClick holderClick;

    private ArrayList<String> list = new ArrayList<>();

    public void updateList(ArrayList<String> list){
        this.list = list;
    }

    public CustomAdapter(ArrayList<String> objects, IViewHolderClick holderClick) {
        this.list = objects;
        this.holderClick = holderClick;
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.listitem , null);
        return new RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, final int position) {
        holder.textViewListItem.setText(list.get(position));
        holder.textViewListItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holderClick.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class RecyclerViewHolder extends RecyclerView.ViewHolder {
        TextView textViewListItem;
        public RecyclerViewHolder(View itemView) {
            super(itemView);
             textViewListItem = itemView.findViewById(R.id.textViewListItem);
        }
    }

    public interface IViewHolderClick {
        void onItemClick(int posision);
    }
}
