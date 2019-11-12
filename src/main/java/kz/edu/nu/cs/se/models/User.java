package kz.edu.nu.cs.se.models;
import kz.edu.nu.cs.se.constants.*;

public class User{
    public String mail;
    public String password;
	public String firstname;
    public String surname;
    public User(){
    }
    public User(String m, String p, String f, String s){
        this.mail = m;
        this.password = p;
        this.firstname = f;
        this.surname = s;
    }
    public String getCreateQuery(){
    	return "INSERT INTO "+CONST.USER_TABLE_NAME + " VALUES ('"+
    			mail+"','"+password+"','"+firstname+"','"+surname+"');";
    }
    public Boolean isFormatValid(){
    	return mail.matches("^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$") &&
                mail.length()> 0 && mail.length()  < 100 &&
                password.length() > 0 && firstname.length() > 0 &&
    			surname.length() > 0 && password.length() < 100 &&
                firstname.length() < 100 && surname.length() < 100;
    }
    public String toString(){
    	return "mail: " + mail + "\npass: "+password + "\nfname: "+firstname + "\nsname: "+surname;
    }
}
