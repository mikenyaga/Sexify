package com.anonymous.makanga.makanga;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HistorySingleActivity extends AppCompatActivity implements OnMapReadyCallback,RoutingListener {

    private GoogleMap mMap;
    private SupportMapFragment mapFragment;

    private String rideId,userId,customerId,driverId,userType;

    private TextView rideLocation;
    private TextView rideDistance;
    private TextView userName;
    private TextView userPhone;
    private TextView rideDate;
    private RatingBar ratingBar;

    private LatLng destinationLatLng,pickupLatLng;

    private ImageView userImage;
    private DatabaseReference historyRideInfoDb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_single);
        polylines = new ArrayList<>();

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        rideId = getIntent().getExtras().getString("rideId");

        rideLocation = findViewById(R.id.rideLocation);
        rideDistance = findViewById(R.id.rideDistance);
        rideDate = findViewById(R.id.rideDate);
        userName = findViewById(R.id.userName);
        userPhone = findViewById(R.id.userPhone);
        userImage = findViewById(R.id.userImage);
        ratingBar = findViewById(R.id.ratingBar);


        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        historyRideInfoDb = FirebaseDatabase.getInstance().getReference().child("History").child(rideId);
        getRideInformation();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

    }

    public void getRideInformation() {
        historyRideInfoDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    for (DataSnapshot child:dataSnapshot.getChildren()){
                        if (child.getKey().equals("customer")){
                            customerId = child.getValue().toString();
                            if (!customerId.equals(userId)){
                                userType = "Drivers";
                                getUserInformation("Customers",customerId);
                            }
                        }
                        if (child.getKey().equals("driver")){
                            driverId = child.getValue().toString();
                            if (!driverId.equals(userId)){
                                userType = "Customers";
                                getUserInformation("Drivers",driverId);
                                displayCustomerRelatedObject();
                            }
                        }
                        if (child.getKey().equals("timestamp")){
                            rideDate.setText(getDate(Long.valueOf(child.getValue().toString())));
                        }

                        if (child.getKey().equals("destination")){
                            rideLocation.setText(child.getValue().toString());
                        }
                        if (child.getKey().equals("location")){
                            pickupLatLng = new LatLng(Double.valueOf(child.child("from").child("lat").getValue().toString()),Double.valueOf(child.child("from").child("lng").getValue().toString()));
                            destinationLatLng = new LatLng(Double.valueOf(child.child("to").child("lat").getValue().toString()),Double.valueOf(child.child("to").child("lng").getValue().toString()));
                            if (destinationLatLng != new LatLng(0.0,0.0)){
                                getRouteToMarker();
                            }
                        }
                        if (child.getKey().equals("rating")){
                            ratingBar.setRating(Integer.valueOf(child.getValue().toString()));
                        }
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void displayCustomerRelatedObject() {
        ratingBar.setVisibility(View.VISIBLE);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean b) {
                historyRideInfoDb.child("rating").setValue(rating);
                DatabaseReference driverRatingDb = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId).child("rating");
                driverRatingDb.child(rideId).setValue(rating);
            }
        });
    }

    private void getUserInformation(String otherUserTye, String otherUserId) {
        DatabaseReference otherUserDb = FirebaseDatabase.getInstance().getReference().child("Users").child(otherUserTye).child(otherUserId);
        otherUserDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    Map<String,Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("name")!=null){
                        userName.setText(map.get("name").toString());
                    }
                    if (map.get("phone")!=null){
                        userPhone.setText(map.get("phone").toString());
                    }
                    if (map.get("profileImageUrl")!=null){
                        Glide.with(getApplication()).load(map.get("profileImageUrl").toString()).into(userImage);
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private String getDate(Long timestamp) {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(timestamp*1000);
        String date = DateFormat.format("MM-dd-yyyy hh:mm",cal).toString();
        return date;
    }

    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.colorBlack};

    @Override
    public void onRoutingFailure(RouteException e) {
        if(e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        LatLngBounds.Builder builder  =new LatLngBounds.Builder();
        builder.include(pickupLatLng);
        builder.include(destinationLatLng);
        LatLngBounds bounds = builder.build();
        int width = getResources().getDisplayMetrics().widthPixels;
        int padding  = (int)(width*0.2);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds,padding);
        mMap.animateCamera(cameraUpdate);
        mMap.addMarker(new MarkerOptions().position(pickupLatLng).title("PickUp"));
        mMap.addMarker(new MarkerOptions().position(destinationLatLng).title("Destination"));
        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onRoutingCancelled() {

    }
    private void erasePolylines(){
        for(Polyline line:polylines){
            line.remove();
        }
        polylines.clear();
    }

    private void getRouteToMarker() {
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(pickupLatLng, destinationLatLng)
                .build();
        routing.execute();
    }
}
