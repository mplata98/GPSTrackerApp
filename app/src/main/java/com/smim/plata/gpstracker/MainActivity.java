package com.smim.plata.gpstracker;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.smim.plata.gpstracker.ui.main.HistoryFragment;
import com.smim.plata.gpstracker.ui.main.MapFragment;
import com.smim.plata.gpstracker.ui.main.RecordsFragment;
import com.smim.plata.gpstracker.ui.main.SectionsPagerAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LocationListener {

    protected LocationManager locationManager;
    protected Double latitude = 0.0, longitude = 0.0;
    protected MapFragment mapFragment;
    protected HistoryFragment historyFragment;
    private boolean locationUpdates = false;
    private static final int RC_SIGN_IN = 123;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private StorageReference fileRef;
    private static final String FILE_NAME="paths.json";
    private File file;
    private FileReader fileReader = null;
    private FileWriter fileWriter = null;
    private BufferedReader bufferedReader= null;
    private BufferedWriter bufferedWriter = null;
    private String response = null;
    private String userID="-1";
    private JSONObject messageDetails;
    private Boolean isUserExisting;
    private ArrayList<Double> userPath;
    private DatabaseReference userCatalog;
    private HashMap<String,String> currentPath;
    private FirebaseDatabase db;
    private DatabaseReference root;
    public ArrayList<DataModel> tmpList;
    private String lastStartingDate;
    private RecordsFragment recordsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        FloatingActionButton fab = findViewById(R.id.tracking_button);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        userPath = new ArrayList<>();
        tmpList = new ArrayList<>();
        lastStartingDate="";
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            return;
        }
        //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(locationUpdates){
                    Snackbar.make(view, "Tracking turned OFF", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }else {
                    Snackbar.make(view, "Tracking turned ON", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                switchLocationUpdates();
            }
        });

        createSignInIntent();
        db = FirebaseDatabase.getInstance("https://upheld-castle-311909-default-rtdb.europe-west1.firebasedatabase.app/");
        currentPath = new HashMap<>();

    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        this.mapFragment.updateLocation(latitude, longitude);
        userPath.add(latitude);
        userPath.add(longitude);
    }

    public Double[] getLocation() {
        return new Double[]{latitude, longitude};
    }

    public void setMapFragment(MapFragment mapFragment) {
        this.mapFragment = mapFragment;
        this.mapFragment.updateLocation(latitude, longitude);
    }

    public void switchLocationUpdates() {
        if (locationUpdates) {
            locationManager.removeUpdates(this);
            mapFragment.googleMap.clear();
            pushToDB(userPath);
            lastStartingDate="";
            userPath.clear();
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            lastStartingDate = Calendar.getInstance().getTime().toString();
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }
        locationUpdates = !locationUpdates;
        if(recordsFragment!=null && historyFragment!=null){
            this.recordsFragment.updateRecords(this.historyFragment.list);
        }
    }

    public void createSignInIntent() {
        // [START auth_fui_create_intent]
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.PhoneBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());

        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setIsSmartLockEnabled(false)
                        .build(),
                RC_SIGN_IN);
        // [END auth_fui_create_intent]
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                assert user != null;
                userID=user.getUid();
                this.root = db.getReference();
                this.userCatalog = root.child(userID);
                this.userCatalog.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(historyFragment==null){
                            tmpList.clear();
                        }else{
                            historyFragment.list.clear();
                        }
                        for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                            DataModel model = dataSnapshot.getValue(DataModel.class);
                            if(historyFragment==null){
                                tmpList.add(model);
                            }else{
                                historyFragment.list.add(model);
                            }

                        }
                        if(historyFragment!=null) {
                            historyFragment.myAdapter.mlist = historyFragment.list;
                            historyFragment.myAdapter.notifyDataSetChanged();
                            if(recordsFragment!=null){
                                recordsFragment.updateRecords(historyFragment.list);
                            }
                        }
                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                // ...
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        }
    }
    // [END auth_fui_result]

    public void signOut() {
        // [START auth_fui_signout]
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        // ...
                    }
                });
        // [END auth_fui_signout]
    }

    private void pushToDB(ArrayList<Double> path){
        if(!userID.equals("-1")){
            currentPath = new HashMap<>();
            currentPath.put("path",String.valueOf(path));
            currentPath.put("dateA", lastStartingDate);
            currentPath.put("dateB", Calendar.getInstance().getTime().toString());
            userCatalog.push().setValue(currentPath);
            this.recordsFragment.updateRecords(this.historyFragment.list);
        }
    }

    public void setHistoryFragment(HistoryFragment historyFragment) {
        this.historyFragment=historyFragment;
        this.historyFragment.list=tmpList;
    }


    public void setRecordsFragment(RecordsFragment recordsFragment) {
        this.recordsFragment = recordsFragment;
        if(historyFragment!=null){
            recordsFragment.updateRecords(historyFragment.list);
        }
    }
}