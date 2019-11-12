package kz.edu.nu.cs.se;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Statement;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ApplicationPath;

import com.google.gson.Gson;

import kz.edu.nu.cs.se.constants.*;
import kz.edu.nu.cs.se.db.DBConnector;
import kz.edu.nu.cs.se.models.Station;
import kz.edu.nu.cs.se.models.Train;
import kz.edu.nu.cs.se.models.Vagon;
import kz.edu.nu.cs.se.util.JWT;
import kz.edu.nu.cs.se.util.Pair;
@Path("/trains")
public class Trains {
	@GET
	@Path("{tid:[0-9]{1,20}}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTrainBytid(@PathParam("tid")String tid){
		System.out.println(tid);
		if(tid == null || !tid.matches("[0-9]{1,20}"))
			return Response.status(400).entity("invalid query parame").header("Access-Control-Allow-Origin", "*").build();
		Connection conn = DBConnector.getConnection();
		Train train = null;
    	try {
    		PreparedStatement stmt = conn.prepareStatement("SELECT T.Type, T.Name "+ 
				" FROM "+ CONST.TRAIN_TABLE_NAME +" T join  " + CONST.TRAVELINSTANCE_TABLE_NAME+ " TI on T.TrainID = TI.TrainID AND TI.ID = '"+tid+"'");		
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
			if (rs != null && rs.next()) {
				train = new Train(rs.getInt("TrainID"), rs.getString("Type"),rs.getString("Name")); 
			}
			stmt = conn.prepareStatement("SELECT V.Vagon_Number, V.Type, V.Number_Of_Seats, S.SeatStatus "+
				" FROM "+ CONST.VAGON_TABLE_NAME + " V join "+ CONST.SEATSTATUS_TABLE_NAME+" S on V.Vagon_Number = S.Vagons_Vagon_Number AND "+
					" S.TravelInstance_ID = '"+tid+"'");
			rs = stmt.getResultSet();
			while (rs != null && rs.next()) {
				train.vagons.add(new Vagon(rs.getString("Vagon_Number"),rs.getString("Type"),
				rs.getString("Number_Of_Seats"),rs.getString("SeatStatus"))); 
			}
			if (stmt != null) {try {stmt.close();} catch (SQLException e) {} }
			Gson gson = new Gson();
			String json = gson.toJson(train);
			return Response.ok(json).header("Access-Control-Allow-Origin", "*").build();
		}catch(SQLException e){
			return Response.status(400).header("Access-Control-Allow-Origin", "*").build();
		}   		
	}
	
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response postTrain(@HeaderParam("Authorization") String token, Train train){
		Pair<Train,Boolean> pair = authorize(token);
        if(pair.first == null)return Response.status(400).header("Access-Control-Allow-Origin", "*").entity("Invalid token!!!").build();
        if(pair.second == true)return Response.status(400).header("Access-Control-Allow-Origin", "*").entity("Token is expired, please authorize again!!!").build();
		
		Connection conn = DBConnector.getConnection();
		PreparedStatement stmt = null;
    	try {
			stmt = conn.prepareStatement(train.getCreateQuery());
			System.out.println(stmt);
			int temp = stmt.executeUpdate();
			System.out.println(temp);
			if(temp > 0) return Response.ok().entity("Well done!").build();
			else return Response.status(400).entity("Ooops something went wrong!").build();
		} catch (SQLException e1) {
			return Response.status(400).header("Access-Control-Allow-Origin", "*").entity("database error, try again!!!").build();
		} 		
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTrains(){		
		Connection conn = DBConnector.getConnection();
		Statement stmt = null;
		String query = ("SELECT *" + " FROM "+ CONST.TRAIN_TABLE_NAME);	
		ArrayList<Train> trains;
        try { 	
        	stmt = conn.createStatement();
        	ResultSet rs = stmt.executeQuery(query);
			trains = new ArrayList<Train>();
			while (rs.next()) {
				trains.add(new Train(rs.getInt("TrainID"),rs.getString("Name"),rs.getString("Type")));
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
		String json = gson.toJson(trains);
		return Response.ok(json).header("Access-Control-Allow-Origin", "*").build();
	}

	public Pair<Train,Boolean> authorize(String token){
        Train train = null;
        if(token == null || token.length() == 0) return new  Pair<Train,Boolean>(null,false);
        if(token.matches(CONST.JWT_PATTERN)){
        	Pair< String, Long > p = null;
        	try{
        		p = JWT.parseJWT(token);
        	}catch(Throwable t){
        		return new  Pair<Train,Boolean>(null,false);
        	}
            if(p.first == null || p.first.length() == 0)return new Pair(null, false);
            long curr = System.currentTimeMillis();
//            if(curr > p.second){
//                return new  Pair<Train,Boolean>(new Train(),true);//second argument in pair is true iff token is expired;
//            }
            Connection conn = DBConnector.getConnection();
            PreparedStatement stmt = null;
			try {
				stmt = conn.prepareStatement("SELECT * FROM "+CONST.TRAIN_TABLE_NAME);
				stmt.execute();
				ResultSet rs = stmt.getResultSet();
				if(rs != null && rs.next()) {
                    train = new Train(rs.getInt("TrainID"), rs.getString("Name"),rs.getString("Type")); 
                }else{
                	return new Pair<Train,Boolean>(null,false);
                }
			} catch (SQLException e1) {
				return new Pair<Train,Boolean>(null, false);
			}
        }else{
            return new Pair<Train,Boolean>(null, false);
        } 
        return new Pair<Train,Boolean>(train, false);
    }
}
