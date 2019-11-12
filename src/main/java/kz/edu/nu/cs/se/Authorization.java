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

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ApplicationPath;

import com.google.gson.Gson;

import kz.edu.nu.cs.se.models.*;
import kz.edu.nu.cs.se.util.JWT;
import kz.edu.nu.cs.se.util.Pair;
import kz.edu.nu.cs.se.constants.*;
import kz.edu.nu.cs.se.db.DBConnector;

@Path("/")
public class Authorization{
    @POST
    @Path("register")
    @Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
    public Response register(User user){
    	System.out.println(user);
        Connection conn = DBConnector.getConnection();
        PreparedStatement stmt = null;
        PreparedStatement duplicateMailCheck = null;
        int temp = 0;
        //check validity of request body
        if(user == null){
        	return Response.status(400).entity("No valid user data!!!").header("Access-Control-Allow-Origin", "*").build();
        }
        if(!user.isFormatValid())
        	//return Response.status(400).header("Access-Control-Allow-Origin", "*").entity("wrong data format!!!").build();
        	return Response.status(400).header("Access-Control-Allow-Origin", "*").header("Access-Control-Allow-Methods", "GET, PUT, POST, DELETE").header( "Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, Authorization").entity("wrong data format!!!").build();
		try {
			stmt = conn.prepareStatement(user.getCreateQuery());
			duplicateMailCheck = conn.prepareStatement("SELECT * FROM "+CONST.USER_TABLE_NAME+" WHERE mail = '"+user.mail+"'");
			duplicateMailCheck.execute();
			ResultSet rs = duplicateMailCheck.getResultSet();
			if(rs != null && rs.next())//if email is in database already;
	               //return Response.status(400).header("Access-Control-Allow-Origin", "*").entity("Email already exists").build();
				return Response.status(400).header("Access-Control-Allow-Origin", "*").header("Access-Control-Allow-Methods", "GET, PUT, POST, DELETE").header( "Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, Authorization").entity("Email already exists").build();
		}catch (SQLException e) {
			return Response.status(400).header("Access-Control-Allow-Origin", "*").entity("Database error, try again!!! "+e.toString()).build();
		}
        try {
        	temp = stmt.executeUpdate();
            stmt.close();
            duplicateMailCheck.close();
        } catch (SQLException e) {
            return Response.status(400).header("Access-Control-Allow-Origin", "*").build();
        }
        if (temp > 0) {
            //create jwt, return
            String jwt = JWT.createJWT(user.mail, CONST.ISSUER,"register", CONST.JWT_ACCESS_DURATION_IN_MILLISEC );
            //return Response.ok(jwt).header("Access-Control-Allow-Origin", "*").build();
            return Response.ok(jwt).header("Access-Control-Allow-Methods", "GET, PUT, POST, DELETE").header( "Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, Authorization").build();

        } else {
            //return Response.status(400).header("Access-Control-Allow-Origin", "*").entity("database error, try again!!!").build();
        	return Response.status(400).header("Access-Control-Allow-Origin", "*").header("Access-Control-Allow-Methods", "GET, PUT, POST, DELETE").header( "Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, Authorization").entity("database error").build();

        }
    }
    
    
    @POST
    @Path("login")
    @Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
    public Response login(User user){
    	if(user == null){
    		return Response.status(400).header("Access-Control-Allow-Origin", "*").entity("Not valid user data!!!").build();
    	}
    	//we need to set some value to firstname and surname so that user data format will be valid;
    	user.firstname = "any non empty string with length < 100";
    	user.surname = "any non empty string with length < 100";
        Connection conn = DBConnector.getConnection();
        PreparedStatement dbUser = null;
        if(!user.isFormatValid())
        	return Response.status(400).header("Access-Control-Allow-Origin", "*").entity("wrong data format!!!").build();
        try {
			dbUser = conn.prepareStatement("SELECT * FROM "+CONST.USER_TABLE_NAME+" WHERE mail = '"+user.mail+"' AND password = '"+user.password+"'");
			dbUser.execute();
			ResultSet rs = dbUser.getResultSet();
			if(rs != null && rs.next()){
				//valid, return jwt;
				String jwt = JWT.createJWT(user.mail, CONST.ISSUER,"login", CONST.JWT_ACCESS_DURATION_IN_MILLISEC );
				return Response.ok(jwt).header("Access-Control-Allow-Origin", "*").build();
			}else{
				return Response.status(400).header("Access-Control-Allow-Origin", "*").entity("wrong creditionals!!!").build();
			}		
        } catch (SQLException e1) {
			return Response.status(400).header("Access-Control-Allow-Origin", "*").entity("database error, try again!!!").build();
		}
    }
    
    
    @GET
    @Path("me")
    //@Produces(MediaType.APPLICATION_JSON)
    public Response getMe(@HeaderParam("Authorization") String token) {
    	System.out.println(token);
        Pair<User,Boolean> pair= authorize(token); 
        if(pair.first == null)return Response.status(400).header("Access-Control-Allow-Origin", "*").entity("Invalid token!!!").build();
        if(pair.second == true)return Response.status(400).header("Access-Control-Allow-Origin", "*").entity("Token is expired, please authorize again!!!").build();
        Gson gson = new Gson();
        String json = gson.toJson(pair.first, User.class);
        return Response.ok(json).build();
    }
    
    @DELETE
    @Path("me")
    //@Produces(MediaType.APPLICATION_JSON)
    public Response deleteMe(@HeaderParam("Authorization") String token) {
        Pair<User,Boolean> pair= authorize(token); 	
        if(pair.first == null)return Response.status(400).header("Access-Control-Allow-Origin", "*").entity("Invalid token!!!").build();
        if(pair.second == true)return Response.status(400).header("Access-Control-Allow-Origin", "*").entity("Token is expired, please authorize again!!!").build();
        Connection conn = DBConnector.getConnection();
        PreparedStatement stmt = null;
        try {
        	stmt = conn.prepareStatement("DELETE FROM "+CONST.USER_TABLE_NAME+" WHERE mail = '"+pair.first.mail+"'");
			int temp = stmt.executeUpdate();
			if(temp > 0) return Response.ok().build();
			else return Response.status(400).entity("Ooops something went wrong!").build();
        } catch (SQLException e1) {
			return Response.status(400).header("Access-Control-Allow-Origin", "*").entity("database error, try again!!!").build();
		}  
    }
    
    @PUT
    @Path("me")
    //@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response putMe(@HeaderParam("Authorization") String token, User user){
    	int temp = 0;
        Pair<User,Boolean> pair = authorize(token);
        if(pair.first == null)return Response.status(400).header("Access-Control-Allow-Origin", "*").entity("Invalid token!!!").build();
        if(pair.second == true)return Response.status(400).header("Access-Control-Allow-Origin", "*").entity("Token is expired, please authorize again!!!").build();
        user.mail = pair.first.mail; //do not allow to change mail;
        if(!user.isFormatValid())
        	return Response.status(400).header("Access-Control-Allow-Origin", "*").entity("wrong data format!!!").build();
            Connection conn = DBConnector.getConnection();
            PreparedStatement stmt = null;
        try {
			stmt = conn.prepareStatement("UPDATE "+CONST.USER_TABLE_NAME + " SET firstname='"+user.firstname + "',password='"+user.password+"',surname='"+user.surname + "' WHERE mail='"+ user.mail +"'");
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
    
    
    public Pair<User,Boolean> authorize(String token){
        User user = null;
        if(token == null || token.length() == 0)return new  Pair<User,Boolean>(null,false);
        if(token.matches(CONST.JWT_PATTERN)){
        	Pair< String, Long > p = null;
        	try{
        		p = JWT.parseJWT(token);
        	}catch(Throwable t){
        		return new  Pair<User,Boolean>(null,false);
        	}
            if(p.first == null || p.first.length() == 0)return new Pair(null, false);
            long curr = System.currentTimeMillis();
            if(curr > p.second){
                return new  Pair<User,Boolean>(new User(),true);//second argument in pair is true iff token is expired;
            }
            Connection conn = DBConnector.getConnection();
            PreparedStatement stmt = null;
			try {
				stmt = conn.prepareStatement("SELECT * FROM "+CONST.USER_TABLE_NAME+ " WHERE mail = '"+p.first+"'");
				stmt.execute();
				ResultSet rs = stmt.getResultSet();
				if(rs != null && rs.next()) {
                    user = new User(rs.getString("mail"),rs.getString("password"),rs.getString("firstname"),rs.getString("surname")); 
                }else{
                	return new Pair<User,Boolean>(null,false);
                }
			} catch (SQLException e1) {
				return new Pair<User,Boolean>(null, false);
			}
        }else{
            return new Pair<User,Boolean>(null, false);
        } 
        return new Pair<User,Boolean>(user, false);
    }
}
