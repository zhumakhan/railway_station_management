package kz.edu.nu.cs.se.models;

import java.util.ArrayList;
import java.util.List;

import kz.edu.nu.cs.se.constants.CONST;

public class Train{
	int TrainID;
	public String TrainType;
	public String TrainName;
	public List<Vagon> vagons;
	public Train(){
	}
	public Train(int id, String type, String name){
		this.TrainID = id;
		this.TrainType = type;
		this.TrainName = name;
		vagons = new ArrayList<Vagon>();
	}
    public String getCreateQuery(){
        return "INSERT INTO "+CONST.TRAIN_TABLE_NAME + " (TrainID, Name, Type) VALUES (default, " + "'"+
                TrainName+"','"+TrainType+"');";
    }
    public Boolean isFormatValid(){
        return TrainName.length() > 0 && TrainName.length()  < 100 &&
                TrainType.length() > 0 && TrainType.length()  < 18;
    }
    public String toString(){
        return "TrainName: " + TrainName + "\n TrainType: "+ TrainType;
    }
}
