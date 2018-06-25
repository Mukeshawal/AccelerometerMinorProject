package com.example.admin.accelerometerminorproject;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    private static String TAG ="myLog";

    private TextView x_data,y_data,z_data,commandBot;
    private SensorManager SM;
    private Sensor mySensor;

    // make bluetooth Adapter
    BluetoothAdapter myBluetoothAdapter;

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(myBluetoothAdapter.ACTION_STATE_CHANGED))
            {
                final int state = intent.getIntExtra(myBluetoothAdapter.EXTRA_STATE,myBluetoothAdapter.ERROR);

                switch (state)
                {
                    case BluetoothAdapter.STATE_OFF:
                    {
                        Log.i(TAG, "state off");
                    }
                    case BluetoothAdapter.STATE_TURNING_OFF:
                    {
                        Log.i(TAG, "state turning off");
                    }
                    case BluetoothAdapter.STATE_ON:
                    {
                        Log.i(TAG, "state on");
                    }
                    case BluetoothAdapter.STATE_TURNING_ON:
                    {
                        Log.i(TAG, "state turning on");
                    }
                    break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG,"on create");

        //initialize bluetooth on/off switch
        Button BtSwitch =  findViewById(R.id.BtSwitch);

        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

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

        //set on click listener on button
        BtSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BtOnOff();
            }
        });
    }
    public void BtOnOff ()
    {
        if (myBluetoothAdapter == null) //if bluetooth is not available in that device
        {
            Log.i(TAG, "bluetooth not available");
            Toast.makeText(MainActivity.this,"Bluetooth not available", Toast.LENGTH_LONG).show();
        }
        if (!myBluetoothAdapter.isEnabled()) // if bluetooth is not enabled
        {
            Intent enableBt = new Intent(myBluetoothAdapter.ACTION_REQUEST_ENABLE); //request to enable bluetooth
            startActivity(enableBt);
            Toast.makeText(MainActivity.this,"Bluetooth enabled", Toast.LENGTH_LONG).show();

            // create intent filter to catch state change in bluetooth
            IntentFilter BtIntent =  new IntentFilter(myBluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(myBroadcastReceiver, BtIntent);
        }
        if (myBluetoothAdapter.isEnabled())
        {
            myBluetoothAdapter.disable();
            Toast.makeText(MainActivity.this,"Bluetooth disabled", Toast.LENGTH_LONG).show();

            IntentFilter BtIntent =  new IntentFilter(myBluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(myBroadcastReceiver, BtIntent);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

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
}
