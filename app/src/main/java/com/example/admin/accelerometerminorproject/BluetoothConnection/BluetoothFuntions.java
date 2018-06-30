package com.example.admin.accelerometerminorproject.BluetoothConnection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ArrayAdapter;

import com.example.admin.accelerometerminorproject.DeviceAdapter;
import com.example.admin.accelerometerminorproject.MainActivity;
import com.example.admin.accelerometerminorproject.R;

import java.util.ArrayList;

import static android.support.v4.content.ContextCompat.startActivity;


public class BluetoothFuntions {


    private Context context;
    private static String TAG ="MYLOG";
    BluetoothAdapter myBluetoothAdapter =  BluetoothAdapter.getDefaultAdapter();
    public ArrayList<BluetoothDevice> mBtDevices = new ArrayList<>();
    //public DeviceAdapter mDeviceListAdapter;
    NewDevice mNewDevice = null;

    public BluetoothFuntions( Context context,NewDevice newDevice)
    {
        this.context = context;
        this.mNewDevice = newDevice;

    }




    //create a Broadcast for action state changed
    private final BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
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

    // Create a BroadcastReceiver for ACTION_SCAN_MODE_CHANGED.
    private final BroadcastReceiver myBroadcastReceiver2 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(myBluetoothAdapter.ACTION_SCAN_MODE_CHANGED))
            {
                final int mode = intent.getIntExtra(myBluetoothAdapter.EXTRA_SCAN_MODE,myBluetoothAdapter.ERROR);

                switch (mode)
                {
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                    {
                        //device is in discoverable mode
                        Log.i(TAG, "my broadcast receiver2 discoverability enabled");
                    }
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                    {
                        Log.i(TAG, "my broadcast receiver2 discoverability disabled able to receive connection");
                    }
                    case BluetoothAdapter.SCAN_MODE_NONE:
                    {
                        Log.i(TAG, "my broadcast receiver2 discoverability disabled. not able to receive connection");
                    }
                    case BluetoothAdapter.STATE_CONNECTING:
                    {
                        Log.i(TAG, "my broadcast receiver2 connecting..");
                    }
                    break;
                    case BluetoothAdapter.STATE_CONNECTED:
                    {
                        Log.i(TAG, "my broadcast receiver2 connected.");
                    }
                }
            }
        }
    };

    private BroadcastReceiver myBroadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String Action = intent.getAction();
            Log.i(TAG,"action_found");
            if(Action.equals(BluetoothDevice.ACTION_FOUND))
            {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mBtDevices.add(device);

                Log.i(TAG,"on receive:"+ device.getName() + device.getAddress());

               if(mNewDevice != null)
               {
                    mNewDevice.onUpdateDevice(mBtDevices);
               }

            }
        }
    };

    public void BtOnOff ()
    {
        if (myBluetoothAdapter == null) //if bluetooth is not available in that device
        {
            Log.i(TAG, "bluetooth not available");
            Toast.makeText(this.context,"Bluetooth not available", Toast.LENGTH_LONG).show();
        }
        if (!myBluetoothAdapter.isEnabled()) // if bluetooth is not enabled
        {
            Intent enableBt = new Intent(myBluetoothAdapter.ACTION_REQUEST_ENABLE); //request to enable bluetooth
            context.startActivity(enableBt);
            Toast.makeText(this.context,"Bluetooth enabled", Toast.LENGTH_LONG).show();

            // create intent filter to catch state change in bluetooth
           IntentFilter BtIntent =  new IntentFilter(myBluetoothAdapter.ACTION_STATE_CHANGED);
            context.registerReceiver(myBroadcastReceiver, BtIntent);
        }
        if (myBluetoothAdapter.isEnabled())
        {
            myBluetoothAdapter.disable();
            Toast.makeText(this.context,"Bluetooth disabled", Toast.LENGTH_LONG).show();

            IntentFilter BtIntent =  new IntentFilter(myBluetoothAdapter.ACTION_STATE_CHANGED);
            context.registerReceiver(myBroadcastReceiver, BtIntent);
        }
    }

    public void DiscoverDevices()
    {
        if(myBluetoothAdapter.isDiscovering())
        {
            myBluetoothAdapter.cancelDiscovery();

            //To start discovering devices, simply call startDiscovery()
            myBluetoothAdapter.startDiscovery();

            // Register for broadcasts when a device is discovered.
            IntentFilter DisDevIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            context.registerReceiver(myBroadcastReceiver3,DisDevIntent);
            Log.i(TAG, "Discovering devices");
        }
        if(!myBluetoothAdapter.isDiscovering())
        {
            //To start discovering devices, simply call startDiscovery()
            myBluetoothAdapter.startDiscovery();
            IntentFilter DisDevIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            context.registerReceiver(myBroadcastReceiver3,DisDevIntent);
            Log.i(TAG, "Discovering devices");
        }



    }

    public void makeDiscoverable()
    {
        Log.i(TAG, "making device discoverable for 45 sec");
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 45);
        context.startActivity(discoverableIntent);

        IntentFilter makeDis = new IntentFilter(myBluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        context.registerReceiver(myBroadcastReceiver2,makeDis);
    }

    public interface NewDevice
    {
        public void onUpdateDevice(ArrayList<BluetoothDevice> mBtDevice);
    }
}



