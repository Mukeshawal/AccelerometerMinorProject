package com.example.admin.accelerometerminorproject;

import android.bluetooth.BluetoothDevice;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.example.admin.accelerometerminorproject.BluetoothConnection.BluetoothFuntions;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SensorEventListener,BluetoothFuntions.NewDevice {

    private static String TAG ="MYLOG";
    private TextView x_data,y_data,z_data,commandBot;
    private SensorManager SM;
    private Sensor mySensor;
    public ListView LvNewDevices;
    public DeviceAdapter mDeviceListAdapter;
    public ArrayList<BluetoothDevice> mBtDeviceMain;

    BluetoothFuntions bluetoothSetup = new BluetoothFuntions(this, this);

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG,"on create");

        //initialize bluetooth on/off switch
        Button BtSwitch =  findViewById(R.id.BtSwitch);
        Button DiscoverBt = findViewById(R.id.DiscoverBt);
        Button makeDiscoverable = findViewById(R.id.makeDiscoverable);
        SensorInitialize();
        SensorRegister();

        //assign text view
        x_data = findViewById(R.id.x_data);
        y_data = findViewById(R.id.y_data);
        z_data = findViewById(R.id.z_data);
        commandBot=findViewById(R.id.commandBot);
        LvNewDevices=findViewById(R.id.LvNewDevices);
        mBtDeviceMain = new ArrayList<>();


        //set on click listener on button
        BtSwitch.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
               bluetoothSetup.BtOnOff();
            }
        });
        DiscoverBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothSetup.DiscoverDevices();
            }
        });
        makeDiscoverable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothSetup.makeDiscoverable();
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


    @Override
    public void onUpdateDevice(ArrayList<BluetoothDevice> mBtDevice) {
        Log.i(TAG,"enterd to onUpdateDevice");
        if(mBtDevice != null)
        {
            mBtDeviceMain = mBtDevice;
            Log.i(TAG,"array adapter copied");
        }
        if(mBtDevice == null)
        {
            Log.i(TAG,"array adapter not received");
        }
        mDeviceListAdapter = new DeviceAdapter(this,  R.layout.device_adapter_view,mBtDeviceMain);
        Log.i(TAG,"adapter set");
        LvNewDevices.setAdapter(mDeviceListAdapter);
        Log.i(TAG,"update complete");


    }
}
