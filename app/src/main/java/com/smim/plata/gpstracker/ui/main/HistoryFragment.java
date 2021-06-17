package com.smim.plata.gpstracker.ui.main;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.smim.plata.gpstracker.Adapter;
import com.smim.plata.gpstracker.DataModel;
import com.smim.plata.gpstracker.MainActivity;
import com.smim.plata.gpstracker.R;

import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class HistoryFragment extends Fragment {

    public RecyclerView recyclerView;
    public Adapter myAdapter;
    public ArrayList<DataModel> list;
    private Bundle savedInstanceState;

    //Map
    MapView mMapView;
    public GoogleMap googleMap;
    LatLng current = new LatLng(-34, 151);



    public static HistoryFragment newInstance() {
        HistoryFragment fragment = new HistoryFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.savedInstanceState = savedInstanceState;
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        list = new ArrayList<>();
        ((MainActivity) getActivity()).setHistoryFragment(this);
        myAdapter=new Adapter(getContext(),list);
        View root = inflater.inflate(R.layout.fragment_history, container, false);

        //Map
        mMapView = (MapView) root.findViewById(R.id.mapViewH);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;

                // For showing a move to my location button
                //googleMap.setMyLocationEnabled(true);

                // For dropping a marker at a point on the Map
                //googleMap.addMarker(new MarkerOptions().position(current).title("Marker Title").snippet("Marker Description"));

                // For zooming automatically to the location of the marker
                //CameraPosition cameraPosition = new CameraPosition.Builder().target(current).zoom(12).build();
                //googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        });


        recyclerView = root.findViewById(R.id.history_recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(myAdapter);
        myAdapter.setHistoryFragment(this);


        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    public void setMap(int positions){
        googleMap.clear();
        LatLng start = new LatLng(list.get(positions).pathToArray().get(0), list.get(positions).pathToArray().get(1));
        int sizemax = list.get(positions).pathToArray().size()-1;
        LatLng stop = new LatLng(list.get(positions).pathToArray().get(sizemax-1), list.get(positions).pathToArray().get(sizemax));
        CameraPosition cameraPosition = new CameraPosition.Builder().target(start).zoom(16).build();
        googleMap.addMarker(new MarkerOptions().position(start).title("Start"));
        googleMap.addMarker(new MarkerOptions().position(stop).title("Stop"));
        drawLine(pathToLatLng(list.get(positions).pathToArray()));
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    public ArrayList<LatLng> pathToLatLng(ArrayList<Double> path){
        ArrayList<LatLng> result = new ArrayList<>();
        for (int i=1;i<path.size();i= i+2){
            result.add(new LatLng(path.get(i-1),path.get(i)));
        }
        return result;
    }

    public void drawLine(ArrayList<LatLng> points){
        for (int i = 0; i < points.size() - 1; i++) {
            LatLng src = points.get(i);
            LatLng dest = points.get(i + 1);

            // mMap is the Map Object
            Polyline line = googleMap.addPolyline(
                    new PolylineOptions().add(
                            new LatLng(src.latitude, src.longitude),
                            new LatLng(dest.latitude,dest.longitude)
                    ).width(5).color(Color.RED).geodesic(true)
            );
        }
    }

}
