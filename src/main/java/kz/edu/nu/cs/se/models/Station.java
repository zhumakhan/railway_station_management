package kz.edu.nu.cs.se.models;

import kz.edu.nu.cs.se.constants.CONST;

public class Station {
		public int StationID;
	    public String name;
	    public String adress;
		public float longitute;
	    public float latitude;
	    public Station(){
	    }
	    public Station(int id, String n, String a, Float lat, Float lng){
	    	this.StationID = id;
	        this.name = n;
	        this.adress = a;
	        this.longitute = lat;
	        this.latitude = lng;
	    }
	    public String getCreateQuery(){
	    	return "INSERT INTO "+CONST.STATION_TABLE_NAME + " VALUES (default, " + "'"+
	    			name+"','"+adress+"',"+longitute+","+latitude+");";
	    }
	    public Boolean isFormatValid(){
	    	return name.length() > 0 && name.length()  < 100 &&
	    			adress.length() > 0 && adress.length()  < 100 &&
	    			longitute > 0 && longitute > 0 &&
	    			latitude > 0 && latitude < 100;
	    }
	    public String toString(){
	    	return "name: " + name + "\n adress: "+adress + "\n longitute: "+longitute + "\n latitute: "+latitude;
	    }
}
