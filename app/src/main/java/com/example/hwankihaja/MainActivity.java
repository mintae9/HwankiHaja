package com.example.hwankihaja;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
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
    Switch onoff;
    TextView O_temp, O_humid, O_dust, O_gas1, O_gas2;
    TextView I_temp, I_humid, I_dust, I_gas1, I_gas2;


    private Socket mSocket = null;
    private PrintWriter mOut;
    private BufferedReader mIn;
    private Thread mReceiverThread = null;

    private static final String TAG = "TcpClient";
    private boolean isConnected = false;

    float o_temp=0, o_humid=1, o_dust=0, o_gas1=1, o_gas2=1;
    float i_temp=1, i_humid=0, i_dust=1, i_gas1=0, i_gas2=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtConnect = findViewById(R.id.txtConnect);
        onoff = findViewById(R.id.onoff);
        btnOn = findViewById(R.id.btnOn);
        btnOff = findViewById(R.id.btnOff);

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
        }else if(o_dust<i_dust){
            I_dust.setTextColor(Color.parseColor("#FF6347"));
        }
        if(o_gas1>i_gas1){
            O_gas1.setTextColor(Color.parseColor("#FF6347"));
        }else if(o_gas1<i_gas1){
            I_gas1.setTextColor(Color.parseColor("#FF6347"));
        }
        if(o_gas2>i_gas2){
            O_gas2.setTextColor(Color.parseColor("#FF6347"));
        }else if(o_gas2<i_gas2){
            I_gas2.setTextColor(Color.parseColor("#FF6347"));
        }

        onoff.setOnCheckedChangeListener(new switchListener());

//                String sendMessage = inputText.getText().toString();
//                if (sendMessage.length() > 0) {
//                    new Thread(new SenderThread(sendMessage)).start();

        if(Record.getInstance().getStatus()=="1"){
            btnOn.setVisibility(View.VISIBLE);
            btnOff.setVisibility(View.VISIBLE);
            onoff.setChecked(true);
            Log.d(TAG,"status :"+Record.getInstance().getStatus());
        }else{
            btnOn.setVisibility(View.INVISIBLE);
            btnOff.setVisibility(View.INVISIBLE);
            onoff.setChecked(false);
            Log.d(TAG,"status :"+Record.getInstance().getStatus());
        }
        btnOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new SenderThread("01")).start();
                Record.getInstance().setStatus("01");
            }
        });
        btnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new SenderThread("00")).start();
                Record.getInstance().setStatus("00");
            }
        });

        new Thread(new ConnectThread("192.168.0.7", 10004)).start();

        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse("{\"o_temp\":28.5, \"o_humid\":30.1}");
        Log.d(TAG, "json test :"+element.getAsJsonObject().get("o_temp").getAsFloat()+","+element.getAsJsonObject().get("o_humid").getAsFloat());
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

            runOnUiThread(new Runnable(){
                @Override
                public void run() {
                    if (isConnected) {
                        Log.d(TAG, "connected to "+serverIP);

                        txtConnect.setText(serverIP);
                        mReceiverThread = new Thread(new ReceiverThread());
                        mReceiverThread.start();
                    }else{
                        Log.d(TAG, "failed to connect to server "+serverIP);
                        txtConnect.setText("failed: " +serverIP);
                    }
                }
            });
        }
    }

    private class switchListener implements CompoundButton.OnCheckedChangeListener{
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(isChecked){
                Log.d(TAG, "checked");
                //new Thread(new SenderThread("1")).start();
                Record.getInstance().setStatus("1");
                btnOn.setVisibility(View.VISIBLE);
                btnOff.setVisibility(View.VISIBLE);
            }else{
                Log.d(TAG, "not checked");
                //new Thread(new SenderThread("0")).start();
                Record.getInstance().setStatus("00");
                btnOn.setVisibility(View.INVISIBLE);
                btnOff.setVisibility(View.INVISIBLE);
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
                while(isConnected){
                    if(mIn == null){
                        Log.d(TAG, "ReceiverThread: mIn is nulll");
                        break;
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


