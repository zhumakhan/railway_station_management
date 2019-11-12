package kz.edu.nu.cs.se;

import javax.servlet.*;

import kz.edu.nu.cs.se.db.DBConnector;
public class ContextListener implements ServletContextListener {
    //server startup
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context=sce.getServletContext();
        String dburl=context.getInitParameter("dbUrl");
        String dbusername=context.getInitParameter("dbUserName");
        String dbpassword=context.getInitParameter("dbPassword");

        DBConnector.createConnection(dburl, dbusername, dbpassword);
        if(DBConnector.getConnection() == null){
        	System.out.println("con is null");
        }else{
        	System.out.println("Connection Establised.........");
        }
    }
    //stoping the server
    public void contextDestroyed(ServletContextEvent sce) {
        DBConnector.closeConnection();
    }

}

