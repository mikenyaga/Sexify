package com.anonymous.sexify.sexify;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.anonymous.sexify.sexify.historyRecylerVIew.HistoryAdapter;
import com.anonymous.sexify.sexify.historyRecylerVIew.HistoryObject;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView historyRecyclerView;
    private RecyclerView.Adapter historyAdapter;
    private RecyclerView.LayoutManager historyLayoutManager;

    private String userType,userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        historyRecyclerView = findViewById(R.id.historyRecylerView);
        historyRecyclerView.setNestedScrollingEnabled(false);
        historyRecyclerView.setHasFixedSize(true);

        historyLayoutManager = new LinearLayoutManager(HistoryActivity.this);
        historyRecyclerView.setLayoutManager(historyLayoutManager);

        historyAdapter = new HistoryAdapter(getDataHistory(),HistoryActivity.this);
        historyRecyclerView.setAdapter(historyAdapter);

        userType = getIntent().getExtras().getString("userType");


        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        getUserHistoryIds();

    }

    private ArrayList<HistoryObject> dataHistory = new ArrayList<>();
    public ArrayList<HistoryObject> getDataHistory() {
        return dataHistory;
    }

    public void getUserHistoryIds() {
        DatabaseReference userHistoryReference = FirebaseDatabase.getInstance().getReference().child("Users").child(userType).child(userId).child("history");
        userHistoryReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    for (DataSnapshot history:dataSnapshot.getChildren()){
                        fetchRideInfo(history.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void fetchRideInfo(String rideKey) {
        DatabaseReference historyDatabaseRef = FirebaseDatabase.getInstance().getReference().child("History").child(rideKey);
        historyDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String rideId = dataSnapshot.getKey();
                    dataHistory.add(new HistoryObject(rideId));
                    historyAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
