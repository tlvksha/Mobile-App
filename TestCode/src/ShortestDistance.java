import java.io.File;
import java.util.Vector;

import org.apache.poi.sl.draw.geom.SqrtExpression;

public class ShortestDistance {
	
	private StationFileInput stationFileInput;
	private Vector<String> getStation;
	private Vector<Double> getGPSx;
	private Vector<Double> getGPSy;
	
	public ShortestDistance(File file){
		stationFileInput = new StationFileInput(file);
		getGPSx = stationFileInput.getGPSx();
		getGPSy = stationFileInput.getGPSy();
		getStation = stationFileInput.getStationName();
		
	}
	
	public String DistanceCalculate(double x, double y){
		double distance = 0;
		double minDistance = 1;
		double factor = 0;
		int index = 0;
		
		for(int i = 0; i < getGPSx.size(); i++){
			factor = Math.pow((getGPSx.elementAt(i)-x), 2) + Math.pow((getGPSy.elementAt(i)-y), 2);
			distance = Math.sqrt(factor);
			//System.out.println("this is " + getStation.elementAt(i) + "'s distance : " + distance);
			
			if(minDistance > distance){
				minDistance = distance;
				index = i;
				//System.out.println("this is minDistance : " + minDistance);
			}
		}
		return getStation.elementAt(index);
	}
	
	
	
	
}
