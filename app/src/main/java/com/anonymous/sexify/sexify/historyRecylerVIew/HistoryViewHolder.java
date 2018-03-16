package com.anonymous.sexify.sexify.historyRecylerVIew;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.anonymous.sexify.sexify.R;

/**
 * Created by bitsoko on 3/16/18.
 */

public class HistoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView rideId;

    public HistoryViewHolder(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);

        rideId = itemView.findViewById(R.id.rideId);
    }

    @Override
    public void onClick(View view) {

    }
}
