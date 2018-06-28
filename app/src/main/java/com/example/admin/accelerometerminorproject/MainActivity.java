package com.example.admin.accelerometerminorproject;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.example.admin.accelerometerminorproject.BluetoothConnection.BluetoothFuntions;

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    private static String TAG ="myLog";
    private TextView x_data,y_data,z_data,commandBot;
    private SensorManager SM;
    private Sensor mySensor;

    BluetoothFuntions bluetoothSetup = new BluetoothFuntions(this);

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG,"on create");

        //initialize bluetooth on/off switch
        Button BtSwitch =  findViewById(R.id.BtSwitch);
        SensorInitialize();
        SensorRegister();

        //assign text view
        x_data = findViewById(R.id.x_data);
        y_data = findViewById(R.id.y_data);
        z_data = findViewById(R.id.z_data);
        commandBot=findViewById(R.id.commandBot);


        //set on click listener on button
        BtSwitch.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
               bluetoothSetup.BtOnOff();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        SensorUnRegister();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SensorRegister();
    }

    @Override
    public void onSensorChanged(SensorEvent event)
     {

        x_data.setText(getString(R.string.x)+ event.values[0]);
        y_data.setText(getString(R.string.y)+ event.values[1]);
        z_data.setText(getString(R.string.z)+ event.values[2]);

        if (event.values[2]>0) //screen facing upwards
        {
            if((event.values[0]<2)&&(event.values[0]>-2)) //stop, forward and backwards
            {
                if(event.values[1]<-3)
                {commandBot.setText(R.string.forward);}
                else if (event.values[1]>3)
                {commandBot.setText(R.string.backward);}
                else
                {commandBot.setText(R.string.stop);}
            }
            else if ((event.values[1]<2)&&(event.values[1]>-2))
            {
                if(event.values[0]<-3)
                {commandBot.setText(R.string.right);}
                else if (event.values[0]>3)
                {commandBot.setText(R.string.left);}
                else
                {commandBot.setText(R.string.stop);}
            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void SensorInitialize()
    {

        //create sensor manager
        SM = (SensorManager)getSystemService(SENSOR_SERVICE);

        //accelerometer sensor
        if (SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null)
        {
            Log.i(TAG, "Accelerometer Sensor detected");
            mySensor = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        else
        {
            Log.i(TAG, "Accelerometer Sensor not detected");
        }
    }
    public void SensorRegister()
    {
        SM.registerListener(this, mySensor,SM.SENSOR_DELAY_NORMAL);
        Log.i(TAG, "sensor registered");
    }
    public void SensorUnRegister()
    {
        SM.unregisterListener(this);
        Log.i(TAG, "sensor unregistered");
    }
}
