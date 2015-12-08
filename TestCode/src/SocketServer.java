import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;


 
public class SocketServer {
 
    HashMap<String, DataOutputStream> clients;
    HashMap<String, Double> gpsLatitude;
    HashMap<String, Double> gpsLongtitude;
    
    
    private ServerSocket ServerSocket = null;
    private File station;
    private StationFileInput stationFileInput;
    private ShortestDistance shortestDistance;
    private double centroidOfPolygonX;
    private double centroidOfPolygonY;
    private String nearStation;
    private TestHashMap testHashMap;
    
    private HashMap<String, Double> testGPSx;
    private HashMap<String, Double> testGPSy;
    
 
    public static void main(String[] args) {
        new SocketServer().start();
    }
 
    public SocketServer() {
 
        // ����� hashmap ������(Key, value) ����
        clients = new HashMap<String, DataOutputStream>();
        gpsLatitude = new HashMap<String, Double>();
        gpsLongtitude = new HashMap<String, Double>();
        String path = SocketServer.class.getResource("").getPath();
        station = new File(path + "���ڵ������ö����ġ��ȸ.xls");
        //System.out.println(path);
        stationFileInput = new StationFileInput(station);
        shortestDistance = new ShortestDistance(station);
        testHashMap = new TestHashMap(station);
        testGPSx = testHashMap.getHashGPSx();
        testGPSy = testHashMap.getHashGPSy();
        
        
        
        // clients ����ȭ
        Collections.synchronizedMap(clients);
    }
 
    private void start() {
        
        // Port ���� ���Ǹ����� 5001�� ���� (Random������ ���氡��)
        int port = 5001;
        Socket socket = null;
 
        try {
            // �������� ������ while������ �����Ͽ� accept(���)�ϰ� ���ӽ� ip�ּҸ� ȹ���ϰ� ����ѵ�
            // MultiThread�� �����Ѵ�.
            ServerSocket = new ServerSocket(port);
            System.out.println("���Ӵ����");
            while (true) {
                socket = ServerSocket.accept();
                InetAddress ip = socket.getInetAddress();
                System.out.println(ip + "  connected");
                new MultiThread(socket).start();
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }
 
    class MultiThread extends Thread {
 
        Socket socket = null;
        	
        String mac = null;
        String msg = null;
 
        DataInputStream input;
        DataOutputStream output;
        
        double latitude = 0;
        double longtitude = 0;
        
        
 
        public MultiThread(Socket socket) {
            this.socket = socket;
            try {
                // ��ü�� �ְ���� Stream�����ڸ� �����Ѵ�.
                input = new DataInputStream(socket.getInputStream());
                output = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
            }
        }
 
        public void run() {
 
            try {
                // ���ӵ��� �ٷ� Mac �ּҸ� �޾ƿ� ����ϰ� clients�� ������ �Ѱ��ְ� Ŭ���̾�Ʈ���� mac�ּҸ�������.
                mac = input.readUTF();
                latitude = input.readDouble();
                longtitude = input.readDouble();
                System.out.println("Mac address : " + mac + " Latitude : " + latitude + " Longtitude : " + longtitude);
                clients.put(mac, output);
                gpsLatitude.put(mac, latitude);
                gpsLongtitude.put(mac, longtitude);
                sendMsg(mac + "   ����");
                gpsLatitude.putAll(testGPSx);
                gpsLongtitude.putAll(testGPSy);
                System.out.println(gpsLatitude);
                System.out.println(gpsLongtitude);
                
                Calculate(gpsLatitude, gpsLongtitude);
                sendMsg("Location " + centroidOfPolygonX + " " + centroidOfPolygonY);
                nearStation = shortestDistance.DistanceCalculate(centroidOfPolygonX, centroidOfPolygonY);
                sendMsg("NearStation" + " " + nearStation);
                
                System.out.println(centroidOfPolygonX + " " + centroidOfPolygonY);
                System.out.println(nearStation);
 
                // ���Ŀ� ä�ø޼������Ž�
                while (input != null) {
                    try {
                        String temp = input.readUTF();
                        sendMsg(temp);
                        System.out.println(temp);
                    } catch (IOException e) {
                        sendMsg("No massege");
                        break;
                    }
                }
            } catch (IOException e) {
                System.out.println(e);
            }finally{
            	
            }
        }
 
        // �޼��������� Ŭ���̾�Ʈ���� Return �� sendMsg �޼ҵ�
        private void sendMsg(String msg) {
 
            // clients�� Key���� �޾Ƽ� String �迭�μ���
            Iterator<String> it = clients.keySet().iterator();
 
            // Return �� key���� ����������
            while (it.hasNext()) {
                try {
                    OutputStream dos = clients.get(it.next());
                    // System.out.println(msg);
                    DataOutputStream output = new DataOutputStream(dos);
                    output.writeUTF(msg);
 
                } catch (IOException e) {
                    System.out.println(e);
                }
            }
        }
        
        public void sendToAllGPS(double GPS){
        	Iterator<String> it = clients.keySet().iterator();
        	 
            while (it.hasNext()) {
                try {
                	OutputStream dos = clients.get(it.next());
                    DataOutputStream output = clients.get(dos);
                    output.writeDouble(GPS);
                } catch (Exception e) {
                }
            }
        }
    }
    public void Calculate(HashMap<String, Double> x, HashMap<String, Double> y){    	
    	Object[] X = new Object[x.size()];
    	Object[] Y = new Object[y.size()];
    	double centerX = 0;
    	double centerY = 0;
    	double factor = 0;
    	double area = 0;
    	int xidx = 0;
    	int yidx = 0;
    	int firstpoint = 0;
    	int secondpoint = 0;
    	
    	if(x.size() != 0){
    		for(Iterator iterator = x.keySet().iterator();iterator.hasNext();){
        		String keyNamex = (String)iterator.next();
        		//System.out.println(keyNamex);
        		X[xidx] = (double)x.get(keyNamex);
        		System.out.println(X[xidx]);
        		xidx++;
        	}
        	
        	
        	for(Iterator iterator = y.keySet().iterator();iterator.hasNext();){
        		String keyNamey = (String)iterator.next();
        		Y[yidx] = (double)y.get(keyNamey);
        		System.out.println(Y[yidx]);
        		yidx++;
        	}
    	} 	
       	else
    		System.out.println("There is nothing HashMap");
		
    	if(X.length > 2){
    		for(int i = 0; i < x.size(); i++){
    			firstpoint = i;
    			secondpoint = (firstpoint+1)%x.size();
    			
    			factor = ((double)X[firstpoint]*(double)Y[secondpoint] - (double)X[secondpoint]*(double)Y[firstpoint]);
    			area += factor;
    			
    			centerX += ((double)X[firstpoint]+(double)X[secondpoint])*factor;
    			centerY += ((double)Y[firstpoint]+(double)Y[secondpoint])*factor;
    		}
    		
    		area /= 2.0;
    		area *= 6.0;
    		
    		factor = 1/area;
    		
    		centerX *= factor;
    		centerY *= factor;
    		
    		this.centroidOfPolygonX = centerX;
    		this.centroidOfPolygonY = centerY;
    	}
    	else if(X.length == 1){
    		this.centroidOfPolygonX = (double)X[0];
    		this.centroidOfPolygonY = (double)Y[0];
    	}
    	else if(X.length ==2){
    		this.centroidOfPolygonX = 0.5*((double)X[0] + (double)X[1]);
    		this.centroidOfPolygonY = 0.5*((double)Y[0] + (double)Y[1]);
    	}/*else{
    		this.centroidOfPolygonX = ((double)X[0] + (double)X[1] + (double)X[2])/3;
        	this.centroidOfPolygonY = ((double)Y[0] + (double)Y[1] + (double)Y[2])/3;
    	}*/
    		
    }
    public void SumHash(HashMap<String, Double>map1, HashMap<String, Double>map2){
    	 
    }
    
}
