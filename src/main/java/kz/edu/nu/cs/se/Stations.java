package kz.edu.nu.cs.se;

import java.util.ArrayList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.google.gson.Gson;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import kz.edu.nu.cs.se.SearchSchedule.Schedule;
import kz.edu.nu.cs.se.constants.CONST;
import kz.edu.nu.cs.se.db.DBConnector;
import kz.edu.nu.cs.se.models.Station;
import kz.edu.nu.cs.se.util.JWT;
import kz.edu.nu.cs.se.util.Pair;


@Path("/stations")
public class Stations {
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getStations(){
		Connection conn = DBConnector.getConnection();
		Statement stmt = null;
		String query = ("select * from " + CONST.STATION_TABLE_NAME);
		ArrayList<Station> stations;
        try { 	
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			stations = new ArrayList<Station>();
			while (rs.next()) {
				stations.add(new Station(rs.getInt("StationID"),rs.getString("Name"),rs.getString("Adress"),rs.getFloat("Longitute"),rs.getFloat("Latitude")));
			}
		} catch (SQLException e ) {
            throw new Error("Problem", e);
        } finally {

            if (stmt != null) { try {
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
				} 
            }
        }
	    Gson gson = new Gson();
	   	String json = gson.toJson(stations);
	   	return Response.ok(json).header("Access-Control-Allow-Origin", "*").build();
	}
	
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response postStations(@HeaderParam("Authorization") String token, Station station){
		Connection conn = DBConnector.getConnection();
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(station.getCreateQuery());
			int temp = stmt.executeUpdate();
			if(temp > 0) return Response.ok().entity("Well done!").build();
			else return Response.status(400).entity("Ooops something went wrong!").build();
		} catch (SQLException e1) {
			return Response.status(400).header("Access-Control-Allow-Origin", "*").entity("database error, try again!!!").build();
		}
	}

	@PUT
    @Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
    public Response putStation(@HeaderParam("Authorization") String token, Station station){
    	int temp = 0; 
        Pair<Station,Boolean> pair = authorize(token);
        if(pair.first == null)return Response.status(400).header("Access-Control-Allow-Origin", "*").entity("Invalid token!!!").build();
        if(pair.second == true)return Response.status(400).header("Access-Control-Allow-Origin", "*").entity("Token is expired, please authorize again!!!").build();
        if(!station.isFormatValid())
        	return Response.status(400).header("Access-Control-Allow-Origin", "*").entity("wrong data format!!!").build();
			Connection conn = DBConnector.getConnection();
			PreparedStatement stmt = null;
        try {
			stmt = conn.prepareStatement("UPDATE "+CONST.STATION_TABLE_NAME + " SET Name='"+station.name + "',Adress='"+station.adress+"',Longitute="+station.longitute+ ",Latitude="+station.latitude + "WHERE StationID = " + station.StationID);
        	temp = stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            return Response.status(400).header("Access-Control-Allow-Origin", "*").build();
        }
        if (temp > 0) {
            return Response.ok("Successfully updated!!!").entity("Successfully updated!!!").header("Access-Control-Allow-Origin", "*").build();
        } else {
            return Response.status(400).header("Access-Control-Allow-Origin", "*").entity("database error, try again!!!").build();
        }
    }

	@DELETE
	@Path("{sid:[0-9]{1,20}}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteStation(@HeaderParam("Authorization") String token, @PathParam("sid") String sid) {
		if(sid == null || !sid.matches("[0-9]{1,20}"))
			return Response.status(400).entity("invalid query parame").header("Access-Control-Allow-Origin", "*").build();
        Pair<Station,Boolean> pair = authorize(token);
        if(pair.first == null)return Response.status(400).header("Access-Control-Allow-Origin", "*").entity("Invalid token!!!").build();
        if(pair.second == true)return Response.status(400).header("Access-Control-Allow-Origin", "*").entity("Token is expired, please authorize again!!!").build();
		Connection conn = DBConnector.getConnection();
        PreparedStatement stmt = null;
        try {
        	stmt = conn.prepareStatement("DELETE FROM "+CONST.STATION_TABLE_NAME+" WHERE StationID = '"+sid+"'" );
			int temp = stmt.executeUpdate();
			if(temp > 0) return Response.ok().entity("Successfully updated!").build();
			else return Response.status(400).entity("Ooops something went wrong!").build();
        } catch (SQLException e1) {
			return Response.status(400).header("Access-Control-Allow-Origin", "*").entity("database error, try again!!!").build();
		}  
    }

	public Pair<Station,Boolean> authorize(String token){
        Station station = null;
        if(token == null || token.length() == 0) return new  Pair<Station,Boolean>(null,false);
        if(token.matches(CONST.JWT_PATTERN)){
        	Pair< String, Long > p = null;
        	try{
        		p = JWT.parseJWT(token);
        	}catch(Throwable t){
        		return new  Pair<Station,Boolean>(null,false);
        	}
            if(p.first == null || p.first.length() == 0)return new Pair(null, false);
            long curr = System.currentTimeMillis();
            if(curr > p.second){
                return new  Pair<Station,Boolean>(new Station(),true);//second argument in pair is true iff token is expired;
            }
            Connection conn = DBConnector.getConnection();
            PreparedStatement stmt = null;
			try {
				stmt = conn.prepareStatement("SELECT * FROM "+CONST.STATION_TABLE_NAME);
				stmt.execute();
				ResultSet rs = stmt.getResultSet();
				if(rs != null && rs.next()) {
                    station = new Station(rs.getInt("StationID"), rs.getString("Name"),rs.getString("Adress"),rs.getFloat("Longitute"),rs.getFloat("Latitude")); 
                }else{
                	return new Pair<Station,Boolean>(null,false);
                }
			} catch (SQLException e1) {
				return new Pair<Station,Boolean>(null, false);
			}
        }else{
            return new Pair<Station,Boolean>(null, false);
        } 
        return new Pair<Station,Boolean>(station, false);
    }
}

