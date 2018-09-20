package com.example.admin.accelerometerminorproject;

import android.bluetooth.BluetoothDevice;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.admin.accelerometerminorproject.BluetoothConnection.BluetoothFuntions;
import com.example.admin.accelerometerminorproject.SensorFunctions.SensorFunctions;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements BluetoothFuntions.BluetoothCallBack,
        SensorFunctions.SensorCallBack {

    private static String TAG ="MYLOG";
    float xMain,yMain,zMain;
    boolean btStatus;
    String commandMain,Direction = "stop" , toSend = "s";

    private TextView x_data,y_data,z_data,commandBot;
    public ListView LvNewDevices;
    public ListView LvPairedDevices;
    public Switch BtSwitch;

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
        if (!Direction.equals(commandMain)) {
            Direction = commandMain;
            Log.i(TAG,"direction changed");
            if (commandMain.equals("Moving forward")) {
                toSend = "f";
                Log.i(TAG, "writing f in toSend");
            }
            if (commandMain.equals("Turning Right")) {
                toSend = "r";
                Log.i(TAG, "writing r in toSend");
            }
            if (commandMain.equals("Turning left")) {
                toSend = "l";
                Log.i(TAG, "writing l in toSend");
            }
            if (commandMain.equals("Moving backward")) {
                toSend = "b";
                Log.i(TAG, "writing b in toSend");
            }
            if (commandMain.equals("stop")) {
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
