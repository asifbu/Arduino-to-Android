package com.islam.asibul.sendreceivedata;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    Button btnOn, btnOff;
    TextView txtArduino, txtString, txtStringLength, sensorView0, sensorView1, sensorView2, sensorView3;
    Handler bluetoothIn;

    final int handlerState = 0;                        //used to identify handler message
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();

   // private ConnectedThread mConnectedThread;

    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // String for MAC address
    private static String address;



    Button button, sendData;
    TextView t1;
    EditText t2;
    Button button2,button3;

   // String address = null ;
    String maddress = null;
    String name=null;

    BluetoothAdapter myBluetooth = null;
   // BluetoothSocket btSocket = null;
    Set<BluetoothDevice> pairedDevices;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    @SuppressLint("HandlerLeak")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);



        t1= findViewById(R.id.textView1);
        try {
            bluetooth_connect_device();
        } catch (IOException e) {
            e.printStackTrace();
        }

      //  mConnectedThread.run();

        //Link the buttons and textViews to respective views
        btnOn = (Button) findViewById(R.id.buttonOn);
        btnOff = (Button) findViewById(R.id.buttonOff);
        txtString = (TextView) findViewById(R.id.txtString);
        txtStringLength = (TextView) findViewById(R.id.testView1);
        sensorView0 = (TextView) findViewById(R.id.sensorView0);
        sensorView1 = (TextView) findViewById(R.id.sensorView1);
        sensorView2 = (TextView) findViewById(R.id.sensorView2);
        sensorView3 = (TextView) findViewById(R.id.sensorView3);

        bluetoothIn = new Handler() {
            @SuppressLint("SetTextI18n")
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {                                     //if message is what we want
                    String readMessage = (String) msg.obj;                                                                // msg.arg1 = bytes from connect thread
                    recDataString.append(readMessage);                                      //keep appending to string until ~
                    int endOfLineIndex = recDataString.indexOf("~");                    // determine the end-of-line
                    if (endOfLineIndex > 0) {                                           // make sure there data before ~
                        String dataInPrint = recDataString.substring(0, endOfLineIndex);    // extract string
                        txtString.setText("Data Received = " + dataInPrint);
                        int dataLength = dataInPrint.length();                          //get length of data received
                        txtStringLength.setText("String Length = " + String.valueOf(dataLength));

                        if (recDataString.charAt(0) == '#')                             //if it starts with # we know it is what we are looking for
                        {
                            String sensor0 = recDataString.substring(1, 5);             //get sensor value from string between indices 1-5
                            String sensor1 = recDataString.substring(6, 10);            //same again...
                            String sensor2 = recDataString.substring(11, 15);
                            String sensor3 = recDataString.substring(16, 20);

                            sensorView0.setText(" Sensor 0  = " + sensor0);    //update the textviews with sensor values
                            sensorView1.setText(" Sensor 1  = " + sensor1);
                            sensorView2.setText(" Sensor 2  = " + sensor2);
                            sensorView3.setText(" Sensor 3  = " + sensor3);
                        }
                        recDataString.delete(0, recDataString.length());                    //clear all string data
                        // strIncom =" ";
                        dataInPrint = " ";
                    }
                }
            }
        };

        //btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter

        // Set up onClick listeners for buttons to send 1 or 0 to turn on/off LED
        btnOff.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                write("0");// Send "0" via Bluetooth
                Toast.makeText(getBaseContext(), "Turn off LED", Toast.LENGTH_SHORT).show();
            }
        });

        btnOn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

                write("1");    // Send "1" via Bluetooth

                Toast.makeText(getBaseContext(), "Turn on LED", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //bluetooth connect

    private void bluetooth_connect_device() throws IOException
    {
        try
        {
            myBluetooth = BluetoothAdapter.getDefaultAdapter();
            address = myBluetooth.getAddress();
            pairedDevices = myBluetooth.getBondedDevices();
            if (pairedDevices.size()>0)
            {
                for(BluetoothDevice bt : pairedDevices)
                {
                    address=bt.getAddress().toString();name = bt.getName().toString();
                    Toast.makeText(getApplicationContext(),"Connected", Toast.LENGTH_SHORT).show();

                }
            }

        }
        catch(Exception we){}
        myBluetooth = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);
        btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);
        btSocket.connect();


        try { t1.setText("BT Name: "+name+"\nBT Address: "+address);
        maddress = address;}
        catch(Exception e){}
    }



    //Checks that the Android device Bluetooth is available and prompts to be turned on if off

    //create new class for connect thread


    public void write(String input) {
        byte[] msgBuffer = input.getBytes();
         final OutputStream mmOutStream;
        OutputStream tmpOut = null;
        try {
            tmpOut = btSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mmOutStream = tmpOut;
        //converts entered String into bytes
        try {

            mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
        } catch (IOException e) {
            //if you cannot write, close the application
            Toast.makeText(getBaseContext(), "Connection Failure", Toast.LENGTH_LONG).show();
            finish();

        }
    }

    public void run()  {
        byte[] buffer = new byte[256];
        int bytes;
         final InputStream mmInStream;
        InputStream tmpIn = null;
        try {
            tmpIn = btSocket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mmInStream = tmpIn;

        // Keep looping to listen for received messages
        while (true) {
            try {
                bytes = mmInStream.read(buffer);                                                     //read bytes from input buffer
                String readMessage = new String(buffer, 0, bytes); // Send the obtained bytes to the UI Activity via handler
                bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
            } catch (IOException e) {
                break;
            }
        }
    }

//    private class ConnectedThread extends Thread {
//        private final InputStream mmInStream;
//      //  private final OutputStream mmOutStream;
//
//        //creation of the connect thread
//        public ConnectedThread(BluetoothSocket socket) {
//            InputStream tmpIn = null;
//          //  OutputStream tmpOut = null;
//
//            try {
//                //Create I/O streams for connection
//                tmpIn = socket.getInputStream();
//                //tmpOut = socket.getOutputStream();
//            } catch (IOException e) { }
//
//            mmInStream = tmpIn;
//           // mmOutStream = tmpOut;
//        }
//
//        public void run() {
//            byte[] buffer = new byte[256];
//            int bytes;
//
//            // Keep looping to listen for received messages
//            while (true) {
//                try {
//                    bytes = mmInStream.read(buffer);            //read bytes from input buffer
//                    String readMessage = new String(buffer, 0, bytes);
//                    // Send the obtained bytes to the UI Activity via handler
//                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
//                } catch (IOException e) {
//                    break;
//                }
//            }
//        }
//
//    }
}
