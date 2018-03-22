package com.anonymous.makanga.makanga.historyRecylerVIew;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.anonymous.makanga.makanga.HistorySingleActivity;
import com.anonymous.makanga.makanga.R;

/**
 * Created by bitsoko on 3/16/18.
 */

public class HistoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView rideId,time,destination;

    public HistoryViewHolder(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);
        rideId = itemView.findViewById(R.id.rideId);
        time = itemView.findViewById(R.id.time);
        destination = itemView.findViewById(R.id.destination);
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(view.getContext(),HistorySingleActivity.class);
        Bundle b = new Bundle();
        b.putString("rideId",rideId.getText().toString());
        intent.putExtras(b);
        view.getContext().startActivity(intent);

    }
}
