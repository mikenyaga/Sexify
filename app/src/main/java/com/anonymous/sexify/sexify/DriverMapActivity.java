package com.anonymous.sexify.sexify;

import android.*;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DriverMapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener,RoutingListener {

    private GoogleMap mMap;
    GoogleApiClient googleApiClient;
    Location myLocation;
    LocationRequest locationRequest;

    private Button logOut,settings,rideStatus;

    private String customerId = "";

    private Boolean isLoggingOut = false;
    private SupportMapFragment mapFragment;

    private LinearLayout customerInfo;
    private ImageView customerProfileImage;
    private TextView customerName,customerPhone,customerDestination;


    //pick customer
    private int rStatus = 0;
    private String destination;
    private LatLng destinationLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        polylines = new ArrayList<>();
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DriverMapActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST_CODE);
        }else {
            mapFragment.getMapAsync(this);
        }


        logOut =findViewById(R.id.logOut);
        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isLoggingOut = true;

                disconnectDriver();

                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(DriverMapActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        settings = findViewById(R.id.settings);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DriverMapActivity.this,DriverSettingsActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });



        getAssignedCustomer();

        customerInfo = findViewById(R.id.customerInfo);
        customerProfileImage = findViewById(R.id.customerProfileImage);
        customerName = findViewById(R.id.customerName);
        customerPhone = findViewById(R.id.customerPhone);
        customerDestination = findViewById(R.id.customerDestination);

        //pick customer
        destinationLatLng = new LatLng(0.0,0.0);
        rideStatus = findViewById(R.id.rideStatus);
        rideStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (rStatus){
                    case 1:
                        //driver on the way to pick customer
                        //erase routes
                        rStatus = 2;
                        erasePolylines();
                        if (destinationLatLng.latitude !=0.0 && destinationLatLng.longitude != 0.0){
                            getRouteToMarker(destinationLatLng);
                        }
                        rideStatus.setText("Drive Completed");
                        break;
                    case  2:
                        recordRide();
                        //driver is on the way to the destination with the customer
                        endRide();
                        break;
                }
            }
        });
    }

    private void getAssignedCustomer(){
        String driverId = FirebaseAuth.getInstance().getUid();
        DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId).child("customerRequest").child("customerRideId");
        assignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    //change status to 1
                    rStatus =1;
                    customerId = dataSnapshot.getValue().toString();
                    getAssignedCustomerPickupLocation();
                    getAssignedCustomerInfo();
                    getAssignedCustomerDestination();
                }else{
                    endRide();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void getAssignedCustomerDestination(){
        String driverId = FirebaseAuth.getInstance().getUid();
        DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId).child("customerRequest");
        assignedCustomerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    Map<String,Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("destination") !=null){
                        destination = map.get("destination").toString();
                        customerDestination.setText("Dest: "+destination);
                    }else{
                        customerDestination.setText("Dest: ");
                    }
                    Double destinationLat = 0.0;
                    Double destinationLng = 0.0;
                    if (map.get("destinationLat")!=null){
                        destinationLat = Double.valueOf(map.get("destinationLat").toString());
                    }
                    if (map.get("destinationLng")!=null){
                        destinationLng = Double.valueOf(map.get("destinationLng").toString());
                    }
                    destinationLatLng = new LatLng(destinationLat,destinationLng);
                    //enroute so, show destination, temporarily
                    getRouteToMarker(destinationLatLng);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void getAssignedCustomerInfo(){
        customerInfo.setVisibility(View.VISIBLE);
        DatabaseReference customerDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(customerId);
        customerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String,Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("name")!=null){
                        customerName.setText(map.get("name").toString());
                    }
                    if (map.get("phone")!=null){
                        customerPhone.setText(map.get("phone").toString());
                    }
                    if (map.get("profileImageUrl")!=null){
                        Glide.with(getApplication()).load(map.get("profileImageUrl").toString()).into(customerProfileImage);
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private Marker pickupMarker;
    private DatabaseReference assignedCustomerPickupLocationRef;

    private ValueEventListener assignedCustomerPickupLocationRefListener;

    private void getAssignedCustomerPickupLocation(){
        assignedCustomerPickupLocationRef = FirebaseDatabase.getInstance().getReference().child("customerRequest").child(customerId).child("l");
        assignedCustomerPickupLocationRefListener = assignedCustomerPickupLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && !customerId.equals("")){
                    List<Object> map = (List<Object>)dataSnapshot.getValue();
                    double locationlat = 0;
                    double locationlng = 0;
                    if (map.get(0) !=null){
                        locationlat = Double.parseDouble(map.get(0).toString());
                    }
                    if (map.get(1) !=null){
                        locationlng = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng pickupLatLng = new LatLng(locationlat,locationlng);

                    pickupMarker = mMap.addMarker(new MarkerOptions().position(pickupLatLng).title("Pick Up Location"));
                    
                    getRouteToMarker(pickupLatLng);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getRouteToMarker(LatLng pickupLatLng) {
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(new LatLng(myLocation.getLatitude(),myLocation.getLongitude()), pickupLatLng)
                .build();
        routing.execute();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DriverMapActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST_CODE);
        }
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
    }
    protected synchronized void buildGoogleApiClient(){
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {

        if (getApplicationContext() != null){
            //called every second

            myLocation = location;
            LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference("driversAvailable");
            DatabaseReference refWorking = FirebaseDatabase.getInstance().getReference("driversWorking");
            GeoFire geoFireAvailable = new GeoFire(refAvailable);
            GeoFire geoFireWorking = new GeoFire(refWorking);

            switch (customerId){
                case "":
                    geoFireWorking.removeLocation(userId, new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {

                        }
                    });
                    geoFireAvailable.setLocation(userId, new GeoLocation(myLocation.getLatitude(), myLocation.getLongitude()), new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {

                        }
                    });
                    break;
                default:
                    geoFireAvailable.removeLocation(userId, new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {

                        }
                    });
                    geoFireWorking.setLocation(userId, new GeoLocation(myLocation.getLatitude(), myLocation.getLongitude()), new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {

                        }
                    });
                    break;
            }

        }

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //when map is called and everything is ready
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DriverMapActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST_CODE);
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void disconnectDriver(){
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("driversAvailable");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {

            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!isLoggingOut) {
            disconnectDriver();
        }
    }

    final int LOCATION_REQUEST_CODE = 1;


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case LOCATION_REQUEST_CODE:
                if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    mapFragment.getMapAsync(this);
                }else{
                    Toast.makeText(getApplicationContext(),"Please provide permissions",Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.primary_dark_material_light};

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


    private void endRide(){
        rideStatus.setText("Enroute,End Ride");
        erasePolylines();
        
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(userId).child("customerRequest");
        driverRef.removeValue();
        
        DatabaseReference  ref = FirebaseDatabase.getInstance().getReference("customerRequest");
        GeoFire geoFire = new GeoFire(ref);

        geoFire.removeLocation(customerId, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {

            }
        });
        customerId = "";

        if (pickupMarker != null){
            pickupMarker.remove();

        }

        customerId = "";

        if (pickupMarker!=null){
            pickupMarker.remove();
        }
        if (assignedCustomerPickupLocationRefListener != null) {
            assignedCustomerPickupLocationRef.removeEventListener(assignedCustomerPickupLocationRefListener);
        }
        customerInfo.setVisibility(View.GONE);
        customerName.setText("");
        customerPhone.setText("");
        customerProfileImage.setImageResource(R.mipmap.ic_launcher);
        customerDestination.setText("Dest: ");

    }

    private void recordRide(){
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(userId).child("history");
        DatabaseReference customerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(customerId).child("history");
        DatabaseReference historyRef = FirebaseDatabase.getInstance().getReference().child("History");
        String requestId = historyRef.push().getKey();

        driverRef.child(requestId).setValue(true);
        customerRef.child(requestId).setValue(true);

        HashMap map = new HashMap();
        map.put("driver",userId);
        map.put("customer",customerId);
        map.put("timestamp",getCurrentTimestamp());
        map.put("rating",0);

        historyRef.child(requestId).updateChildren(map);
    }

    public Long getCurrentTimestamp() {
        Long timestamp = System.currentTimeMillis()/1000;
        return timestamp;
    }
}
