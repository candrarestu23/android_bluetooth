package com.example.candr.android_bluetooth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.UUID;

/**
 * Created by candr on 12/30/2017.
 */

public class bluetoothActivity extends AppCompatActivity {
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    int tempData,tempData2;
    boolean statData=false;
    int PID1 =1;
    String aString;
    int PIDstop = 2;
    BluetoothSocket btSocket = null;
    BluetoothAdapter mybluetooth = null;
    String address = null;
    InputStreamReader aReader = null;
    InputStream mmInputStream = null;
    boolean checker = false;
    String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/TAFOLDER";
    private ProgressDialog progress;
    private boolean isBtConnected = false;
    private BufferedReader mBufferedReader = null;
    private int STORAGE_PERMISSION_CODE = 23;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_activity);
        final TextView SpeedValue = (TextView) findViewById(R.id.speedText);
        final TextView brakeBarValue = (TextView) findViewById(R.id.brakeBarValue);
        final Button DataTransferOn = (Button) findViewById(R.id.RecordOn);
        final Button DataTransferOff = (Button) findViewById(R.id.RecordOff);
        final TextView SeekBarValue = (TextView) findViewById(R.id.SeekBarValue);
        final TextView recordStat = (TextView) findViewById(R.id.recordStat);
        final Button Gas=(Button)findViewById(R.id.TEST);
        final Button Rem = (Button)findViewById(R.id.Rem);
        final Button PID = (Button) findViewById(R.id.PID);

        SeekBar speedBar = (SeekBar) findViewById(R.id.speedBar);
        SeekBar brakeBar = (SeekBar) findViewById(R.id.brakeBar);
        speedBar.setMax(90);
        brakeBar.setMax(90);
        Intent newIntent = getIntent();
        address = newIntent.getStringExtra(MainActivity.EXTRA_ADDRESS);

        new connectBluetooth().execute();



        Thread textSpeedThread = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(200);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run(){
                                try {
                                    mmInputStream=btSocket.getInputStream();
                                    aReader = new InputStreamReader(mmInputStream);
                                    mBufferedReader = new BufferedReader(aReader);
                                    aString = mBufferedReader.readLine();

                                    SpeedValue.setText(String.valueOf(aString));
                                    if(statData==true){
                                        savePublic();
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };
        textSpeedThread.start();


        Gas.setOnTouchListener(new View.OnTouchListener() {
            String temp;
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if(checker==false){
                        try {
                            temp = String.valueOf(tempData);
                            Log.d("TEST",temp);
                            btSocket.getOutputStream().write(temp.toString().getBytes());
                            checker=true;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    try {
                        temp = String.valueOf("90");
                        Log.d("TEST",temp);
                        btSocket.getOutputStream().write("90".toString().getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    checker =false;
                }

                return false;
            }
        });

        PID.setOnTouchListener(new View.OnTouchListener() {
            String temp;
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction()==motionEvent.ACTION_DOWN){
                    if(checker == false){
                        try {
                            temp = String.valueOf("PID");
                            btSocket.getOutputStream().write(temp.toString().getBytes());
                            Log.d("PID",temp);
                            checker=true;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    try {
                        temp = String.valueOf("PIDstop");
                        btSocket.getOutputStream().write(temp.toString().getBytes());
                        Log.d("PID",temp);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    checker =false;
                }

                return false;
            }
        });

        Rem.setOnTouchListener(new View.OnTouchListener() {
            String temp;
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if(checker==false){
                        try {
                            temp = String.valueOf(tempData2);
                            Log.d("REM",temp);
                            btSocket.getOutputStream().write(temp.toString().getBytes());
                            checker=true;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    try {
                        temp = String.valueOf("90");
                        Log.d("REM",temp);
                        btSocket.getOutputStream().write("90".toString().getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    checker =false;
                }

                return false;
            }
        });

        speedBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;


            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progressChangedValue = i + 90;
                tempData = progressChangedValue;
                if (b == true)//fromUser --> callback that notifies client when the progress level has changed
                {

                    Thread textSpeedThread = new Thread() {

                        @Override
                        public void run() {
                            try {
                                while (!isInterrupted()) {
                                    Thread.sleep(200);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            SeekBarValue.setText(String.valueOf(progressChangedValue));
                                        }
                                    });
                                }
                            } catch (InterruptedException e) {
                            }
                        }
                    };
                    textSpeedThread.start();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        brakeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;


            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progressChangedValue = 90 - i;
                tempData2 = progressChangedValue;
                if (b == true)//fromUser --> callback that notifies client when the progress level has changed
                {

                    Thread textSpeedThread = new Thread() {

                        @Override
                        public void run() {
                            try {
                                while (!isInterrupted()) {
                                    Thread.sleep(200);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            brakeBarValue.setText(String.valueOf(progressChangedValue));
                                        }
                                    });
                                }
                            } catch (InterruptedException e) {
                            }
                        }
                    };
                    textSpeedThread.start();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        DataTransferOn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View view) {
                recordStat.setText("ON");
                if (btSocket != null) {
                    try {
                       canWriteOnExternalStorage();
                        btSocket.getOutputStream().write("DataOn".toString().getBytes());//sends off to the bluetooth module
                        msg("Transfer Data Turned On");
                        statData=true;
                    } catch (IOException e) {
                        msg("Transfer Data Error");//calls msg fuction to display "Error"
                    }
                }
            }
        });
        DataTransferOff.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View view) {
                recordStat.setText("OFF");
                if (btSocket != null) {
                    try {
                        statData=false;
                        btSocket.getOutputStream().write("DataOff".toString().getBytes());//sends off to the bluetooth module
                        msg("Transfer Data Turned off");
                    } catch (IOException e) {
                        msg("Turned off Transfer Error");//calls msg fuction to display "Error"
                    }
                }
            }
        });


    }

    private void Disconnect() {
        if (btSocket != null) {
            try {
                btSocket.close();//disconnects bluetooth
                finish();
            } catch (IOException e) {
                msg("Error");// displays "Error" message
            }
        }
    }

    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();//displays a message on screen
    }

    public boolean canWriteOnExternalStorage(){
        // get the state of your external storage
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // if storage is mounted return true
            Toast.makeText(getApplicationContext(), state, Toast.LENGTH_LONG).show();
            Log.v("sTag", "Yes, can write to external storage.");
            return true;
        }else {
            Toast.makeText(getApplicationContext(), "External Storage not available", Toast.LENGTH_SHORT).show();
        }
        return false;
    }



    public void savePublic() {
        //Permission to access external storage
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        String info = aString+"\n";
        File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);// Folder Name
        File myFile = new File(folder, "TAFILE");// Filename
        // Make sure the Pictures directory exists.
        if(!myFile.exists()) {
            myFile.mkdirs();
        }
        try{
            File gpxfile = new File(myFile, "TEST.txt");
            FileWriter writer = new FileWriter(gpxfile,true);
            writer.append(info);
            writer.flush();
            writer.close();

        }catch (Exception e){
            e.printStackTrace();

        }
        // Tell the media scanner about the new file so that it is
        // immediately available to the user.
        MediaScannerConnection.scanFile(this,
                new String[] { myFile.toString() }, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });
    }


    private class connectBluetooth extends AsyncTask<Void, Void, Void> {
        private boolean ConnectSuccess = true;

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(bluetoothActivity.this, "Connecting", "Please Wait");// show a progress of connection
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progressdialog is shown, connection is done in background
        {
            try {
                if (btSocket == null || isBtConnected)//when bluetooth device not connected
                {
                    mybluetooth = BluetoothAdapter.getDefaultAdapter();//getthe local device's bluetooth adapter
                    BluetoothDevice device = mybluetooth.getRemoteDevice(address);// connects to the device's address
                    btSocket = device.createInsecureRfcommSocketToServiceRecord(myUUID);// creates a connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();
                }
            } catch (IOException e) {
                ConnectSuccess = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result)// after executing doInBackground, it checks if everything went fine]
        {
            super.onPostExecute(result);
            if (!ConnectSuccess) {
                msg("Connection Failed! Try Again!");
                finish();
            } else {
                msg("Connected");
                isBtConnected = true;
            }
            progress.dismiss();//closes progress bar

        }
    }



}
