package com.example.candr.android_bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    public static String EXTRA_ADDRESS = "device_address";
    private Set<BluetoothDevice> PairedDevices;
    private BluetoothAdapter myBluetooth = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button Search = (Button) findViewById(R.id.button);
        final ListView Devicelist = (ListView) findViewById(R.id.list_device);

        myBluetooth = BluetoothAdapter.getDefaultAdapter();

        if (myBluetooth == null) {
            Toast.makeText(getApplicationContext(), "Bluetooth Device not available", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            if (myBluetooth.isEnabled()) {
            } else {
                Intent turnBtn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(turnBtn, 1);
            }

            Search.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ArrayAdapter adapter = PairedDevicesList();
                    Devicelist.setAdapter(adapter);
                    Devicelist.setOnItemClickListener(myListClickListener);
                }
            });
        }
    }


    private ArrayAdapter<String> PairedDevicesList() {
        PairedDevices = myBluetooth.getBondedDevices();
        ArrayList<String> arrayList = new ArrayList<String>();

        if (PairedDevices.size() > 0)
        {
            for (BluetoothDevice bt : PairedDevices) {
                arrayList.add(bt.getName() + "\n" + bt.getAddress());
            }
        } else {
            Toast.makeText(getApplicationContext(), "No Paired Devices Found", Toast.LENGTH_SHORT).show();
        }
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrayList);
        return adapter;


    }

    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView av, View v, int arg2, long arg3) {

            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);
            Intent i = new Intent(MainActivity.this, bluetoothActivity.class);
            i.putExtra(EXTRA_ADDRESS, address);
            startActivity(i);
        }
    };
}
