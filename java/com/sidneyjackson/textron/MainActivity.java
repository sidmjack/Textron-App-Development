package com.sidneyjackson.textron;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import static com.sidneyjackson.textron.R.color.colorPrimary;
import static com.sidneyjackson.textron.R.color.colorPrimaryDark;

public class MainActivity extends AppCompatActivity {

    private final static int REQUEST_ENABLE_BT = 1;

    private Set<BluetoothDevice> pairedDevices; //Set of Bluetooth Devices
    private BluetoothAdapter mBluetoothAdapter; // Device Bluetooth Adapter
    public String currAddress = "";
    private HashMap<String, String> btHashmap = new HashMap<String, String>();

    ListView deviceList; // List of Bluetooth Devices
    Button deviceListBtn; // Button to View List of Bluetooth Devices
    Button btnUnlock; // Unlock SmartBox Button
    Button btnLock; // Lock SmartBox Button
    Button btnDisconnect; // Disconnect from Bluetooth Device Button

    private ConnectBT cbt;

    private ProgressDialog progress;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    //static final UUID myUUID = UUID.fromString("19706a80-b858-11e7-8f1a-0800200c9a66"); //generated for me
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //given
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // TODO: Check whether this will actually work.
        cbt = new ConnectBT(); // Not sure how this will work...
        //cbt.execute();


        /* Bluetooth Adapter Object that represents the device's own Bluetooth Adapter -
        needed for all Bluetooth Activities...*/
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth...
            Toast.makeText(getApplicationContext(), "Bluetooth Device Not Available", Toast.LENGTH_LONG).show();
            finish(); // Finish APK
        } else if (!mBluetoothAdapter.isEnabled()) { // If Bluetooth isn't enable, request user to enable it.
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        /* Initialize Bluetooth Device List and Buttons */

        btnUnlock = (Button)findViewById(R.id.unlock_button_id);
        btnLock = (Button)findViewById(R.id.lock_button_id);
        deviceListBtn = (Button)findViewById(R.id.view_bt_pair_button_id);
        deviceList = (ListView)findViewById(R.id.listView);
        btnDisconnect = (Button)findViewById(R.id.disconnect_button_id);

        deviceListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                pairedDevicesList(); //method that will be called
            }
        });

        btnUnlock.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // TODO: Check whether this will actually work.
                btnUnlock.setBackgroundColor(getResources().getColor(colorPrimaryDark));
                btnLock.setBackgroundColor(getResources().getColor(colorPrimary));
                turnOnLed();      //Method to turn LED on (Simulating Unlock Feature)
            }
        });

        btnLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                // TODO: Check whether this will actually work.
                btnUnlock.setBackgroundColor(getResources().getColor(colorPrimary));
                btnLock.setBackgroundColor(getResources().getColor(colorPrimaryDark));
                turnOffLed();   //Method to turn LED off (Simulating Lock Feature)
            }
        });

        btnDisconnect.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ((TextView)findViewById(R.id.connected_bt_device_name_id)).setText("No BlueTooth Device Connected.");
                Disconnect(); //close connection
            }
        });

        buttonEffect(btnDisconnect);
        buttonEffect(deviceListBtn);

    }

    // Methods:

    private void pairedDevicesList()
    {
        pairedDevices = mBluetoothAdapter.getBondedDevices();
        ArrayList list = new ArrayList();

        if (pairedDevices.size()>0)
        {
            for(BluetoothDevice bt : pairedDevices)
            {
                list.add(bt.getName() + "\n" + bt.getAddress()); //Get the device's name and the address
                btHashmap.put(bt.getAddress(), bt.getName());
            }
        }
        else
        {
            Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
        }

        final ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, list);
        deviceList.setAdapter(adapter);
        deviceList.setOnItemClickListener(myListClickListener); //Method called when the device from the list is clicked

    }

    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener()
    {
        public void onItemClick (AdapterView av, View v, int arg2, long arg3)
        {
            // Get the device MAC address, the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);
            currAddress = address;

            cbt.execute(); // TODO: Hopefully this works?

            // Update text field with the name of the Bluetooth Device connected to.
            ((TextView)findViewById(R.id.connected_bt_device_name_id)).setText("Connected to: " + btHashmap.get(currAddress));
        }
    };

    // PROBLEM CHILD AS OF OCTOBER 23, 2017
    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(MainActivity.this, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try
            {
                if (btSocket == null || !isBtConnected)
                {
                    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = mBluetoothAdapter.getRemoteDevice(currAddress);//connects to the device's address and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess)
            {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                //finish();
            }
            else
            {
                msg("Connected.");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }
    // PROBLEM CHILD ENDS HERE...

    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }

    private void Disconnect()
    {
        if (btSocket!=null) //If the btSocket is busy
        {
            try
            {
                btSocket.close(); //close connection
            }
            catch (IOException e)
            { msg("Error");}
        }
    }

    private void turnOffLed() // Lock
    {
        if (btSocket!=null)
        {
            try
            {
                // TODO: Change Button Colors.
                btSocket.getOutputStream().write("TF".toString().getBytes());
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }

    private void turnOnLed() // Unlock
    {
        if (btSocket!=null)
        {
            try
            {
                // TODO: Change Button Colors.
                btSocket.getOutputStream().write("TO".toString().getBytes());
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }

    /* Just for effect...*/
    public static void buttonEffect(View button){
        button.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        v.getBackground().setColorFilter(0xff808080, PorterDuff.Mode.MULTIPLY);
                        v.invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        v.getBackground().clearColorFilter();
                        v.invalidate();
                        break;
                    }
                }
                return false;
            }
        });
    }

}
