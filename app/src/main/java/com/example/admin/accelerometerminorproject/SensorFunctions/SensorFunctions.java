package com.example.admin.accelerometerminorproject.SensorFunctions;

import android.content.Context;
import android.util.Log;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import com.example.admin.accelerometerminorproject.R;
import static android.content.Context.SENSOR_SERVICE;
/**********************************************************************************************
 * ********************************************************************************************
 * ***********this class is used for all Accelerometer Sensor related functions****************
 * *******************************************************************************************
 * *******************************************************************************************/
public class SensorFunctions implements SensorEventListener{

    public Context context;
    private static String TAG ="MYLOG";
    private SensorManager SM;
    private Sensor mySensor;
    public float x,y,z; //to store readings from sensor
    public String command;
    public int commandInt;

    //interface type variable to callback a function in MainActivity
    SensorCallBack sensorCallBack = null;

    //this is a constructor that takes context of activity and interface type variable for CallBack
    public SensorFunctions(Context context,SensorCallBack sensorCallBack )
    {
        this.context = context;
        this.sensorCallBack = sensorCallBack;
    }

    //Call this method to Initialize Accelerometer Sensor
    public void SensorInitialize()
    {
        //create sensor manager
        SM = (SensorManager)context.getSystemService(SENSOR_SERVICE);

        //get accelerometer sensor
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

    //call this method to register sensorListener
    public void SensorRegister()
    {
        SM.registerListener(this, mySensor,SM.SENSOR_DELAY_NORMAL);
        Log.i(TAG, "sensor registered");
    }

    //call this method to unregister sensorListener
    public void SensorUnRegister()
    {
        SM.unregisterListener(this);
        Log.i(TAG, "sensor unregistered");
    }

    //this method is invoked when the orientation of phone changes or Accelerometer reading changes
    @Override
    public void onSensorChanged(SensorEvent event) {

        //event.values[] gives float type readings from sensor
        x = event.values[0];
        y = event.values[1];
        z = event.values[2];

        if (event.values[2]>0) //screen facing upwards
        {
            if((event.values[0]<2)&&(event.values[0]>-2)) //stop, forward and backwards
            {
                if(event.values[1]<-3)
                {command = context.getString(R.string.forward);
                commandInt = 8;}
                else if (event.values[1]>3)
                {command = context.getString(R.string.backward);
                commandInt = 2;}
                else
                {command = context.getString(R.string.stop);
                commandInt = 5;}
            }
            else if ((event.values[1]<2)&&(event.values[1]>-2))
            {
                if(event.values[0]<-3)
                {command = context.getString(R.string.right);
                    commandInt = 6;}
                else if (event.values[0]>3)
                {command = context.getString(R.string.left);
                    commandInt = 4;}
                else
                {command = context.getString(R.string.stop);
                    commandInt = 5;}
            }

        }
        //to update new readings in Activity
        sensorCallBack.updateSensorData(x,y,z,command,commandInt);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    //Interface definition to execute method in MainActivity
    public interface SensorCallBack
    {
        //method to pass float type variables(readings of sensor) and
        // string(orientation of phone) to other Activity
        public void updateSensorData(float x, float y, float z, String command, int commandint);
    }
}
