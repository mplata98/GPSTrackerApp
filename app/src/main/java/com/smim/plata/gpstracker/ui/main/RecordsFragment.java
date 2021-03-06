package com.smim.plata.gpstracker.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.smim.plata.gpstracker.DataModel;
import com.smim.plata.gpstracker.MainActivity;
import com.smim.plata.gpstracker.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;


public class RecordsFragment extends Fragment {

    TextView distance,time;
    String distanceVal="", timeVal="";


    public static RecordsFragment newInstance(int index) {
        RecordsFragment fragment = new RecordsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onResume(){
        super.onResume();
        distance.setText(distanceVal);
        time.setText(timeVal);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_records, container, false);
        distance = root.findViewById(R.id.recordsDistanceValue);
        time = root.findViewById(R.id.recordsTimeValue);
        distance.setText(distanceVal);
        time.setText(timeVal);
        ((MainActivity) getActivity()).setRecordsFragment(this);
        return root;
    }

    public void updateRecords(ArrayList<DataModel> list){
        Double maxDist = 0.0;
        String maxDistStr = "";
        long maxTime=0;
        for (int i=0;i<list.size();i++){
            if(list.get(i).getDistance()>maxDist){
                maxDist= list.get(i).getDistance();
                maxDistStr = list.get(i).getDistanceRounded();
            }
            if(calculateTimeFromString(list.get(i).getDateB())-calculateTimeFromString(list.get(i).getDateA())>maxTime){
                maxTime=calculateTimeFromString(list.get(i).getDateB())-calculateTimeFromString(list.get(i).getDateA());
            }
        }

        int seconds = (int) (maxTime / 1000) % 60 ;
        int minutes = (int) ((maxTime / (1000*60)) % 60);
        int hours   = (int) ((maxTime / (1000*60*60)) % 24);


        distanceVal = (String.valueOf(maxDistStr+"km"));
        timeVal = (String.valueOf(String.valueOf(hours)+"h "+String.valueOf(minutes)+"m "+String.valueOf(seconds)+"s"));
        distance.setText(distanceVal);
        time.setText(timeVal);
    }

    public long calculateTimeFromString(String str_date){
        DateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
        DateFormat formatter2 = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        Date date = null;

        try {
            date = (Date) formatter.parse(str_date);
        } catch (ParseException e) {
            try {
                date = (Date) formatter2.parse(str_date);
            } catch (ParseException parseException) {
                parseException.printStackTrace();
            }
        }
        return date.getTime();
    }
}