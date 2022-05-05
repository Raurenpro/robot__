package com.example.robot_app;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.ubtechinc.cruzr.sdk.ros.RosRobotApi;
import com.ubtechinc.cruzr.sdk.speech.SpeechRobotApi;
import com.ubtechinc.cruzr.serverlibutil.interfaces.InitListener;
import com.ubtechinc.cruzr.serverlibutil.interfaces.SpeechTtsListener;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MainActivity extends AppCompatActivity {


    private Button btn;
    private static final String TAG = "MyTag";
    private String topic, clientID;
    private MqttAndroidClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init_Robot();
        init();
    }

    private void init() {
        btn = findViewById(R.id.btn_sub);
        clientID = "xxx";
        topic = "testtopic/rauren";
        client =
                new MqttAndroidClient(this.getApplicationContext(), "tcp://broker.hivemq.com:1883",
                        clientID);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectX();
            }
        });
    }

    private void connectX() {
        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();
                    sub();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Toast.makeText(MainActivity.this, "NoConnected", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void sub() {
        try {
            client.subscribe(topic, 0);
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {

                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    Toast.makeText(MainActivity.this, new String(message.getPayload()), Toast.LENGTH_SHORT).show();

                    if (new String(message.getPayload()).equals("led")) {
                        ledSetOnOff(true);
                    }
                    else if (new String(message.getPayload()).equals("slt")) {
                        runSlt("pose1");
                    }
                    else if (new String(message.getPayload()).equals("head")) {
                        moveHead(40);
                    }
                    else if (new String(message.getPayload()).equals("text")) {
                        tts("Hello everyone i'm here");
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {

                }
            });
        }catch (MqttException e) {

        }
    }

    public void init_Robot() {
        RosRobotApi.get().initializ(this, new InitListener() {
            @Override
            public void onInit() {
                //Initialization successful
            }
        });

        SpeechRobotApi.get().initializ(this, 103, new InitListener() {
            @Override
            public void onInit() {
                //Initialization successful
            }
        });
    }

    public int tts(String text) {
        return SpeechRobotApi.get().speechStartTTS(text, new SpeechTtsListener());
    }

    public int ledSetOnOff(boolean onOff) {
        return RosRobotApi.get().ledSetOnOff(onOff);
    }

    public int runSlt(String run) {
        return RosRobotApi.get().run(run);
    }

    // Head control
    public int moveHead(double angle) {
        double PER_SECOND_ANGLE = 30 * Math.PI / 180;
        double duration = Math.abs(angle) / PER_SECOND_ANGLE;
        if (duration < 1) {
            duration = 1;
        }
        return RosRobotApi.get().setAngles( "HeadPitch", (float) angle, (float) duration);
    }



}