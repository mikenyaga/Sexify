package com.anonymous.sexify.sexify.historyRecylerVIew;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.anonymous.sexify.sexify.R;

import java.util.List;

/**
 * Created by bitsoko on 3/16/18.
 */

public class HistoryAdapter extends RecyclerView.Adapter<HistoryViewHolder> {
    private List<HistoryObject> itemList;
    private Context context;

    public HistoryAdapter(List<HistoryObject> itemList,Context context){
        this.itemList = itemList;
        this.context = context;
    }
    @Override
    public HistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history,null) ;
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);
        HistoryViewHolder rcv = new HistoryViewHolder(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(HistoryViewHolder holder, int position) {
        holder.rideId.setText(itemList.get(position).getRideId());
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }
}
