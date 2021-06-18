package com.smim.plata.gpstracker;

import java.util.ArrayList;

public class DataModel {
    String dateA;
    String dateB;
    String path;

    public DataModel() {}

    public DataModel(String dateA, String dateB,String path){
        this.dateA=dateA;
        this.dateB=dateB;
        this.path=path;
    }

    public String getDateA() {
        return dateA;
    }
    public String getDateB() {
        return dateB;
    }

    public String getPath() {
        return path;
    }

    public ArrayList<Double> getCenter() {
        ArrayList<Double> path = pathToArray();
        double maxV,minV,maxH,minH;
        maxV=path.get(0);
        minV=path.get(0);
        maxH=path.get(1);
        minH=path.get(1);
        for (int i=0;i<path.size();i++){
            if(i%2==0){
                if(path.get(i)> maxV) maxV=path.get(i);
                if(path.get(i)< minV) minV=path.get(i);
            }else{
                if(path.get(i)> maxH) maxH=path.get(i);
                if(path.get(i)< minH) minH=path.get(i);
            }
        }
        ArrayList<Double> result = new ArrayList<>();
        result.add(maxV-minV);
        result.add(maxH-minH);
        return result;
    }

    public ArrayList<Double> pathToArray(){
        String str[] = path.substring(1,path.length()-1).split(", ");
        ArrayList<Double> result = new ArrayList<>();
        for (int i=0;i<str.length;i++){
            result.add(Double.parseDouble(str[i]));
        }
        return result;
    }

    public String getDistanceRounded(){
        ArrayList<Double> path=pathToArray();
        double distance =0.0;
        for (int i=2;i<path.size();i=i+2){
            distance+= calcDistance(path.get(i-2),path.get(i-1),path.get(i),path.get(i+1));
        }

        return  String.valueOf(Math.floor(distance * 100) / 100);
    }

    public Double getDistance(){
        ArrayList<Double> path=pathToArray();
        double distance =0.0;
        for (int i=2;i<path.size();i=i+2){
            distance+= calcDistance(path.get(i-2),path.get(i-1),path.get(i),path.get(i+1));
        }

        return  distance;
    }

    private static double calcDistance(double lat1, double lon1, double lat2, double lon2) {
        if ((lat1 == lat2) && (lon1 == lon2)) {
            return 0;
        }
        else {
            double theta = lon1 - lon2;
            double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515;
            dist = dist * 1.609344;
            return (dist);
        }
    }

    public void setDateA(String date) {
        this.dateA = date;
    }

    public void setDateB(String date) {
        this.dateB = date;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
