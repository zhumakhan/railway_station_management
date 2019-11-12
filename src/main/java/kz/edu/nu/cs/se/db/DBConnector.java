package kz.edu.nu.cs.se.db;
import java.sql.*;
public class DBConnector {

    private static Connection conn;

    public static void createConnection(String dbUrl,String dbusername,String dbPassword){
        try {
            System.out.println(dbUrl);
            Class.forName("com.mysql.jdbc.Driver");
            conn=DriverManager.getConnection(dbUrl, dbusername, dbPassword); 
        } catch (Exception ex) {
            ex.printStackTrace();
        }    
    }

    public static Connection getConnection(){
        return conn;
    }

    public static void closeConnection(){
        if(conn!=null){
            try {
                conn.close();
            } catch (SQLException ex) {
                 ex.printStackTrace();
            }
        }

    }



}