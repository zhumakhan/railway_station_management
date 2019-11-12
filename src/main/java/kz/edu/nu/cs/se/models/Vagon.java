package kz.edu.nu.cs.se.models;

public class Vagon {
	String Vagon_Number;
	String Type;
	String Num_Of_Seats;
	String SeatStatus;
	public Vagon(String vn, String t, String ns, String ss){
		this.Vagon_Number = vn;
		this.Type = t;
		this.Num_Of_Seats = ns;
		this.SeatStatus = ss;
	}
	
}
