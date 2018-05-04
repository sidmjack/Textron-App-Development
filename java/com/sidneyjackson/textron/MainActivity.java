package com.sidneyjackson.textron;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

import android.telephony.SmsManager;

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

    // Bluetooth Variables:

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

    // SMS Variables:

    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS =0 ;
    SmsManager smsManager = SmsManager.getDefault();

    // Wifi Variable:
    TcpClient mTcpClient;

    // Box and Button State
    boolean isLocked = true;

    // Message Type Flags:
    boolean btActivated = true;
    boolean smsActivated = false;
    boolean wifiActivated = false;

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

        //btnUnlock = (Button)findViewById(R.id.unlock_button_id);
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

        /*btnUnlock.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                btnUnlock.setBackgroundColor(getResources().getColor(colorPrimaryDark));
                btnLock.setBackgroundColor(getResources().getColor(colorPrimary));
                turnOnLed(); //Method to turn LED on (Simulating Unlock Feature)
            }
        });*/

        btnLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if (isLocked) {
                    btnLock.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    btnLock.setTextColor(getResources().getColor(R.color.colorAccent));
                    btnLock.setTypeface(Typeface.DEFAULT_BOLD);
                    btnLock.setText("Lock Venus Capture");
                    //sendSMS(phoneNumber, unlock_message);

                    // This works, but doesn't exactly do what we want...
                    /*Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + phoneNumber));
                    intent.putExtra("sms_body", unlock_message);
                    startActivity(intent);*/

                    // SMS:
                    if (smsActivated == true) {
                        sendSMSMessage(isLocked );
                    }

                    // Wifi Webpage:
                    if (wifiActivated == true) {
                        openWifiWebpage();
                    }

                    // Wifi Stuff:
                   /* new ConnectTask().execute("");
                    if (mTcpClient != null) {
                        mTcpClient.sendMessage("1");
                        msg("Wifi Message Sent!");
                    }
                    if (mTcpClient != null) {
                        mTcpClient.stopClient();
                    }*/


                    if (btActivated == true) {
                        turnOnLed(); //Method to turn LED off (Simulating Lock Feature)
                    }

                    isLocked = false;
                } else {
                    btnLock.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    btnLock.setTextColor(getResources().getColor(R.color.white));
                    btnLock.setTypeface(Typeface.DEFAULT);
                    btnLock.setText("Unlock Venus Capture");
                    //sendSMS(phoneNumber, lock_message);

                    // This works, but doesn't exactly do what we want...
                    /*Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + phoneNumber));
                    intent.putExtra("sms_body", unlock_message);
                    startActivity(intent);*/

                    // SMS:
                    if (smsActivated == true) {
                        sendSMSMessage(isLocked);
                    }

                    // Wifi Webpage:
                    if (wifiActivated == true) {
                        openWifiWebpage();
                    }

                    // Wifi Stuff:
                   /* new ConnectTask().execute("");
                    if (mTcpClient != null) {
                        mTcpClient.sendMessage("0");
                        msg("Wifi Message Sent!");
                    }
                    if (mTcpClient != null) {
                        mTcpClient.stopClient();
                    }*/

                    if (btActivated == true) {
                        turnOffLed(); //Method to turn LED off (Simulating Lock Feature)
                    }
                    isLocked = true;
                }
            }
        });

        registerForContextMenu(btnLock); // New

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
    // This is where Context Menu's are "inflated" in the main activity.

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        //MenuInflater inflater = getMenuInflater();
        if (v.getId() == R.id.lock_button_id) {
            menu.setHeaderTitle("Venus Comm Menu");
            getMenuInflater().inflate(R.menu.lock_communication_menu, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // Handle item selection
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        switch (item.getItemId()) {
            case R.id.blue_tooth_communication:
                // BT: Connect, Scan, Disconnect...
                // Turn On/Off appropriate methods of communication.
                btActivated = true;
                smsActivated = false;
                wifiActivated = false;
                msg("Switching to Bluetooth Connection");
                return true;
            case R.id.wifi_communication:
                // Wifi: Connect, Scan, Disconnect...
                // Turn On/Off appropriate methods of communication.
                btActivated = false;
                smsActivated = false;
                wifiActivated = true;
                msg("Switching to WiFi Connection");
                return true;
            case R.id.sms_communication:
                // SMS: Connect, Scan, Disconnect Bluetooth...
                // Turn On/Off appropriate methods of communication.
                btActivated = false;
                smsActivated = true;
                wifiActivated = false;
                msg("Switching to SMS Connection");
                return true;
            default:
                msg("No changes made for communication method.");
                return super.onOptionsItemSelected(item);
        }
    }

    // Methods:

    protected void openWifiWebpage() {
        Uri uri = Uri.parse("http://192.168.1.75"); // missing 'http://' will cause crashed
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    protected void sendSMSMessage(boolean boxIsLocked) { // Need to encrypt this... (No need to give user visual)

        //String phoneNo = "3869565577"; // Need to update this to box phoneNumber
        //String phoneNo = "4434534989";
        String phoneNo = "443-739-3024";
        //String message = "Open Sesame!";

        String lock_message;
        String lock_message_enc;

        if (boxIsLocked) {
            //lock_message_enc = "Unlock_101010101";
            //lock_message = "@lighton#";
            lock_message = "@Unlock_101010101#";
        } else {
            //lock_message_enc = "Lock_01010101010";
            //lock_message = "@lightoff#";
            lock_message = "@Lock_01010101010#";
        }
        lock_message_enc = lock_message_enc.replaceAll("(\\r|\\n)", "");

        String key = "aaaaaaaaaaaaaaaa";
        String ival = "AAAAAAAAAAAAAAAA";

        byte[] new_lock_message = new byte[16]; // Seems sketch... Initialize with something eventually.
        int nextByte = 0;

        try {
            AES encryptionTool = new AES();
            new_lock_message = encryptionTool.encrypt(lock_message_enc, key, ival.getBytes("UTF-8"));
            System.out.println(encryptionTool.getCipher("Lock_01010101010", key, ival.getBytes("UTF-8")));
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.SEND_SMS)) {


            } else {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.SEND_SMS}, MY_PERMISSIONS_REQUEST_SEND_SMS);
            }
        } else {
            Toast.makeText(MainActivity.this, "Permission (already) Granted!", Toast.LENGTH_SHORT).show();
            // Send the message
            smsManager.sendTextMessage(phoneNo, null, lock_message, null, null);
            String enc_message = "@" + (new_lock_message).toString() + "#";
            smsManager.sendTextMessage(phoneNo, null, enc_message, null, null);
        }
    }

    private void sendSMS(String phoneNumber, String message) {
        smsManager.sendTextMessage(phoneNumber, null, message, null, null);
    }

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
                    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();//get the mobile bt_icon device
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
            }
            progress.dismiss();
        }
    }

    // Wifi Stuff:

    public class ConnectTask extends AsyncTask<String, String, TcpClient> {

        @Override
        protected TcpClient doInBackground(String... message) {

            //we create a TCPClient object
            mTcpClient = new TcpClient(new TcpClient.OnMessageReceived() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(String message) {
                    //this method calls the onProgressUpdate
                    publishProgress(message);
                }
            });
            mTcpClient.run();

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            //response received from server
            Log.d("test", "response " + values[0]);
            //process server response here....

        }
    }

    // Displays Toasts:
    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_SHORT).show();
    }

    // Disconnect Version 2:
    private void Disconnect() {
        try {
            if (btSocket.getInputStream() != null) {
                try {

                    btSocket.getInputStream().close();
                } catch (Exception e) {}
            }

            if (btSocket.getOutputStream() != null) {
                try {
                    btSocket.getOutputStream().close();
                } catch (Exception e) {}
            }

            if (btSocket != null) {
                try {
                    btSocket.close();
                } catch (Exception e) {
                }
                btSocket = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        isBtConnected = false;
        bt_connection_established = false;
        ((TextView)findViewById(R.id.connected_bt_device_name_id)).setText("No Bluetooth Device Connected...");
        msg("BlueTooth Disconnected");

    }

    private void turnOffLed() // Lock
    {
        if (btSocket!=null)
        {
            try
            {

                String lock_message = "Lock_01010101010";
                lock_message = lock_message.replaceAll("(\\r|\\n)", "");

                String key = "aaaaaaaaaaaaaaaa";
                String ival = "AAAAAAAAAAAAAAAA";

                byte[] new_lock_message = new byte[16]; // Seems sketch... Initialize with something eventually.
                int nextByte = 0;

                try {
                    AES encryptionTool = new AES();
                    new_lock_message = encryptionTool.encrypt(lock_message, key, ival.getBytes("UTF-8"));
                    System.out.println(encryptionTool.getCipher("Lock_01010101010", key, ival.getBytes("UTF-8")));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                System.out.print("Last value output: ");
                System.out.println(new_lock_message[15]);
                btSocket.getOutputStream().write(new_lock_message);
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
                String unlock_message = "Unlock_101010101"; // 11/14/17
                unlock_message = unlock_message.replaceAll("(\\r|\\n)", "");

                String key = "aaaaaaaaaaaaaaaa";
                String ival = "AAAAAAAAAAAAAAAA";

                byte[] new_lock_message = new byte[16]; // Seems sketch... Initialize with something eventually.
                int nextByte;

                try {
                    AES encryptionTool = new AES();
                    new_lock_message = encryptionTool.encrypt(unlock_message, key, ival.getBytes("UTF-8"));
                    System.out.println(encryptionTool.getCipher("Unlock_101010101", key, ival.getBytes("UTF-8")));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                System.out.print("Last value output: ");
                System.out.println(new_lock_message[15]);
                btSocket.getOutputStream().write(new_lock_message);
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
