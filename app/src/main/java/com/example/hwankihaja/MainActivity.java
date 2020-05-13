package com.example.hwankihaja;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewParent;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class MainActivity extends Activity {

    TextView txtConnect;
    Button btnOn;
    Button btnOff;
    Button btnWeak;
    Switch onoff;
    TextView O_temp, O_humid, O_dust, O_gas1, O_gas2;
    TextView I_temp, I_humid, I_dust, I_gas1, I_gas2;
    SwipeRefreshLayout swipe;

    private static int REQUEST=0;
    private static int AUTO=1;
    private static int MANUAL_WEAK=2;
    private static int MANUAL_STRONG=3;
    private static int MANUAL_STOP=4;


    private Socket mSocket = null;
    private PrintWriter mOut;
    private BufferedReader mIn;
    private Thread mReceiverThread = null;

    private static final String TAG = "TcpClient";
    private boolean isConnected = false;

    float o_temp=(float)26.42, o_humid=(float)47.00, o_dust=(float)1.37, o_gas1=(float)1.37, o_gas2=(float)90;
    float i_temp=(float)26.64, i_humid=(float)57.00, i_dust=(float)4.24, i_gas1=(float)1.37, i_gas2=(float)92;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtConnect = findViewById(R.id.txtConnect);
        onoff = findViewById(R.id.onoff);
        btnOn = findViewById(R.id.btnOn);
        btnOff = findViewById(R.id.btnOff);
        btnWeak = findViewById(R.id.btnWeak);
        swipe = (SwipeRefreshLayout) findViewById(R.id.swipe);

        O_temp = findViewById(R.id.o_temp);
        O_humid = findViewById(R.id.o_humid);
        O_dust = findViewById(R.id.o_dust);
        O_gas1 = findViewById(R.id.o_gas1);
        O_gas2 = findViewById(R.id.o_gas2);
        I_temp = findViewById(R.id.i_temp);
        I_humid = findViewById(R.id.i_humid);
        I_dust = findViewById(R.id.i_dust);
        I_gas1 = findViewById(R.id.i_gas1);
        I_gas2 = findViewById(R.id.i_gas2);

        final ColorStateList oldColors =  I_temp.getTextColors();

//        O_temp.setText(String.valueOf(o_temp)+"°C");
//        O_humid.setText(String.valueOf(o_humid+"%"));
//        O_dust.setText(String.valueOf(o_dust+"ug/m3"));
//        O_gas1.setText(String.valueOf(o_gas1+"ppm"));
//        O_gas2.setText(String.valueOf(o_gas2));
//        I_temp.setText(String.valueOf(i_temp)+"°C");
//        I_humid.setText(String.valueOf(i_humid+"%"));
//        I_dust.setText(String.valueOf(i_dust+"ug/m3"));
//        I_gas1.setText(String.valueOf(i_gas1+"ppm"));
//        I_gas2.setText(String.valueOf(i_gas2));

        new Thread(new ConnectThread("124.50.95.231", 10004)).start();
        new Thread(new SenderThread("{\"id\":1,\"cmd\":"+REQUEST+"}")).start();
        new Thread(new ReceiverThread()).start();

//        if(o_temp>i_temp){
//            O_temp.setTextColor(Color.parseColor("#FF6347"));
//        }else if(o_temp<i_temp){
//            I_temp.setTextColor(Color.parseColor("#FF6347"));
//            Log.d(TAG, O_temp.getTextColors().toString());
//        }
//        if(o_humid>i_humid){
//            O_humid.setTextColor(Color.parseColor("#FF6347"));
//        }else if(o_humid<i_humid){
//            I_humid.setTextColor(Color.parseColor("#FF6347"));
//        }
//        if(o_dust>i_dust){
//            O_dust.setTextColor(Color.parseColor("#FF6347"));
//        }else if(o_dust<i_dust){
//            I_dust.setTextColor(Color.parseColor("#FF6347"));
//        }
//        if(o_gas1>i_gas1){
//            O_gas1.setTextColor(Color.parseColor("#FF6347"));
//        }else if(o_gas1<i_gas1){
//            I_gas1.setTextColor(Color.parseColor("#FF6347"));
//        }
//        if(o_gas2>i_gas2){
//            O_gas2.setTextColor(Color.parseColor("#FF6347"));
//        }else if(o_gas2<i_gas2){
//            I_gas2.setTextColor(Color.parseColor("#FF6347"));
//        }

        if(Record.getInstance().getStatus()==0){
            txtConnect.setText("현재 부엌 \"환기 중지\" 상태입니다.");
        }else if(Record.getInstance().getStatus()==1) {
            txtConnect.setText("현재 부엌 \"약식 환기\" 상태입니다.");
        }else if(Record.getInstance().getStatus()==2) {
            txtConnect.setText("현재 부엌 \"환기\" 상태입니다.");
        }

        if(Record.getInstance().getAuto()==0){
            onoff.setChecked(false);
        }else{
            onoff.setChecked(true);
        }

        onoff.setOnCheckedChangeListener(new switchListener());
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
//                o_humid=(float)58.00;
//                i_dust=(float)3.25;
//                i_gas2 =(float)90;
//                O_humid.setText(String.valueOf(o_humid+"%"));
//                I_dust.setText(String.valueOf(i_dust+"ug/m3"));
//                I_gas2.setText(String.valueOf(i_gas2));
//
//                O_humid.setTextColor(Color.parseColor("#FF6347"));
//                I_humid.setTextColor(oldColors);
//                I_gas2.setTextColor(oldColors);

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                swipe.setRefreshing(false);
            }
        });

        if(Record.getInstance().getAuto()==1){
            btnOn.setVisibility(View.INVISIBLE);
            btnOff.setVisibility(View.INVISIBLE);
            btnWeak.setVisibility(View.INVISIBLE);
            onoff.setChecked(true);
            Log.d(TAG,"status :"+Record.getInstance().getStatus());
        }else{
            btnOn.setVisibility(View.VISIBLE);
            btnOff.setVisibility(View.VISIBLE);
            btnWeak.setVisibility(View.VISIBLE);
            onoff.setChecked(false);
            Log.d(TAG,"status :"+Record.getInstance().getStatus());
        }
        btnOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new SenderThread("{\"id\":1,\"cmd\":"+MANUAL_STRONG+"}")).start();
                Record.getInstance().setStatus(2);
                txtConnect.setText("현재 부엌 \"환기\" 상태입니다.");
                Toast.makeText(MainActivity.this, "환기로 전환합니다", Toast.LENGTH_SHORT).show();
            }
        });
        btnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new SenderThread("{\"id\":1,\"cmd\":"+MANUAL_STOP+"}")).start();
                Record.getInstance().setStatus(0);
                txtConnect.setText("현재 부엌 \"환기 중지\" 상태입니다.");
                Toast.makeText(MainActivity.this, "환기 중단합니다", Toast.LENGTH_SHORT).show();
            }
        });
        btnWeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new SenderThread("{\"id\":1,\"cmd\":"+MANUAL_WEAK+"}")).start();
                Record.getInstance().setStatus(1);
                txtConnect.setText("현재 부엌 \"약식 환기\" 상태입니다.");
                Toast.makeText(MainActivity.this, "약식 환기로 전환합니다", Toast.LENGTH_SHORT).show();
            }
        });

        if(o_temp >= 20 && o_humid>40 && o_humid<50) {
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(MainActivity.this)
                    .setSmallIcon(R.mipmap.ic_launcher_new_round)
                    .setContentTitle("빨래하자")
                    .setContentText("지금 빨래 말리기 딱 좋아요:)")
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_new_round))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true);

            NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            mNotificationManager.notify(0, mBuilder.build());
        }
    }

    private class ConnectThread implements Runnable{
        private String serverIP;
        private int serverPort;

        ConnectThread(String ip, int port){
            serverIP = ip;
            serverPort = port;
        }

        @Override
        public void run() {
            try{
                mSocket = new Socket(serverIP, serverPort);
                Log.d(TAG, "err: can't find host");
            }catch(SocketTimeoutException e){
                Log.d(TAG, "err: timeout");
            }catch(Exception e){
                Log.e(TAG, ("err:" +e.getMessage()));
            }

            if(mSocket !=null) {
                try{
                    mOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream(), "UTF-8")), true);
                    mIn = new BufferedReader(new InputStreamReader(mSocket.getInputStream(), "UTF-8"));
                    isConnected = true;
                }catch(IOException e){
                    Log.e(TAG, ("err:"+e.getMessage()));
                }
            }
        }
    }

    private class switchListener implements CompoundButton.OnCheckedChangeListener{
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(isChecked){
                Log.d(TAG, "checked");
                new Thread(new SenderThread("{\"id\":1,\"cmd\":"+AUTO+"}")).start();
                Record.getInstance().setAuto(1);
                btnOn.setVisibility(View.INVISIBLE);
                btnOff.setVisibility(View.INVISIBLE);
                btnWeak.setVisibility(View.INVISIBLE);
                Toast.makeText(MainActivity.this, "자동 환기로 전환합니다.", Toast.LENGTH_SHORT).show();
            }else{
                Log.d(TAG, "not checked");
                new Thread(new SenderThread("{\"id\":1,\"cmd\":"+MANUAL_STRONG+"}")).start();
                Record.getInstance().setStatus(0);
                btnOn.setVisibility(View.VISIBLE);
                btnOff.setVisibility(View.VISIBLE);
                btnWeak.setVisibility(View.VISIBLE);
                Toast.makeText(MainActivity.this, "수동 환기로 전환합니다", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class SenderThread implements Runnable{

        private String msg;

        SenderThread(String msg){
            this.msg = msg;
        }

        @Override
        public void run() {
            mOut.println(this.msg);
            mOut.flush();

            runOnUiThread(new Runnable(){
                @Override
                public void run() {
                    Log.d(TAG, "send message: "+msg);
                }
            });
        }
    }

    private class ReceiverThread implements Runnable{
        @Override
        public void run() {
            try{
                    if(mIn == null){
                        Log.d(TAG, "ReceiverThread: mIn is nulll");
                    }
                    final String recvMessage = mIn.readLine();
                    if(recvMessage != null){
                        runOnUiThread(new Runnable(){
                            @Override
                            public void run() {
                                Log.d(TAG, "recv message: "+recvMessage);
                                JsonParser parser = new JsonParser();
                                JsonElement element = parser.parse(recvMessage);
                                o_temp = element.getAsJsonObject().get("o_temp").getAsFloat();
                                o_humid = element.getAsJsonObject().get("o_humid").getAsFloat();
                                o_dust = element.getAsJsonObject().get("o_dust").getAsFloat();
                                o_gas1 = element.getAsJsonObject().get("o_gas1").getAsFloat();
                                o_gas2 = element.getAsJsonObject().get("o_gas2").getAsFloat();

                                i_temp = element.getAsJsonObject().get("i_temp").getAsFloat();
                                i_humid = element.getAsJsonObject().get("i_humid").getAsFloat();
                                i_dust = element.getAsJsonObject().get("i_dust").getAsFloat();
                                i_gas1 = element.getAsJsonObject().get("i_gas1").getAsFloat();
                                i_gas2 = element.getAsJsonObject().get("i_gas2").getAsFloat();

                                O_temp.setText(String.valueOf(o_temp));
                                O_humid.setText(String.valueOf(o_humid));
                                O_dust.setText(String.valueOf(o_dust));
                                O_gas1.setText(String.valueOf(o_gas1));
                                O_gas2.setText(String.valueOf(o_gas2));
                                I_temp.setText(String.valueOf(i_temp));
                                I_humid.setText(String.valueOf(i_humid));
                                I_dust.setText(String.valueOf(i_dust));
                                I_gas1.setText(String.valueOf(i_gas1));
                                I_gas2.setText(String.valueOf(i_gas2));

                                if(o_temp>i_temp){
                                    O_temp.setTextColor(Color.parseColor("#FF6347"));
                                }else if(o_temp<i_temp){
                                    I_temp.setTextColor(Color.parseColor("#FF6347"));
                                }
                                if(o_humid>i_humid){
                                    O_humid.setTextColor(Color.parseColor("#FF6347"));
                                }else if(o_humid<i_humid){
                                    I_humid.setTextColor(Color.parseColor("#FF6347"));
                                }
                                if(o_dust>i_dust){
                                    O_dust.setTextColor(Color.parseColor("#FF6347"));
                                }else if(o_temp<i_temp){
                                    I_dust.setTextColor(Color.parseColor("#FF6347"));
                                }
                                if(o_gas1>i_gas1){
                                    O_temp.setTextColor(Color.parseColor("#FF6347"));
                                }else if(o_gas1<i_gas1){
                                    I_temp.setTextColor(Color.parseColor("#FF6347"));
                                }
                                if(o_gas2>i_gas2){
                                    O_temp.setTextColor(Color.parseColor("#FF6347"));
                                }else if(o_gas2<i_gas2){
                                    I_temp.setTextColor(Color.parseColor("#FF6347"));
                                }
                            }
                        });
                    }
                Log.d(TAG, "ReceiverThread: thread has exited");
                if(mOut != null){
                    mOut.flush();
                    mOut.close();
                }
                mIn = null;
                mOut = null;

                if(mSocket != null){
                    try{
                        mSocket.close();
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                }
            }catch(IOException e){
                Log.e(TAG, "receiverThread: "+e);
            }
        }
    }


}


