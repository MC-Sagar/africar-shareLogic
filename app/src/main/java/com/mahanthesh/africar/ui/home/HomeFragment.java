package com.mahanthesh.africar.ui.home;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.RequestResult;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Info;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.github.javiersantos.bottomdialogs.BottomDialog;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.protobuf.NullValue;
import com.mahanthesh.africar.R;
import com.mahanthesh.africar.activity.RideCompleteActivty;
import com.mahanthesh.africar.model.JobRequest;

import java.util.ArrayList;
import java.util.Objects;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.location.config.LocationAccuracy;
import io.nlopez.smartlocation.location.config.LocationParams;
import io.nlopez.smartlocation.location.providers.LocationGooglePlayServicesProvider;

import static com.mahanthesh.africar.utils.Constants.DISTANCE_CHECK_IN_METER;
import static com.mahanthesh.africar.utils.Constants.RIDER_DISTANCE_IN_METER;
import static com.mahanthesh.africar.utils.Constants.TRACKING_DISTANCE;
import static com.mahanthesh.africar.utils.Utils.getAddress;
import static com.mahanthesh.africar.utils.Utils.getCurrentTime;
import static com.mahanthesh.africar.utils.Utils.getDistance;
import static com.mahanthesh.africar.utils.Utils.getLocationFromLocationInfo;

public class HomeFragment extends Fragment implements OnMapReadyCallback, OnLocationUpdatedListener, ChildEventListener, View.OnClickListener {

    private HomeViewModel homeViewModel;
    private GoogleMap map;
    private SupportMapFragment supportMapFragment;
    private long mLocTrackingInterval = 1000 * 10;
    private static final int LOCATION_PERMISSION_ID = 1001;
    private BottomDialog bottomDialog;
    private LayoutInflater inflater;
    private FirebaseDatabase firebaseDatabase;
    private GeoFire geoFire;
    private FirebaseUser firebaseUser;
    private JobRequest jobRequest;
    private Marker currentMarker;
    private boolean isReadingQueue = false;
    private DatabaseReference queueReference;
    private EditText otpEditText;
    private TextView pickupRequestTextView, dropRequestTextView;
    private Button acceptedRideArrivedButton;
    private Location currentLocation;
    private boolean hasRide = false;
    private int SeatsAvailable = 3;
    private double lat1,long1,dlat1,dlong1;
    private double lat2,long2,dlat2,dlong2;
    private double lat3,long3,dlat3,dlong3;
    private int RideNo = 0;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        homeViewModel.getUserInfo().observe(requireActivity(), userInfo -> {
            if (userInfo != null) {
                Log.e("status", userInfo.getActive().toString());
                if (userInfo.getActive()) {
                    rideSearchingLayout();
                } else {
                    if (bottomDialog != null)
                        bottomDialog.dismiss();
                }
            }

        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (supportMapFragment == null) {
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            supportMapFragment = SupportMapFragment.newInstance();
            ft.replace(R.id.mapView, supportMapFragment).commit();
        }
        supportMapFragment.getMapAsync(this);

        // Location permission not granted
        if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_ID);
        }
        inflater = (LayoutInflater) requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // initialzing firebase and geo gence
        firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference driverReference = firebaseDatabase.getReference("AvailableDriver");
        geoFire = new GeoFire(driverReference);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        homeViewModel.updateUserId(firebaseUser.getUid());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(requireContext());
        map = googleMap;
        startLocationSearch();

    }

    @Override
    public void onLocationUpdated(Location location) {
        Log.e("Updated Location", location.toString());
        currentLocation = location;
        currentMarker.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15f));
        if (homeViewModel != null && homeViewModel.getUserInfo().getValue().getActive()) {
            addUserToGeoFire(location);
        }
        if (jobRequest != null) {
            if (jobRequest.getAccepted().equalsIgnoreCase("true")  && jobRequest.getIsVerified().equalsIgnoreCase("false")) {
                // ride accepted but not reach verifies
                double distance = getDistance(currentLocation, jobRequest.getPickup());
                Log.e("Pick up Distance: ", String.valueOf(distance));
                if (distance < DISTANCE_CHECK_IN_METER) {
                    if (acceptedRideArrivedButton != null)
                        acceptedRideArrivedButton.setEnabled(true);
                }

            } else if (jobRequest.getAccepted().equalsIgnoreCase("true") && jobRequest.getIsVerified().equalsIgnoreCase("true") && jobRequest.getIsCompleted().equalsIgnoreCase("false")) {
                // ride accepted but not reach verifies
                double distance = getDistance(currentLocation, jobRequest.getDrop());
                Log.e("Drop up Distance:", String.valueOf(distance));
                if (distance < DISTANCE_CHECK_IN_METER) {
                    rideCompleteView();
                }
            }
        }


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SmartLocation.with(requireActivity()).geocoding().stop();
    }

    private void startLocationSearch() {
        LocationAccuracy trackingAccuracy = LocationAccuracy.HIGH;

        LocationParams.Builder builder = new LocationParams.Builder()
                .setAccuracy(trackingAccuracy)
                .setDistance(TRACKING_DISTANCE)
                .setInterval(mLocTrackingInterval);
        LocationGooglePlayServicesProvider provider = new LocationGooglePlayServicesProvider();
        provider.setCheckLocationSettings(true);

        SmartLocation smartLocation = new SmartLocation.Builder(requireContext()).logging(true).build();
        smartLocation.location(provider).start(this);
        map.clear();
        MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(12.9797, 77.5912));
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_cur_location));
        currentMarker = map.addMarker(markerOptions);

        SmartLocation.with(getContext()).location()
                .continuous()
                .config(LocationParams.BEST_EFFORT)
                .config(builder.build())
                .start(this);

    }

    /**
     * listing to the request queue for the ride request
     */
    private void listeningToRequestQueue() {
        if (!isReadingQueue) {
            queueReference = firebaseDatabase.getReference().child("rider_queue");
            queueReference.addChildEventListener(this);
            isReadingQueue = true;
        }
    }

    /**
     * adding the driver to the available queue
     *
     * @param location
     */
    private void addUserToGeoFire(Location location) {
        if (homeViewModel != null) {
            geoFire.setLocation(firebaseUser.getUid(), new GeoLocation(location.getLatitude(), location.getLongitude()), (key, error) -> {
                if (error != null) {
                    Log.e("GeoFire", "There was an error saving the location to GeoFire: " + error);
                } else {
                    Log.e("GeoFire", "Location saved on server successfully!");
                    // check the request queue
                    if (key.equalsIgnoreCase(firebaseUser.getUid())) {
                        listeningToRequestQueue();
                    }

                }
            });
        }


    }

    /**
     * show bottom dialog with selected view
     *
     * @param view
     */
    private void showDialog(View view) {
        if (bottomDialog != null) {
            bottomDialog.dismiss();
        }
        bottomDialog = new BottomDialog.Builder(requireContext())
                .setCustomView(view)
                .setTitle(getContext().getResources().getString(R.string.driverInfo))
                .setCancelable(false)
                .setNegativeText("Cancel")
                .setNegativeTextColor(R.color.colorWhite)
                .onNegative(BottomDialog::dismiss)
                .show();
    }


    /**
     * get the polyline for the direction
     * @param origin
     * @param dest
     */
    private void getDirection(LatLng origin, LatLng dest) {
        String apiKey = getString(R.string.google_maps_key);

        GoogleDirection.withServerKey(apiKey)
                .from(origin)
                .to(dest)
                .transportMode(TransportMode.DRIVING).execute(new DirectionCallback() {
            @Override
            public void onDirectionSuccess(@Nullable Direction direction) {
                String status = direction.getStatus();
                if (status.equals(RequestResult.OK)) {
                    Route route = direction.getRouteList().get(0);
                    Leg leg = route.getLegList().get(0);
                    Info distanceInfo = leg.getDistance();
                    Info durationInfo = leg.getDuration();
                    String distance = distanceInfo.getText();
                    String duration = durationInfo.getText();
                    ArrayList<LatLng> directionPositionList = leg.getDirectionPoint();
                    PolylineOptions polylineOptions = DirectionConverter.createPolyline(requireContext(), directionPositionList, 5, Color.rgb(26, 129, 53));
                    map.addPolyline(polylineOptions);
                    Log.d("myLoc", "Duration: " + duration + " distance: " + distance);


                }
            }

            @Override
            public void onDirectionFailure(@NonNull Throwable t) {

            }
        });

    }

    /**
     * Ride searching layout
     */
    private void rideSearchingLayout(){
        View findView = inflater.inflate(R.layout.ride_find_layout, null);
        showDialog(findView);
    }


    /**
     * Request ride bottom dialog view
     */
    private void rideRequestLayout() {
        View requestView = inflater.inflate(R.layout.ride_request_layout, null);
        Button acceptRequestButton = requestView.findViewById(R.id.acceptRequestButton);
        pickupRequestTextView = requestView.findViewById(R.id.pickupRequestTextView);
        dropRequestTextView = requestView.findViewById(R.id.dropRequestTextView);
        TextView requestPriceTextView = requestView.findViewById(R.id.requestPriceTextView);
        TextView requestDistanceTextView = requestView.findViewById(R.id.requestDistanceTextView);
        TextView requestRatingTextView = requestView.findViewById(R.id.requestRatingTextView);
        TextView requestDurationTextView = requestView.findViewById(R.id.requestDurationTextView);
        if (jobRequest != null) {
            Location pickupLocation = getLocationFromLocationInfo(jobRequest.getPickup(), "Pickup");
            Location dropLocation = getLocationFromLocationInfo(jobRequest.getDrop(), "Drop");
            pickupRequestTextView.setText("My Pickup");
            dropRequestTextView.setText("My Drop");
            requestRatingTextView.setText("3.5");
            String price = "$" + jobRequest.getRide_cost();
            requestPriceTextView.setText(price);
            requestDistanceTextView.setText(jobRequest.getDistance());
            requestDurationTextView.setText(jobRequest.getDuration());
            acceptRequestButton.setOnClickListener(this);
            SmartLocation.with(requireContext()).geocoding()
                    .reverse(pickupLocation, (location, list) -> {
                        if (location.getProvider().equalsIgnoreCase("Pickup")) {
                            if (list.size() > 0) {
                                String pickUpAddress = getAddress(list.get(0));
                                pickupRequestTextView.setText(pickUpAddress);
                            }

                        } else if (location.getProvider().equalsIgnoreCase("Drop")) {
                            if (list.size() > 0) {
                                String dropAddress = getAddress(list.get(0));
                                dropRequestTextView.setText(dropAddress);
                            }
                        }

                    });

            SmartLocation.with(requireContext()).geocoding()
                    .reverse(dropLocation, (location, list) -> {
                        if (location.getProvider().equalsIgnoreCase("Pickup")) {
                            if (list.size() > 0) {
                                String pickUpAddress = getAddress(list.get(0));
                                pickupRequestTextView.setText(pickUpAddress);
                            }

                        } else if (location.getProvider().equalsIgnoreCase("Drop")) {
                            if (list.size() > 0) {
                                String dropAddress = getAddress(list.get(0));
                                dropRequestTextView.setText(dropAddress);
                            }
                        }

                    });
            showDialog(requestView);
            MarkerOptions pickupMarkerOptions = new MarkerOptions().position(new LatLng(jobRequest.getPickup().getLatitude(), jobRequest.getPickup().getLongitude()));
            map.addMarker(pickupMarkerOptions);
            // get the polyline
            getDirection(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), new LatLng(jobRequest.getPickup().getLatitude(), jobRequest.getPickup().getLongitude()));

        }


    }

    /**
     * Request accept bottom dialog view
     */
    private void rideAcceptLayout() {
        View acceptedView = inflater.inflate(R.layout.ride_accept_layout, null);
        ImageView acceptedRideNavigateButton = acceptedView.findViewById(R.id.acceptedRideNavigateButton);
        ImageView acceptedRideCallButton = acceptedView.findViewById(R.id.acceptedRideCallButton);
        ImageView acceptedRideCancelButton = acceptedView.findViewById(R.id.acceptedRideCancelButton);
        TextView waitDurationTextView = acceptedView.findViewById(R.id.waitDurationTextView);
        TextView waitDistanceTextView = acceptedView.findViewById(R.id.waitDistanceTextView);

        acceptedRideArrivedButton = acceptedView.findViewById(R.id.acceptedRideArrivedButton);
        acceptedRideNavigateButton.setOnClickListener(this);
        acceptedRideCallButton.setOnClickListener(this);
        acceptedRideCancelButton.setOnClickListener(this);
        acceptedRideArrivedButton.setOnClickListener(this);
        acceptedRideArrivedButton.setEnabled(false);
        if (jobRequest != null) {
            waitDurationTextView.setText(jobRequest.getDuration());
            waitDistanceTextView.setText(jobRequest.getDistance());
        }
        showDialog(acceptedView);

    }

    /**
     * Request verify bottom dialog view
     */
    private void verifyRideLayout() {
        View verifyView = inflater.inflate(R.layout.ride_verify_layout, null);
        otpEditText = verifyView.findViewById(R.id.otpEditText);
        Button verifyButton = verifyView.findViewById(R.id.verifyRideButton);
        Button cancelButton = verifyView.findViewById(R.id.cancelTripButton);
        TextView rideVerifyDistanceTextView = verifyView.findViewById(R.id.rideVerifyDistanceTextView);
        TextView rideVerifyDurationTextView = verifyView.findViewById(R.id.rideVerifyDurationTextView);
        verifyButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
        if (jobRequest != null) {
            rideVerifyDistanceTextView.setText(jobRequest.getDistance());
            rideVerifyDurationTextView.setText(jobRequest.getDuration());
        }
        showDialog(verifyView);

    }

    /**
     * Request ride in progress dialog view
     */
    private void rideInProgressLayout() {
        View rideProgressView = inflater.inflate(R.layout.ride_progress_layout, null);
        TextView rideDurationTextView = rideProgressView.findViewById(R.id.rideDurationTextView);
        TextView rideDistanceTextView = rideProgressView.findViewById(R.id.rideDistanceTextView);
        TextView riderInfoTextView = rideProgressView.findViewById(R.id.riderInfoTextView);
        ImageView rideNavigateButton = rideProgressView.findViewById(R.id.rideNavigateButton);
        hasRide = false;
        if (jobRequest != null) {
            rideDurationTextView.setText(jobRequest.getDuration());
            rideDistanceTextView.setText(jobRequest.getDistance());
            rideNavigateButton.setOnClickListener(this);
        }
        showDialog(rideProgressView);


    }

    /**
     * ride in Progress event triggered
     */
    private void rideInProgress() {
        // clear the map
        map.clear();
        // add pickup and drop location
        MarkerOptions pickupMarkerOptions = new MarkerOptions().position(new LatLng(jobRequest.getPickup().getLatitude(), jobRequest.getPickup().getLongitude()));
        MarkerOptions dropMarkerOptions = new MarkerOptions().position(new LatLng(jobRequest.getDrop().getLatitude(), jobRequest.getDrop().getLongitude()));
        map.addMarker(pickupMarkerOptions);
        map.addMarker(dropMarkerOptions);
        // get the polyline
        getDirection(new LatLng(jobRequest.getPickup().getLatitude(), jobRequest.getPickup().getLongitude()), new LatLng(jobRequest.getDrop().getLatitude(), jobRequest.getDrop().getLongitude()));
        rideInProgressLayout();


    }

    /**
     * ride complete view
     */
    private void rideCompleteView() {
        View completeRideView = inflater.inflate(R.layout.ride_complete_layout, null);
        Button completeButton = completeRideView.findViewById(R.id.completeRideButton);
        TextView rideCompleteDurationTextView = completeRideView.findViewById(R.id.rideCompleteDurationTextView);
        TextView rideCompleteDistanceTextView = completeRideView.findViewById(R.id.rideCompleteDistanceTextView);
        if (jobRequest != null) {
            rideCompleteDurationTextView.setText(jobRequest.getDuration());
            rideCompleteDistanceTextView.setText(jobRequest.getDistance());
        }
        completeButton.setOnClickListener(this);
        showDialog(completeRideView);

    }

    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
        if (!hasRide) {
            JobRequest selectedJobRequest = snapshot.getValue(JobRequest.class);
            if(selectedJobRequest.getPickup() != null && selectedJobRequest.getDrop() != null){
            double distance = getDistance(currentLocation, selectedJobRequest.getPickup());
            // distance of 2 km
            if (selectedJobRequest.getAccepted().equalsIgnoreCase("false") && distance < RIDER_DISTANCE_IN_METER) {
                // IF selectedJobRequest.getType = "shared"
                if (selectedJobRequest.getType().equalsIgnoreCase("share")) {
                    // IF selectedJobRequest.getSeatsRequired <= Driver.FreeSeats
                    if (selectedJobRequest.getRequired_seats() <= SeatsAvailable) {
                        // - > Freeseats = Freeseats - selectedJobRequest.getSeatsRequired
                        SeatsAvailable = SeatsAvailable - selectedJobRequest.getRequired_seats();
                        jobRequest = selectedJobRequest;
                        RideNo = RideNo + 1;
                        jobRequest.setId(snapshot.getKey());
                        if(RideNo == 1){
                            lat1 = jobRequest.getPickup().getLatitude();
                            long1 = jobRequest.getPickup().getLongitude();
                            dlat1 = jobRequest.getDrop().getLatitude();
                            dlong1 = jobRequest.getDrop().getLongitude();
                        } if(RideNo == 2){
                            lat2 = jobRequest.getPickup().getLatitude();
                            long2 = jobRequest.getPickup().getLongitude();
                            dlat2 = jobRequest.getDrop().getLatitude();
                            dlong2 = jobRequest.getDrop().getLongitude();
                        } if(RideNo == 3){
                            lat3 = jobRequest.getPickup().getLatitude();
                            long3 = jobRequest.getPickup().getLongitude();
                            dlat3 = jobRequest.getDrop().getLatitude();
                            dlong3 = jobRequest.getDrop().getLongitude();
                        }
                        hasRide = true;
                        rideRequestLayout();
                        Log.d("myTag", "Number: " + SeatsAvailable);
                        Log.d("myLog", "TYPE: " + selectedJobRequest.getType());
                    }
                    Log.d("myTag", "Number: " + SeatsAvailable);
                }
                // IF selectedJobRequest.getType != "shared"
                if (selectedJobRequest.getType().equalsIgnoreCase("single")) {
                    jobRequest = selectedJobRequest;
                    jobRequest.setId(snapshot.getKey());
                    hasRide = true;
                    Log.d("myLog", "TYPE: " + selectedJobRequest.getType());
                    rideRequestLayout();
                }
            }
        }
        }

    }


    @Override
    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
        Log.e("onChildChanged", "previousChildName");
        /*
        if (!hasRide) {
            jobRequest = snapshot.getValue(JobRequest.class);
            if (jobRequest != null)
                jobRequest.setId(snapshot.getKey());
            hasRide = true;
        } */
        if (!hasRide)  {
            JobRequest selectedJobRequest = snapshot.getValue(JobRequest.class);
            if(selectedJobRequest.getPickup() != null && selectedJobRequest.getDrop() != null){
                double distance = getDistance(currentLocation, selectedJobRequest.getPickup());
                // distance of 2 km
                if (selectedJobRequest.getAccepted().equalsIgnoreCase("false") && distance < RIDER_DISTANCE_IN_METER) {
                    // IF selectedJobRequest.getType = "shared"
                    if (selectedJobRequest.getType().equalsIgnoreCase("share")) {
                        // IF selectedJobRequest.getSeatsRequired <= Driver.FreeSeats
                        if (selectedJobRequest.getRequired_seats() <= SeatsAvailable) {
                            // - > Freeseats = Freeseats - selectedJobRequest.getSeatsRequired
                            SeatsAvailable = SeatsAvailable - selectedJobRequest.getRequired_seats();
                            jobRequest = selectedJobRequest;
                            RideNo = RideNo + 1;
                            jobRequest.setId(snapshot.getKey());
                            if(RideNo == 1){
                                lat1 = jobRequest.getPickup().getLatitude();
                                long1 = jobRequest.getPickup().getLongitude();
                                dlat1 = jobRequest.getDrop().getLatitude();
                                dlong1 = jobRequest.getDrop().getLongitude();
                            } if(RideNo == 2){
                                lat2 = jobRequest.getPickup().getLatitude();
                                long2 = jobRequest.getPickup().getLongitude();
                                dlat2 = jobRequest.getDrop().getLatitude();
                                dlong2 = jobRequest.getDrop().getLongitude();
                            } if(RideNo == 3){
                                lat3 = jobRequest.getPickup().getLatitude();
                                long3 = jobRequest.getPickup().getLongitude();
                                dlat3 = jobRequest.getDrop().getLatitude();
                                dlong3 = jobRequest.getDrop().getLongitude();
                            }
                            hasRide = true;
                            rideRequestLayout();
                            Log.d("myTag", "Number: " + SeatsAvailable);
                            Log.d("myLog", "TYPE: " + selectedJobRequest.getType());
                        }
                        Log.d("myTag", "Number: " + SeatsAvailable);
                    }
                    // IF selectedJobRequest.getType != "shared"
                    if (selectedJobRequest.getType().equalsIgnoreCase("single")) {
                        jobRequest = selectedJobRequest;
                        jobRequest.setId(snapshot.getKey());
                        hasRide = true;
                        Log.d("myLog", "TYPE: " + selectedJobRequest.getType());
                        rideRequestLayout();
                    }
                }
            }
        }
        else {
            if (Objects.requireNonNull(snapshot.getKey()).equalsIgnoreCase(jobRequest.getId())) {
                updateJobQueue(snapshot.getValue(JobRequest.class));
            }
        }
    }

    @Override
    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

    }

    @Override
    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.acceptRequestButton:
                // update the driver id and set request accepted to true
                queueReference.child(jobRequest.getId()).child("driverId").setValue(firebaseUser.getUid());
                queueReference.child(jobRequest.getId()).child("accepted").setValue("true");
                if (jobRequest != null) {
                    jobRequest.setAcceptedTime(getCurrentTime());
                }
                // show ride accepted view
                rideAcceptLayout();
                break;
            case R.id.acceptedRideNavigateButton:
                if (jobRequest != null) {
                    if(RideNo == 1){
                        String path = "http://maps.google.com/maps?daddr=" + lat1 + "," + long1;
                        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                                Uri.parse(path));
                        startActivity(intent);
                    } else if(RideNo == 2){
                        String path = "http://maps.google.com/maps?daddr=" + lat2 + "," + long2;
                        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                                Uri.parse(path));
                        startActivity(intent);
                    } else if(RideNo == 3){
                        String path = "http://maps.google.com/maps?daddr=" + lat3 + "," + long3;
                        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                                Uri.parse(path));
                        startActivity(intent);
                    }

                }
                break;
            case R.id.acceptedRideCallButton:
                Toast.makeText(requireContext(), "Call user", Toast.LENGTH_LONG).show();
                break;
            case R.id.acceptedRideCancelButton:
            case R.id.cancelTripButton:
                Toast.makeText(requireContext(), "Cancel Trip", Toast.LENGTH_LONG).show();
                break;
            case R.id.acceptedRideArrivedButton:
                verifyRideLayout();
                break;
            case R.id.verifyRideButton:
                if (otpEditText != null && !otpEditText.getText().toString().isEmpty() && otpEditText.getText().toString().length() == 4) {
                    if (jobRequest.getOTP().equalsIgnoreCase(otpEditText.getText().toString())) {
                        queueReference.child(jobRequest.getId()).child("isVerified").setValue("true");
                        queueReference.child(jobRequest.getId()).child("isCompleted").setValue("false");
                        // show ride in progress layout
                        rideInProgress();

                    }
                }
                break;
            case R.id.rideNavigateButton:
                if(RideNo == 1){
                    String path = "http://maps.google.com/maps?daddr=" + dlat1 + "," + dlong1;
                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                            Uri.parse(path));
                    startActivity(intent);
                } else if(RideNo == 2){
                    String path = "http://maps.google.com/maps?daddr=" + dlat1 + "," + dlong1 + "+to:" + dlat2 + "," + dlong2;
                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                            Uri.parse(path));
                    startActivity(intent);
                } else if(RideNo == 3){
                    String path = "http://maps.google.com/maps?daddr=" + dlat1 + "," + dlong1 + "+to:" + dlat2 + "," + dlong2 + "+to:" + dlat3 + "," + dlong3;
                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                            Uri.parse(path));
                    startActivity(intent);
                }
                break;
            case R.id.completeRideButton:
                queueReference.child(jobRequest.getId()).child("isCompleted").setValue("true");
                // clear the job request and reset the has ride
                //jobRequest = null;
                hasRide = false;
                rideSearchingLayout();
                map.clear();
                MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_cur_location));
                currentMarker = map.addMarker(markerOptions);
                Intent paymentIntent = new Intent(requireContext(), RideCompleteActivty.class);
                paymentIntent.putExtra("RideInfo", jobRequest);
                startActivity(paymentIntent);
                break;

        }
    }

    private void updateJobQueue(JobRequest changedJob) {
        if (jobRequest == null) {
            jobRequest = changedJob;
        } else {
            jobRequest.setOTP(changedJob.getOTP());
            jobRequest.setAccepted(changedJob.getAccepted());
            jobRequest.setIsCompleted(changedJob.getIsCompleted());
            jobRequest.setIsVerified(changedJob.getIsVerified());
            jobRequest.setRide_cost(changedJob.getRide_cost());
        }


    }

    @Override
    public void onResume() {
        super.onResume();
        if (hasRide && jobRequest.getIsCompleted().equalsIgnoreCase("true")) {
            hasRide = false;
            jobRequest = null;
            isReadingQueue = false;

        }
        Log.e("App resume", "resumed");
    }
}