package kz.edu.nu.cs.se;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ApplicationPath;

import kz.edu.nu.cs.se.db.DBConnector;

import com.google.gson.Gson;

@Path("/schedules")
public class SearchSchedule {
	public SearchSchedule(){
		
	}
    @GET
    @Produces(MediaType.APPLICATION_JSON)
	public Response GetSchedules(@QueryParam("from")String from, @QueryParam("to")String to, @QueryParam("date") String date){
    	ArrayList<Schedule> lists = new ArrayList<Schedule>();
		String namePattern= "[0-9A-Za-z]*";
		String datePattern = "\\d{4}-\\d{2}-\\d{2}";//YYYY-MM-DD;    	
		if ( from == null || from.length() > 45 || !from.matches(namePattern)
				|| to == null || to.length() > 45 || !to.matches(namePattern)
    			|| date == null || date.length() != 10 || !date.matches(datePattern))
    	{
			return Response.status(400).header("Access-Control-Allow-Origin", "*").build();
    	}
    	Connection conn = DBConnector.getConnection();
    	PreparedStatement stmt1 = null, stmt2 = null, stmt3 = null;
    	String query1 = "SELECT S1.StationID, RS1.Station_Order, S2.StationID, RS2.Station_Order INTO @ID1,@O1,@ID2,@O2 "+
    					" FROM Stations S1 join Stations S2 on (S1.Name = '"+from +"' AND S2.Name = '"+to +"')"+
    					" join RoutesToStations RS1 on RS1.Stations_StationID = S1.StationID "+
    					" join RoutesToStations RS2 on RS2.Stations_StationID = S2.StationID "+
    					" WHERE RS2.Routes_idRoute = RS1.Routes_idRoute; ";

		String query2 = " SELECT TS1.DepartureTime AS dep, TS2.ArrivalTime AS arr, T.Name AS tname, T.Type AS ttype, TS1.TravelInstrance_ID AS id "+
						" FROM TrainSchedules AS TS1 join TrainSchedules AS TS2 "+
						" on TS1.TravelInstrance_ID = TS2.TravelInstrance_ID "+
						" join TravelInstance TI on TI.ID = TS2.TravelInstrance_ID "+
						" join Trains AS T on T.TrainID = TI.TrainID "+
						" WHERE TS1.Stations_StationID = @ID1 AND TS2.Stations_StationID = @ID2 "+
						" AND DATE(TS1.DepartureTime) = '"+date+"' AND TS1.Direction = SIGN(@O2 - @O1); ";
		String query3 = " SET @ID1 = NULL; ";
    	try {
			stmt1 = conn.prepareStatement(query1);
			stmt2 = conn.prepareStatement(query2);
			stmt3 = conn.prepareStatement(query3);
			stmt1.execute();
			stmt2.execute();
			stmt3.execute();
			lists = new ArrayList<Schedule>();
			try(ResultSet rs = stmt2.getResultSet()){
				while (rs != null && rs.next()) {
					Schedule sh = new Schedule(rs.getString("id"),rs.getString("tname"), rs.getString("ttype"),rs.getString("arr"),rs.getString("dep") );
					lists.add(sh); 
				}
			}catch(SQLException e){
				return Response.status(400).header("Access-Control-Allow-Origin", "*").build();
			}
		} catch (SQLException e ) {
            return Response.status(400).header("Access-Control-Allow-Origin", "*").build();
        } finally {
            if (stmt1 != null) {
            	 try {
					stmt1.close();
				} catch (SQLException e) {
					return Response.status(400).header("Access-Control-Allow-Origin", "*").build();
				} 
			}
        }
    Gson gson = new Gson();
    System.out.println(lists);
	String json = gson.toJson(lists);
	return Response.ok(json).header("Access-Control-Allow-Origin", "*").build();
    }

    class Schedule {
    	  String TravelInstanceID;
		  String TrainName;
		  String TrainType;
		  String DepartureTime;
		  String ArrivalTime;
		  public Schedule(String id, String trainName, String trainType,String arrivalTime, String departureTime) {
			  this.TravelInstanceID = id;
			  this.TrainName = trainName;
			  this.TrainType = trainType;
		      this.ArrivalTime = arrivalTime;
		      this.DepartureTime = departureTime;
		}
		public String toString(){
			return this.TravelInstanceID+" "+this.TrainName+" "+this.TrainType+" "+ this.ArrivalTime+" "+this.DepartureTime;
		}
	  }

}
