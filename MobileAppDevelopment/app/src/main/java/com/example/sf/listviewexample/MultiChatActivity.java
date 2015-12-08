package com.example.sf.listviewexample;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.Socket;
import java.util.LinkedList;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class MultiChatActivity extends AppCompatActivity {
    private String streammsg;
    private TextView showText;
    private String ip;
    private String port;
    private EditText editText_message;
    private Handler msghandler;
    private GpsInfo gps;
    private String stationName;

    private SocketClient client;
    private RecieveThread recieve;
    private SendThread send;
    private Socket socket;
    private LocationManager locationManager;

    private String userID;
    private double latitude;
    private double longtitude;

    private PipedInputStream sendStream = null;
    private PipedOutputStream receiveStream = null;

    private double x;
    private double y;

    public double centroidOfUsersPositionX;
    public double centroidOfUsersPositionY;

    LinkedList<SocketClient> threadList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_chat);


        Intent intent = getIntent();
        userID = intent.getStringExtra("ID");

        gps = new GpsInfo(MultiChatActivity.this);
        latitude = gps.getLatitude();
        longtitude = gps.getLongitude();

        showText = (TextView)findViewById(R.id.showText_TextView);
        editText_message = (EditText)findViewById((R.id.sendText));
        threadList = new LinkedList<MultiChatActivity.SocketClient>();

        InputMethodManager mInputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        mInputMethodManager.hideSoftInputFromWindow(editText_message.getWindowToken(), 0);

        ip = "165.194.17.20";
        port = "5001";

        centroidOfUsersPositionX = 0;
        centroidOfUsersPositionY = 0;

        msghandler = new Handler(){
          public void handleMessage(Message hdmsg){
              if(hdmsg.what == 1111){
                  showText.append(hdmsg.obj.toString() + "\n");
                  Log.d("Text Change", hdmsg.obj.toString());
              }
          }
        };

        locationManager = (LocationManager)getSystemService(this.LOCATION_SERVICE);
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            alertCheckGPS();
        }

        client = new SocketClient(ip, port);
        Log.d("Client create", "클라이언트 생성");
        threadList.add(client);
        client.start();

    }

    public void onSendClicked(View v){
        if(editText_message.getText().toString() != null){
            send = new SendThread(socket);
            send.start();
            Log.d("Send Msg", editText_message.getText().toString());



            editText_message.setText("");
            InputMethodManager mInputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            mInputMethodManager.hideSoftInputFromWindow(editText_message.getWindowToken(), 0);
        }
    }

    public void onMapViewClicked(View v){
        Log.d("위도 경도", +centroidOfUsersPositionX + " " + centroidOfUsersPositionY);
        Intent intent = new Intent(MultiChatActivity.this, MapsActivity.class);
        intent.putExtra("latitude", centroidOfUsersPositionX);
        intent.putExtra("longtitude", centroidOfUsersPositionY);
        Toast.makeText(getApplicationContext(),stationName,Toast.LENGTH_LONG).show();
        startActivity(intent);

    }

    public void onExitClicked(View v){
        finish();
    }

    public void onMyLocationClicked(View v){
        Intent intent = new Intent(MultiChatActivity.this, MapsActivity.class);
        intent.putExtra("latitude", latitude);
        intent.putExtra("longtitude", longtitude);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_multi_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class SocketClient extends Thread{
        boolean threadAlive;
        String ip;
        String port;
        String mac;

        InputStream inputStream = null;
        OutputStream outputStream = null;
        BufferedReader bufferedReader = null;

        private DataOutputStream output = null;
        private DataInputStream input = null;

        public SocketClient(String ip, String port){
            threadAlive = true;
            this.ip = ip;
            this.port = port;
        }

        public void run(){
            try{
                //연결 후 바로 RecieveThread 시작
                socket = new Socket(ip, Integer.parseInt(port));
                inputStream = socket.getInputStream();
                output = new DataOutputStream(socket.getOutputStream());
                recieve = new RecieveThread(socket);
                recieve.start();

                //Mac주소 받아오는 설정
                WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                mac = wifiInfo.getMacAddress();

                //Mac 전송
                output.writeUTF(mac);
                output.writeDouble(latitude);
                output.writeDouble(longtitude);
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    class RecieveThread extends Thread{
        private Socket socket = null;
        DataInputStream input;
        String location = "";
        String[] trash;

       // private double centroidOfUsersPoisitionX;
        //private double centroidOfUsersPositionY;

        public RecieveThread(Socket socket){
            this.socket = socket;
            //centroidOfUsersPositionX = 0;
            //centroidOfUsersPositionY = 0;
            try{
                input = new DataInputStream(socket.getInputStream());
                //centroidOfUsersPoisitionX = input.readDouble();
               // centroidOfUsersPositionY = input.readDouble();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

       /*public double getPositionX(){

            return centroidOfUsersPoisitionX;
        }

        public double getPositionY(){
            return centroidOfUsersPositionY;
        }*/

        public void run(){
            try{
                //centroidOfUsersPositionX = input.readDouble();
               // centroidOfUsersPositionY = input.readDouble();
                while(input != null){
                    String msg = input.readUTF();

                    if(msg != null){
                        Log.d(ACTIVITY_SERVICE, "test");
                        location = msg;
                        if(location.contains("Location")){
                            trash = location.split(" ");
                            centroidOfUsersPositionX = Double.parseDouble(trash[1]);
                            centroidOfUsersPositionY = Double.parseDouble(trash[2]);
                        }
                        else if(location.contains("NearStation")){
                            trash = location.split(" ");
                            stationName = trash[1];
                            Message hdmsg = msghandler.obtainMessage();
                            hdmsg.what = 1111;
                            hdmsg.obj = msg;
                            msghandler.sendMessage(hdmsg);
                            Log.d(ACTIVITY_SERVICE, hdmsg.obj.toString());
                        }
                        else{
                            Message hdmsg = msghandler.obtainMessage();
                            hdmsg.what = 1111;
                            hdmsg.obj = msg;
                            msghandler.sendMessage(hdmsg);
                            Log.d(ACTIVITY_SERVICE, hdmsg.obj.toString());
                        }
                    }
                }
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
        public boolean isStringDouble(String s){
           try{
               Double.parseDouble(s);
               return true;
           }catch(NumberFormatException e){
               return false;
           }
        }
    }

    class SendThread extends  Thread{
        private Socket socket;
        String sendMsg = editText_message.getText().toString();
        DataOutputStream output;

        public SendThread(Socket socket){
            this.socket = socket;
            try{
                output = new DataOutputStream(socket.getOutputStream());
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        public void run(){
            try{
                //누구 메세지인지 식별하기 위해 mac사용
                Log.d(ACTIVITY_SERVICE, "1111");
                String mac = null;
                WifiManager mng = (WifiManager)getSystemService(WIFI_SERVICE);
                WifiInfo wifiInfo = mng.getConnectionInfo();
                mac = wifiInfo.getMacAddress();

                if(output != null){
                    if(sendMsg != null){
                        output.writeUTF(mac + " "  + sendMsg);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }catch (NullPointerException npe){
                npe.printStackTrace();
            }
        }
    }

    public boolean isStringDouble(String s){
        try{
            Double.parseDouble(s);
            return true;
        }catch(NumberFormatException e){
            return false;
        }
    }

    private void alertCheckGPS(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("GPS를 사용하시겠습니까?").setCancelable(false)
                .setPositiveButton("GPS사용", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }
                }).setNegativeButton("사용안함", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
