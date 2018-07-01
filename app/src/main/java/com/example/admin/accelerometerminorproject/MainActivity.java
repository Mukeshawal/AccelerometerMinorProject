package com.example.admin.accelerometerminorproject;

import android.bluetooth.BluetoothDevice;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.example.admin.accelerometerminorproject.BluetoothConnection.BluetoothFuntions;
import com.example.admin.accelerometerminorproject.SensorFunctions.SensorFunctions;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements BluetoothFuntions.BluetoothCallBack,
        SensorFunctions.SensorCallBack {

    private static String TAG ="MYLOG";
    float xMain,yMain,zMain;
    String commandMain;

    private TextView x_data,y_data,z_data,commandBot;
    public ListView LvNewDevices;
    public ListView LvPairedDevices;

    public DeviceAdapter mDeviceListAdapter;
    public DeviceAdapter mPairedDeviceListAdapter;
    public ArrayList<BluetoothDevice> mBtDeviceMain;
    public ArrayList<BluetoothDevice> mPairedBtDeviceMain;

    //create objects of class SensorFuntions and BluetoothFunctions
    SensorFunctions sensorFunctions = new SensorFunctions(this, this);
    BluetoothFuntions bluetoothSetup = new BluetoothFuntions(this, this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG,"on create");

        //initialize bluetooth function switches
        Button BtSwitch =  findViewById(R.id.BtSwitch);
        Button DiscoverBt = findViewById(R.id.DiscoverBt);
        Button makeDiscoverable = findViewById(R.id.makeDiscoverable);

        //Initialize sensor
        sensorFunctions.SensorInitialize();

        //assign text view
        x_data = findViewById(R.id.x_data);
        y_data = findViewById(R.id.y_data);
        z_data = findViewById(R.id.z_data);
        commandBot=findViewById(R.id.commandBot);
        LvNewDevices=findViewById(R.id.LvNewDevices);
        LvPairedDevices = findViewById(R.id.LvPairedDevices);

        //assign ArrayList
        mBtDeviceMain = new ArrayList<>();

        //set on click listener on button
        BtSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               bluetoothSetup.BtOnOff();
            }
        });
        DiscoverBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mDeviceListAdapter != null)
                {mDeviceListAdapter.clear();}
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
        sensorFunctions.SensorUnRegister();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorFunctions.SensorRegister();
    }


    //override method of interface BluetoothCallBack to update new discovered devices
    @Override
    public void updateDeviceList(ArrayList<BluetoothDevice> mBtDevice) {
        Log.i(TAG,"enterd to onUpdateDevice");
        if((mBtDevice != null))
        {   //copy received mBtDevice ArrayList<> type to mBtDeviceMain
            mBtDeviceMain = mBtDevice;
            Log.i(TAG,"array adapter copied");
        }
        if(mBtDevice == null)
        {
            Log.i(TAG,"array adapter not received");
        }
        //Pass data to adapter with context,Layout,ArrayList<BluetoothDevice>
        mDeviceListAdapter = new DeviceAdapter(this,  R.layout.device_adapter_view,mBtDeviceMain);
        Log.i(TAG,"adapter set");

        //Set Adapter
        LvNewDevices.setAdapter(mDeviceListAdapter);
        Log.i(TAG,"update complete");
    }

    @Override
    public void pairedDeviceList(ArrayList<BluetoothDevice> mPairedBtDevice) {
        if((mPairedBtDevice != null))
        {   //copy received mBtDevice ArrayList<> type to mBtDeviceMain
            mPairedBtDeviceMain = mPairedBtDevice;
            Log.i(TAG,"paired array adapter copied");
        }
        if(mPairedBtDevice == null)
        {
            Log.i(TAG,"paired array adapter not received");
        }
        //Pass data to adapter with context,Layout,ArrayList<BluetoothDevice>
        mPairedDeviceListAdapter = new DeviceAdapter(this,  R.layout.device_adapter_view,mPairedBtDeviceMain);
        Log.i(TAG,"paired adapter set");

        //Set Adapter
        LvPairedDevices.setAdapter(mPairedDeviceListAdapter);
        Log.i(TAG,"paired update complete");
    }

    @Override
    public void clearList() {
        if(mDeviceListAdapter != null)
        {mDeviceListAdapter.clear();}
        if(mPairedDeviceListAdapter != null)
        {mPairedDeviceListAdapter.clear();}
    }

    //override method of interface SensorCallBack to update new Sensor readings
    @Override
    public void updateSensorData(float x, float y, float z, String command) {
        //copy received sensor readings to float type variables and orientation in string
        xMain = x;        yMain = y;        zMain = z;        commandMain = command;

        //update textFields on screen
        x_data.setText(getString(R.string.x)+ xMain);
        y_data.setText(getString(R.string.y)+ yMain);
        z_data.setText(getString(R.string.z)+ zMain);
        commandBot.setText(commandMain);
    }
}
