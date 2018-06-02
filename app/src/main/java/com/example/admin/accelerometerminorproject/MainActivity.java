package com.example.admin.accelerometerminorproject;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener{


    private TextView x_data,y_data,z_data,commandBot;
    private SensorManager SM;
    private Sensor mySensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //create sensor manager
        SM = (SensorManager)getSystemService(SENSOR_SERVICE);

        //accelerometer sensor
        mySensor = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        //register sensor listener
        SM.registerListener(this, mySensor, SensorManager.SENSOR_DELAY_NORMAL);

        //assign text view
        x_data = (TextView)findViewById(R.id.x_data);
        y_data = (TextView)findViewById(R.id.y_data);
        z_data = (TextView)findViewById(R.id.z_data);
        commandBot=(TextView)findViewById(R.id.commandBot);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        x_data.setText("X : "+ event.values[0]);
        y_data.setText("Y : "+ event.values[1]);
        z_data.setText("Z : "+ event.values[2]);

        if (event.values[2]>0) //screen facing upwards
        {
            if((event.values[0]<2)&&(event.values[0]>-2)) //stop, forward and backwards
            {
                if(event.values[1]<-3)
                {commandBot.setText("move forward");}
                else if (event.values[1]>3)
                {commandBot.setText("move backward");}
                else
                {commandBot.setText("stop");}
            }
            else if ((event.values[1]<2)&&(event.values[1]>-2))
            {
                if(event.values[0]<-3)
                {commandBot.setText("Turn Right");}
                else if (event.values[0]>3)
                {commandBot.setText("Turn left");}
                else
                {commandBot.setText("stop");}
            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
