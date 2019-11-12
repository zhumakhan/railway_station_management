package kz.edu.nu.cs.se.constants;

public final class CONST {
	public static final String API_KEY 						= "TheJwtKey";
	public static final String ISSUER 						= "royalway";
	public static final long JWT_ACCESS_DURATION_IN_MILLISEC= 1000 * 1 * 31556952L / 12;//1 month
	public static final String TRAVELINSTANCE_TABLE_NAME    = "TravelInstance";
	public static final String USER_TABLE_NAME 				= "Users";
	public static final String TRAIN_TABLE_NAME 			= "Trains";
	public static final String STATION_TABLE_NAME 			= "Stations";
	public static final String VAGON_TABLE_NAME 			= "Vagons";
	public static final String SEATSTATUS_TABLE_NAME        = "SeatStatus";
	public static final String JWT_PATTERN                    = "^[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.?[A-Za-z0-9-_.+/=]*$";
}
