package com.sidneyjackson.textron;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
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
    public String connectedAddress = "";

    private HashMap<String, String> btHashmap_paired = new HashMap<String, String>();
    private HashMap<String, String> bt_Hashmap_discovered = new HashMap<String, String>();
    private ArrayList<BluetoothDevice> btDeviceList = new ArrayList<BluetoothDevice>();

    //final ArrayList discover_list = new ArrayList();

    public boolean bt_connection_established = false;

    ListView deviceListPaired; // List of Bluetooth Devices
    ListView deviceListScanned; // List of Bluetooth Devices
    Button deviceListPairedBtn; // Button to View List of Past Bluetooth Devices
    Button deviceListScanBtn; // Button to View List of all Discovered Bluetooth Devices
    Button btnUnlock; // Unlock SmartBox Button
    Button btnLock; // Lock SmartBox Button
    Button btnDisconnect; // Disconnect from Bluetooth Device Button

    public final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                btDeviceList.add(device);
            }
        }
    };

    private ProgressDialog progress;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //given

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Bluetooth Adapter Object that represents the device's own Bluetooth Adapter - needed for all Bluetooth Activities...*/
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth...
            Toast.makeText(getApplicationContext(), "Bluetooth Device Not Available", Toast.LENGTH_LONG).show();
            finish(); // Finish APK
        } else if (!mBluetoothAdapter.isEnabled()) { // If Bluetooth isn't enable, request user to enable it.
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            // Register the broadcast receiver
            //IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            //registerReceiver(mReceiver, filter);
        }

        /* Initialize Bluetooth Device List and Buttons */

        btnUnlock = (Button)findViewById(R.id.unlock_button_id);
        btnLock = (Button)findViewById(R.id.lock_button_id);

        deviceListPairedBtn = (Button)findViewById(R.id.view_bt_pair_button_id);
        deviceListPaired = (ListView)findViewById(R.id.listView_paired_items);

        //deviceListScanBtn = (Button)findViewById(R.id.view_bt_scan_button_id);
        //deviceListScanned = (ListView)findViewById(R.id.listView_scanned_items);

        btnDisconnect = (Button)findViewById(R.id.disconnect_button_id);

        // Displays formerly paired devices.
        deviceListPairedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                // Populates List of Formerly Paired Devices.
                pairedDevicesList();
            }
        });

        // Displays currently discovered devices.
        /*deviceListScanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if (mBluetoothAdapter.isDiscovering()) {
                    mBluetoothAdapter.cancelDiscovery();
                }
                mBluetoothAdapter.startDiscovery();

                discoveredDevicesList();
            }
        });*/

        btnUnlock.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                btnUnlock.setBackgroundColor(getResources().getColor(colorPrimaryDark));
                btnLock.setBackgroundColor(getResources().getColor(colorPrimary));
                turnOnLed(); //Method to turn LED on (Simulating Unlock Feature)
            }
        });

        btnLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                btnUnlock.setBackgroundColor(getResources().getColor(colorPrimary));
                btnLock.setBackgroundColor(getResources().getColor(colorPrimaryDark));
                turnOffLed(); //Method to turn LED off (Simulating Lock Feature)
            }
        });

        btnDisconnect.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ((TextView)findViewById(R.id.connected_bt_device_name_id)).setText("No BlueTooth Device Connected.");
                Disconnect(); //Close BT Connection
                bt_connection_established = false; // Cause app to crash
            }
        });

        buttonEffect(btnDisconnect);
        buttonEffect(deviceListPairedBtn);
        //buttonEffect(deviceListScanBtn);
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
                btHashmap_paired.put(bt.getAddress(), bt.getName());
            }
        }
        else
        {
            Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
        }

        final ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, list);
        deviceListPaired.setAdapter(adapter);
        deviceListPaired.setOnItemClickListener(myListClickListener_paired); //Method called when the device from the list is clicked

    }

    /*private void discoveredDevicesList()
    {
        ArrayList list = new ArrayList();

        if (btDeviceList.size()>0)
        {
            for(BluetoothDevice bt : btDeviceList)
            {
                list.add(bt.getName() + "\n" + bt.getAddress()); //Get the device's name and the address
                bt_Hashmap_discovered.put(bt.getAddress(), bt.getName());
            }
        }
        else
        {
            Toast.makeText(getApplicationContext(), "No Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
        }

        ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, list);
        deviceListScanned.setAdapter(adapter);
        deviceListScanned.setOnItemClickListener(myListClickListener_discovered); //Method called when the device from the list is clicked
    }*/

    // On CLick List Item Listener for Formerly Paired Devices.
    private AdapterView.OnItemClickListener myListClickListener_paired = new AdapterView.OnItemClickListener()
    {
        public void onItemClick (AdapterView av, View v, int arg2, long arg3)
        {
            // Get the device MAC address, the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);
            currAddress = address;

            if (bt_connection_established == false) {
                myRunner.run();
            } else {
               String message = "Already Paired with a Device";
               msg(message);
            }
        }
    };

    // On CLick List Item Listener for Currently Discovered Devices.
    /*private AdapterView.OnItemClickListener myListClickListener_discovered = new AdapterView.OnItemClickListener()
    {
        public void onItemClick (AdapterView av, View v, int arg2, long arg3)
        {
            // Get the device MAC address, the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);
            currAddress = address;

            if (bt_connection_established == false) {
                myRunner.run();
            } else {
                String message = "Already Paired with a Device";
                msg(message);
            }
        }
    };*/

    // //////////////////////////////////
    // //////////////////////////////////
    // DONT NEED TO FOCUS ON RIGHT NOW
    // //////////////////////////////////
    // //////////////////////////////////

    // Establishes Bluetooth Connection:
    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(MainActivity.this, "Connecting...", "Please wait...");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try
            {
                // TODO: Add these comments back in eventually.
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
                isBtConnected = false;
                bt_connection_established = false;
                ((TextView)findViewById(R.id.connected_bt_device_name_id)).setText("No BlueTooth Device Connected.");
                //connectedAddress = "No Device Connected.";
                //finish();
            }
            else
            {
                msg("Connected.");
                isBtConnected = true;
                bt_connection_established = true;
                if (btHashmap_paired.get(currAddress) != null) {
                    ((TextView)findViewById(R.id.connected_bt_device_name_id)).setText("Connected to: " + btHashmap_paired.get(currAddress));
                    connectedAddress = btHashmap_paired.get(currAddress);
                }

                if(btSocket != null)
                {
                    try
                    {
                        btSocket.getOutputStream().write("InitBTConnection".getBytes(Charset.forName("UTF-8")));
                    }
                    catch (IOException e)
                    {
                        msg("Error");
                    }
                }

                /*else if (bt_Hashmap_discovered.get(currAddress) != null){
                    ((TextView)findViewById(R.id.connected_bt_device_name_id)).setText("Connected to: " + bt_Hashmap_discovered.get(currAddress));
                    //connectedAddress = bt_Hashmap_discovered.get(currAddress);
                }*/

            }
            progress.dismiss();
        }
    }

    // Displays Long Toasts:
    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }

    // Disconnects Established BT Connection
    private void Disconnect()
    {
        if (btSocket!=null) //If the btSocket is busy
        {
            try
            {
                byte[] buffer = new byte[256];  // buffer store for the stream
                int bytes; // bytes returned from read()

                btSocket.getOutputStream().write("Disconnect010101".getBytes(Charset.forName("UTF-8")));
                bytes = new DataInputStream(btSocket.getInputStream()).read(buffer);
                String readMessage = new String(buffer, 0, bytes);
                if (readMessage.contains("BtDisconnectACKD")) {
                    btSocket.close(); //close connection
                    isBtConnected = false;
                    bt_connection_established = true;
                    //connectedAddress = "";
                    msg("BlueTooth Disconnected");
                }
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }

    private void turnOffLed() // Lock
    {
        if (btSocket!=null)
        {
            try
            {
                // TODO: Change Button Colors.
                //btSocket.getOutputStream().write("TF".toString().getBytes()); // Formerly "TF"

                //btSocket.getOutputStream().write('0'); // Formerly "TF" // 11/9/17

                btSocket.getOutputStream().write("Lock_01010101010".getBytes(Charset.forName("UTF-8")));

                /*String lock_message = "Lock_01010101010"; // 11/14/17
                lock_message = lock_message.replaceAll("(\\r|\\n)", "");
                String key = "aaaaaaaaaaaaaaaa";
                String ival = "AAAAAAAAAAAAAAAA";

                byte[] new_lock_message = new byte[16]; // Seems sketch... Initialize with something eventually.
                int nextByte = 0;

                try {
                    AES encryptionTool = new AES();
                    new_lock_message = encryptionTool.encrypt(lock_message, key, ival.getBytes("UTF-8"));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                InputStream inputStream = new ByteArrayInputStream(new_lock_message);

                while ((nextByte=inputStream.read()) != -1) {
                    btSocket.getOutputStream().write(nextByte);
                    System.out.println(new Integer(nextByte));
                }*/
            }
            catch (IOException e)
            {
                if(bt_connection_established == false) {
                    msg("No BlueTooth Device Connected");
                }
            }
        } else {
            msg("No BlueTooth Device Connected");
        }
    }

    private void turnOnLed() // Unlock
    {
        if (btSocket!=null)
        {
            try
            {
                // TODO: Change Button Colors: Potential Bug/Error - Double Check sequential pairing.
                // TODO: Attempt using the end-line and trim() functions to get this to work.
                //btSocket.getOutputStream().write("TO".toString().getBytes()); // Formerly "TO"
               // btSocket.getOutputStream().write('1'); // Formerly "TF" 11/9/17

                btSocket.getOutputStream().write("Unlock_101010101".getBytes(Charset.forName("UTF-8")));

                /*String unlock_message = "Unlock_101010101"; // 11/14/17
                unlock_message = unlock_message.replaceAll("(\\r|\\n)", "");

                String key = "aaaaaaaaaaaaaaaa";
                String ival = "AAAAAAAAAAAAAAAA";

                byte[] new_lock_message = new byte[16]; // Seems sketch... Initialize with something eventually.
                int nextByte;

                try {
                    AES encryptionTool = new AES();
                    new_lock_message = encryptionTool.encrypt(unlock_message, key, ival.getBytes("UTF-8"));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                InputStream inputStream = new ByteArrayInputStream(new_lock_message);

                while ((nextByte=inputStream.read()) != -1) {
                    btSocket.getOutputStream().write(nextByte);
                    System.out.println(new Integer(nextByte));
                }*/
            }
            catch (IOException e)
            {
                if(bt_connection_established == false) {
                    msg("No BlueTooth Device Connected");
                }
            }
        } else {
            msg("No BlueTooth Device Connected");
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

    //Creates a new instance of ConnectBT in order to run a fresh BT Connection Thread
    Runnable myRunner = new Runnable(){
        public void run() {
            new ConnectBT ().execute();
        }
    };

}
