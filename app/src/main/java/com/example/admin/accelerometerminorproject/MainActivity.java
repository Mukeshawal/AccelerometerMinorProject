package com.example.admin.accelerometerminorproject;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.admin.accelerometerminorproject.BluetoothConnection.BluetoothFuntions;
import com.example.admin.accelerometerminorproject.SensorFunctions.SensorFunctions;

import java.util.ArrayList;
import java.lang.String;


public class MainActivity extends AppCompatActivity implements BluetoothFuntions.BluetoothCallBack,
        SensorFunctions.SensorCallBack {

    private static String TAG ="MYLOG";
    float xMain,yMain,zMain;
    public int speed = 1, speedToSend, commandNumber, commandNumberDirection = 5;
    boolean btStatus;
    int REQUEST_CODE = 1;
    String commandMain, toSend = "s";

    private TextView x_data,y_data,z_data,commandBot;
    public ListView LvNewDevices;
    public ListView LvPairedDevices;
    public Switch BtSwitch;
    public SeekBar seekBar;

    public DeviceAdapter mDeviceListAdapter;
    public ArrayList<BluetoothDevice> mBtDeviceMain;

    //create objects of class SensorFuntions and BluetoothFunctions
    SensorFunctions sensorFunctions = new SensorFunctions(this, this);
    BluetoothFuntions bluetoothSetup = new BluetoothFuntions(this, this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG,"on create");

        //initialize bluetooth function switches
        BtSwitch =  findViewById(R.id.BtSwitch);

        //Initialize sensor
        sensorFunctions.SensorInitialize();

        //assign text view
        x_data = findViewById(R.id.x_data);
        y_data = findViewById(R.id.y_data);
        z_data = findViewById(R.id.z_data);
        commandBot=findViewById(R.id.commandBot);
        LvNewDevices=findViewById(R.id.LvNewDevices);

        //assign seek bar
        seekBar = findViewById(R.id.mYseekBar);

        //assign ArrayList
        mBtDeviceMain = new ArrayList<>();

        //on change in state of bluetooth switch
       BtSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
           @Override
           public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
               Log.i(TAG,"checked changed listener triggered");
               if (isChecked)
                   {
                       bluetoothSetup.BtOn();
                       clearList();
                       bluetoothSetup.DiscoverDevices();
                   }
               else
                   {
                       bluetoothSetup.Btoff();
                       clearList();
                   }

           }
       });

        LvNewDevices.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                bluetoothSetup.cancelDiscovery();
                bluetoothSetup.pairDevice(mBtDeviceMain.get(position));
                Log.i(TAG, "you clicked on a device");
                bluetoothSetup.getBtDeviceToBeConnected(mBtDeviceMain.get(position));
                bluetoothSetup.startBtConnection();
            }
        });

        // reading change in seek bar

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(speed != progress)
                {
                    speed = progress;
                    switch (speed){
                        case 0:
                            speedToSend = 0;
                            Log.i(TAG, "writing 0 in SpeedToSend");
                            break;
                        case 1:
                            speedToSend = 1;
                            Log.i(TAG, "writing 1 in SpeedToSend");
                            break;
                        case 2:
                            speedToSend = 2;
                            Log.i(TAG, "writing 2 in SpeedToSend");
                            break;
                        case 3:
                            speedToSend = 3;
                            Log.i(TAG, "writing 3 in SpeedToSend");
                            break;
                        case 4:
                            speedToSend = 4;
                            Log.i(TAG, "writing 4 in SpeedToSend");
                            break;
                        case 5:
                            speedToSend = 5;
                            Log.i(TAG, "writing 5 in SpeedToSend");
                            break;
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG,"on pause");
        sensorFunctions.SensorUnRegister();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG,"on resume");
        sensorFunctions.SensorRegister();
        updateSwitchStatus();
        Log.i(TAG,"switch status update OnResume");
        bluetoothSetup.DiscoverDevices();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try
        {bluetoothSetup.unRegisterBroadcastReceiver();}
        catch (IllegalArgumentException e)
        {}
    }

    //override method of interface BluetoothCallBack to update new discovered devices
    @Override
    public void updateNewDeviceList(ArrayList<BluetoothDevice> mBtDevice) {
        Log.i(TAG,"entered to onUpdateNewDevice");
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

    //to clear device list on screen
    @Override
    public void clearList() {
        if(mDeviceListAdapter != null)
        {mDeviceListAdapter.clear();
        Log.i(TAG,"list cleared!!");
        }
    }

    @Override
    public void updateReceivedTextList(String string) {
            Log.i(TAG,"updating received message");
            String message = string;
            Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
            Log.i(TAG,"updated received message");
    }

    @Override
    public void getPermissionResult(Intent intent) {
        startActivityForResult(intent,REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == REQUEST_CODE) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                Log.i(TAG,"permission approved");
                Toast.makeText(this, "Bluetooth enabled", Toast.LENGTH_SHORT).show();

            }
            else{
                Toast.makeText(this,"permission denied!!",Toast.LENGTH_SHORT).show();
                Log.i(TAG,"permission denied");
            }
        }
    }

    //override method of interface SensorCallBack to update new Sensor readings
    @Override
    public void updateSensorData(float x, float y, float z, String command, int commandint) {
        //copy received sensor readings to float type variables and orientation in string
        xMain = x;        yMain = y;        zMain = z;        commandMain = command; commandNumber=commandint;

        //update textFields on screen
        x_data.setText(getString(R.string.x)+ xMain);
        y_data.setText(getString(R.string.y)+ yMain);
        z_data.setText(getString(R.string.z)+ zMain);
        commandBot.setText(commandMain);
        if (commandNumber!=commandNumberDirection) {
            commandNumberDirection = commandNumber;
            Log.i(TAG,"direction changed");
            if (commandNumber == 8) {
                toSend = "f";
                Log.i(TAG, "writing f in toSend");
            }
            if (commandNumber == 6) {
                toSend = "r";
                Log.i(TAG, "writing r in toSend");
            }
            if (commandNumber == 4) {
                toSend = "l";
                Log.i(TAG, "writing l in toSend");
            }
            if (commandNumber == 2) {
                toSend = "b";
                Log.i(TAG, "writing b in toSend");
            }
            if (commandNumber == 5) {
                toSend = "s";
                Log.i(TAG, "writing s in toSend");
            }
            byte[] bytes = toSend.getBytes();
            Log.i(TAG,"sending direction code to bluetoothFuntions");
            bluetoothSetup.write(bytes);
        }
    }


   public void updateSwitchStatus()
   {
       //to set the initial status of bluetooth switch
       btStatus = bluetoothSetup.GetBtStatus();
       if (btStatus == true)
       {
           BtSwitch.setChecked(true);
       }
       else
       {
           BtSwitch.setChecked(false);
       }
   }

}
