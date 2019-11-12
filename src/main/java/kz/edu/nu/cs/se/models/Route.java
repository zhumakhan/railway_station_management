package kz.edu.nu.cs.se.models;

import java.util.List;

public class Route {
	public int RouteID;
	public List<Integer> stations;
    public Route(int RouteID, int ... stationID){
    	this.RouteID = RouteID;
    	for(int id:stationID){
    		stations.add(id);
    	}
    }
}
