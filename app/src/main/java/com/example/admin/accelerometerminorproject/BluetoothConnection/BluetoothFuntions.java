package com.example.admin.accelerometerminorproject.BluetoothConnection;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import static android.support.v4.app.ActivityCompat.startActivityForResult;

/**********************************************************************************************
 * ********************************************************************************************
 * ******************this class is used for all Bluetooth related functions********************
 * *******************************************************************************************
 * *******************************************************************************************/
public class BluetoothFuntions {

    public Context context;
    private BluetoothAdapter myBluetoothAdapter;
    private static String TAG ="MYLOG";
    private static final String appName = "Accelerometer Minor Project";
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public BluetoothDevice toBePairedDevice;    //used in method "pairDevice"
    private BluetoothDevice btDeviceToBeConnected;  //used in method "getBtDeviceToBeConnected"

    //private AcceptThread mInsecureAcceptThread;

    ProgressDialog mProgressDialog;     //used in method "startClient"

    private ConnectThread mConnectThread;
    private BluetoothDevice mDevice;    // used in connect thread
    private UUID deviceUUID;    // used in connect thread

    public String message;
    public int checker = 0;

    private ConnectedThread mConnectedThread;

    //ArrayList of Bluetooth Device to store newly discovered devices
    public ArrayList<BluetoothDevice> mBtDevices = new ArrayList<>();
    public ArrayList<BluetoothDevice> mPairedBtDevices = new ArrayList<>();

    //interface type variable to callback a function in MainActivity
    public BluetoothCallBack mBluetoothCallBack = null;

    //this is a constructor that takes context of activity and interface type variable for callback
    public BluetoothFuntions( Context context,BluetoothCallBack bluetoothCallBack)
    {
        this.context = context;
        this.mBluetoothCallBack = bluetoothCallBack;
        //define Bluetooth adapter
        myBluetoothAdapter =  BluetoothAdapter.getDefaultAdapter();
        //start();
    }

    /**
     * start the chat service. specifically start AcceptThread to begin a
     * session to listening (server) mode. called by the Activity OnResume()
     */
    /*public synchronized void start()
    {
        Log.i(TAG,"start");

        //cancel any Thread attempting to make connection
        if(mConnectThread != null)
        {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if(mInsecureAcceptThread == null)
        {
            mInsecureAcceptThread = new AcceptThread();
            mInsecureAcceptThread.start();
        }
    }

    // this thread runs while listening for incoming connections
    //it behaves like server side client
    //it runs until a connection is accepted or until cancelled
    private class AcceptThread extends Thread
    {
        //the local server socket
        private final BluetoothServerSocket mServerSocket;;

        public AcceptThread()
        {
            BluetoothServerSocket tmp = null;

            //create a new listening server socket

            try {
                tmp = myBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName,MY_UUID_INSECURE);
                Log.i(TAG,"Accept thread:setting up server using " + MY_UUID_INSECURE);
            } catch (IOException e) {
                Log.i(TAG,"Accept thread : IOException " + e.getMessage());
            }
            mServerSocket = tmp;
        }

        public void run()
        {
            Log.i(TAG,"run: accept thread running");
            BluetoothSocket socket = null;
            try
            {
                //this is blocking call and will only return on a successful connection or an exception
                Log.i(TAG, "run: RFCOM server socket start...");
                socket = mServerSocket.accept();
                Log.i(TAG, "run: RFCOM Connection successful");

            }
            catch (IOException e)
            {
                Log.i(TAG, "run: IOException " + e.getMessage());
            }
            catch (NullPointerException e1)
            {e1.printStackTrace();}
            if (socket != null)
            {
                connected(socket,mDevice);
            }
            Log.i(TAG,"END mAcceptThread");
        }

        public void cancel()
        {
            Log.i(TAG,"Cancel: cancelling AcceptThread");
            try
            {
                mServerSocket.close();
            }
            catch (IOException e)
            {
                Log.i(TAG," cancel: close of AcceptTread ServerSocket Failed "+ e.getMessage());
            }
        }
    }*/

    private class ConnectThread extends Thread
    {
        private BluetoothSocket msocket;

        public ConnectThread(BluetoothDevice device, UUID uuid)
        {
            Log.i(TAG,"connectThread: started");
            mDevice = device;
            deviceUUID = uuid;
        }

        public void run()
        {
            final String deviceName1 = mDevice.getName();
            BluetoothSocket tmp = null;
            Log.i(TAG,"RUN mconnectThread");

            //get a BluetoothSocket for a connection with the given BTdevice
            try{
                Log.i(TAG,"connect Thread: trying to create InsecuredRFcommSocket using UUID:" + MY_UUID_INSECURE);
                tmp = mDevice.createRfcommSocketToServiceRecord(deviceUUID);
            }catch (IOException e){
                Log.i(TAG,"connectTHread : could not create insecureRFcommSocket:"+e.getMessage());
            }

            msocket = tmp;

            //always cancel discovery because it will slow down a connection
            myBluetoothAdapter.cancelDiscovery();

            //make a connection to the bluetooth socket

            try{
                //this is a blocking call and will only return in a successful connection or an exception
                msocket.connect();
            }
            catch (IOException e)
            {
                //close the socket
                try
                {
                    msocket.close();
                    Log.i(TAG,"connect thread run: closed socket");
                }
                catch (IOException el)
                {
                    Log.i(TAG,"connectThread: run : unable to close connection in socket:"+ el.getMessage());
                }
                Log.i(TAG,"run: connectThread: could not connect to UUID:"+ MY_UUID_INSECURE);

                //dismiss progress dialog
                try{
                    mProgressDialog.dismiss();}
                catch (NullPointerException elk){
                    e.printStackTrace();}

                //toast to display unable to connect
                Handler mHandler = new Handler(Looper.getMainLooper());
                Runnable mRunnable = new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context,"unable to connect to "+deviceName1 +"!!",Toast.LENGTH_SHORT).show();
                    }
                };
                mHandler.post(mRunnable);
                cancel();
            }
            connected(msocket,mDevice);

        }
        public void cancel()
        {
            Log.i(TAG,"connect thread Cancel: closing client Socket");
            try
            {
                msocket.close();
            }
            catch (IOException e)
            {
                Log.i(TAG," connect thread cancel: close() of msocket in ConnectThread Failed "+ e.getMessage());
            }
        }
    }

    /**
     * Accept thread starts and sits waiting for a connection.
     * then ConnectThread starts and attempts to make a connection with the other
     * devices AcceptThread.
     */

    public void startClient(BluetoothDevice device, UUID uuid)
    {
        Log.i(TAG,"startClient: started.");


        //init progress dialog
        mProgressDialog = ProgressDialog.show(context,"connecting Bluetooth"
                ,"please wait......",true);

        mConnectThread = new ConnectThread(device, uuid);
        mConnectThread.start();
    }

    private class ConnectedThread extends Thread
    {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private BluetoothDevice btDevice;

        public ConnectedThread(BluetoothSocket socket, BluetoothDevice bluetoothDevice)
        {
            Log.i(TAG,"ConnectedThread: starting.");

            btDevice = bluetoothDevice;

            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            //dismiss the progress dialog when connection is established

            try{
            mProgressDialog.dismiss();}
            catch (NullPointerException e){
                e.printStackTrace();}

            try{
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            }catch (IOException e){
                e.printStackTrace();
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }
        public void run()
        {
            final String deviceName = btDevice.getName();
            Handler mHandler = new Handler(Looper.getMainLooper());

            Runnable mRunnable = new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context,"connected to "+deviceName +"!!",Toast.LENGTH_SHORT).show();
                }
            };
            mHandler.post(mRunnable);

            byte[] buffer = new byte[3]; //buffer store for the stream

            int bytes; //bytes returned from read()
            checker = 1;

            while (true)
            {//read from inputStream
                try {
                    bytes = mmInStream.read(buffer);
                    final String incomingMessage = new String(buffer, 0, bytes);
                    Log.i(TAG,"InputStream:" + incomingMessage);
                    for (int i = 0; i < buffer.length; i++)
                    {
                    Log.i(TAG,"InputStream:incoming bits" + buffer[i]);}
                } catch (IOException e) {
                    Log.i(TAG,"write: error reading Input stream." + e.getMessage());
                    break;
                }
            }
        }


       // call this from the main activity to send data to the remote device
       public void write(byte[] bytes)
       {
           String text = new String(bytes, Charset.defaultCharset());
           Log.i(TAG,"bytes being transmitted are :");
           for (int i = 0; i < bytes.length; i++) {
               Log.i(TAG, String.valueOf(bytes[i]));
           }
           Log.i(TAG,"write: writing to output stream:" + text);
           try {
               mmOutStream.write(bytes);
               Log.i(TAG,"data write successful");
           } catch (IOException e) {
               Log.i(TAG,"write: error writing to output stream. "+e.getMessage());

           }

       }

       //call this from the main activity to shutdown the connection
        public void cancel()
        {
            try{
                mmSocket.close();
                Log.i(TAG,"connected thread run: closed socket");

            }catch (IOException e){}
        }
    }

    private void connected(BluetoothSocket mmSocket, BluetoothDevice mmDevice)
    {
        Log.i(TAG,"connected: starting");

        //start the thread to manage the connection and perform transmission
        mConnectedThread = new ConnectedThread(mmSocket,mmDevice);
        mConnectedThread.start();
    }

    /**
     * write to the connectedThread in an un synchronized manner
     *
     */

    public void write(byte[] out)
    {
        if(checker == 1) {
            //create temporary object
            ConnectedThread r;

            //synchronize a copy of the ConnectedThread
            Log.i(TAG, "write: write called.");
            //perform the write
            mConnectedThread.write(out);
        }
        else
        {Log.i(TAG,"bluetoothFunctions - write:device not connected yet");}
    }

    //call this method to get bluetooth status
    public boolean GetBtStatus(){
        if (myBluetoothAdapter == null) //if bluetooth is not available in that device
        {
            Log.i(TAG, "bluetooth not available");
            Toast.makeText(this.context,"Bluetooth not available", Toast.LENGTH_LONG).show();
        }
        if (!myBluetoothAdapter.isEnabled()) // if bluetooth is not enabled
        {
            return(false);
        }
        else
        {
            return(true);
        }
    }
    // Call this method to enable Bluetooth
    public void BtOn()
    {
        if (myBluetoothAdapter == null) //if bluetooth is not available in that device
        {
            Log.i(TAG, "bluetooth not available");
            Toast.makeText(this.context,"Bluetooth not available", Toast.LENGTH_LONG).show();
        }
        if (myBluetoothAdapter !=null)
        {
            if (!myBluetoothAdapter.isEnabled()) // if bluetooth is not enabled
            {
                Intent enableBt = new Intent(myBluetoothAdapter.ACTION_REQUEST_ENABLE); //request to enable bluetooth
                mBluetoothCallBack.getPermissionResult(enableBt);

                    // create intent filter to catch state change in bluetooth
                    IntentFilter BtIntent = new IntentFilter(myBluetoothAdapter.ACTION_STATE_CHANGED);
                    context.registerReceiver(myBroadcastReceiver, BtIntent);
            }
        }
    }

    //call this method to disable bluetooth
    public void Btoff()
    {
        if (myBluetoothAdapter != null)
        {
            if (myBluetoothAdapter.isEnabled())
            {
                myBluetoothAdapter.disable();
                Toast.makeText(this.context, "Bluetooth disabled", Toast.LENGTH_LONG).show();

                IntentFilter BtIntent = new IntentFilter(myBluetoothAdapter.ACTION_STATE_CHANGED);
                context.registerReceiver(myBroadcastReceiver, BtIntent);
                clearArrayList();
                Log.i(TAG, "array list cleared");
                mBluetoothCallBack.clearList();
                Log.i(TAG, "device adapter cleared");
            }
        }
    }


    //definition of myBroadcastReceiver with ACTION_STATE_CHANGED used for BtOnOff
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


    // call this method to make your device discoverable for 45 secs
    public void makeDiscoverable()
    {
        Log.i(TAG, "making device discoverable for 45 sec");
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 45);
        context.startActivity(discoverableIntent);

        IntentFilter makeDis = new IntentFilter(myBluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        context.registerReceiver(myBroadcastReceiver2,makeDis);
    }

    //definition of myBroadcastReceiver2 with ACTION_SCAN_MODE_CHANGED used for makeDiscoverable
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


    //Call this method to search for other available Bluetooth device in Proximity
    public void DiscoverDevices()
    {
        if(myBluetoothAdapter.isEnabled()) {
            if (myBluetoothAdapter.isDiscovering()) {
                myBluetoothAdapter.cancelDiscovery();

                //To start discovering devices, simply call startDiscovery()
                myBluetoothAdapter.startDiscovery();

                // Register for broadcasts when a device is discovered.
                IntentFilter DisDevIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                context.registerReceiver(myBroadcastReceiver3, DisDevIntent);
                Log.i(TAG, "Discovering devices");
            }
            if (!myBluetoothAdapter.isDiscovering()) {
                //To start discovering devices, simply call startDiscovery()
                myBluetoothAdapter.startDiscovery();
                IntentFilter DisDevIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                context.registerReceiver(myBroadcastReceiver3, DisDevIntent);
                Log.i(TAG, "Discovering devices");
            }
        }
    }

    //definition of myBroadcastReceiver3 with ACTION_FOUND used for DiscoverDevices
    private BroadcastReceiver myBroadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String Action = intent.getAction();
            Log.i(TAG,"action_found");
            if(Action.equals(BluetoothDevice.ACTION_FOUND))
            {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                //this adds available devices in ArrayList<BluetoothDevice> type variable mBtDevices
                mBtDevices.add(device);

                //this prints available devices in Logcat
                Log.i(TAG,"on receive:"+ device.getName() + device.getAddress());
                if(mBluetoothCallBack != null)
                {
                    //to update Device list from MainActivity using interface "BluetoothCallBack" method updateDeviceList
                    mBluetoothCallBack.updateNewDeviceList(mBtDevices);
                }
            }
        }
    };

    //to pair new devices and update paired device list
    public void pairDevice(BluetoothDevice toBepairedDevice)
    {
        toBePairedDevice = toBepairedDevice;

        String devicename = toBePairedDevice.getName();
        String deviceaddress = toBePairedDevice.getAddress();

        Log.i(TAG,"you clicked on device name: "+devicename);
        Log.i(TAG,"device Address: "+deviceaddress);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Log.i(TAG,"trying to pair with  "+devicename);
            toBePairedDevice.createBond();
        }
        else
        {
            Log.i(TAG,"cannot pair from app!!  api less than 21");

        }
        IntentFilter pairDeviceIntent = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        context.registerReceiver(myBroadcastReceiver4,pairDeviceIntent);
    }
    private BroadcastReceiver myBroadcastReceiver4 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String Action = intent.getAction();
            Log.i(TAG,"action_bond_state_changed");
            if(Action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED))
            {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                //case1: Already Bonded
                if (device.getBondState() == BluetoothDevice.BOND_BONDED)
                {
                    Log.i(TAG,"device bond bonded");
                }
                //case2: creating bond
                if (device.getBondState() == BluetoothDevice.BOND_BONDING)
                {Log.i(TAG,"device bond bonding");}

                //case3: breaking bond
                if (device.getBondState() == BluetoothDevice.BOND_BONDING)
                {Log.i(TAG,"device bond bonding");}
            }
        }
    };


    //to get list of paired devices
    public void getPairedDevice()
    {
        if(myBluetoothAdapter.isEnabled())
        {
            Toast.makeText(this.context,"updating paired device list...",Toast.LENGTH_SHORT).show();
            clearArrayList();
            Set<BluetoothDevice> pairedDevices = myBluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                // There are paired devices. Get the name and address of each paired device.
                for (BluetoothDevice device : pairedDevices) {
                    mPairedBtDevices.add(device);
                    }
            }
        }
        Toast.makeText(this.context,"paired device list updated!",Toast.LENGTH_SHORT).show();

    }

    //to cancel discovery
    public void cancelDiscovery()
    {
        myBluetoothAdapter.cancelDiscovery();
        Log.i(TAG,"discovery canceled");
    }

    //to unregister all broadcast receiver
    public void unRegisterBroadcastReceiver()
    {
        context.unregisterReceiver(myBroadcastReceiver);
        context.unregisterReceiver(myBroadcastReceiver2);
        context.unregisterReceiver(myBroadcastReceiver3);
        context.unregisterReceiver(myBroadcastReceiver4);
    }

    //to clear ArrayLIst
    public void clearArrayList()
    {
        Log.i(TAG,"BluetoothFunction:clearArraylist");
        if (mBtDevices !=null)
        {mBtDevices.clear();}
        if (mPairedBtDevices !=null)
        {mPairedBtDevices.clear();}
    }

    public void getBtDeviceToBeConnected(BluetoothDevice device)
    {
        Log.i(TAG,"in getBtDeviceToBeConnected");
        btDeviceToBeConnected = device;
        Log.i(TAG,"you clicked on device:"+btDeviceToBeConnected.getName());
    }

    /**
     * starting chat service method
     *
     */
    public void startBtConnection(){
        Log.i(TAG,"startBtConnection: initializing RFCOM Bluetooth connection");
        startClient(btDeviceToBeConnected,MY_UUID_INSECURE);
    }


    //Interface definition to execute method in MainActivity
    public interface BluetoothCallBack
    {
        //method to pass ArrayList<BluetoothDevice> type variable to other Activity
        public void updateNewDeviceList(ArrayList<BluetoothDevice> mBtDevice);
        public void clearList();
        public void updateReceivedTextList(String string);
        public void getPermissionResult(Intent intent);
    }

}



