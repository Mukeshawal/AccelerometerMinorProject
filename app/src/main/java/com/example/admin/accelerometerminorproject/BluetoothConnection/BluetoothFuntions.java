package com.example.admin.accelerometerminorproject.BluetoothConnection;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;
import static android.support.v4.content.ContextCompat.startActivity;


public class BluetoothFuntions {
    public BluetoothFuntions(Context context)
    {
        this.context = context;
    }
    private Context context;
    private static String TAG ="MYLOG";
    BluetoothAdapter myBluetoothAdapter =  BluetoothAdapter.getDefaultAdapter();

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
}
