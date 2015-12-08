import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Vector;

public class TestHashMap {
	
	private StationFileInput stationFileInput;
	private HashMap<String, Double> hashGPSx;
	private HashMap<String, Double> hashGPSy;
	private BufferedReader br;
	
	
	public TestHashMap(File file){
		stationFileInput = new StationFileInput(file);
		hashGPSx = new HashMap<String, Double>();
		hashGPSy = new HashMap<String, Double>();
		br = new BufferedReader(new InputStreamReader(System.in));
		setHashGPS();
		
	}
	
	public void setHashGPS(){
		String station ="";
		double gpsx;
		double gpsy;
		while(true){
			try{
				System.out.print("station : ");
				station = br.readLine();
				if(station.equals("exit"))
					break;
				//System.out.print(station);
				System.out.print("gpsx : ");
				gpsx = Double.parseDouble(br.readLine());
				System.out.print("gpsy : ");
				gpsy = Double.parseDouble(br.readLine());
			
				hashGPSx.put(station, gpsx);
				//System.out.println(hashGPSx);
				hashGPSy.put(station, gpsy);
			}
			catch(IOException e){
				e.printStackTrace();
			}
			
			
		}
	}
	
	
	public HashMap<String, Double>getHashGPSx(){
		return hashGPSx;
		
	}
	
	public HashMap<String, Double>getHashGPSy(){
		return hashGPSy;
		
	}
}
