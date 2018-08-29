package com.skylin.uav.drawforterrain.select.gps_hand;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.skylin.uav.R;
import com.skylin.uav.drawforterrain.BaseActivity;
import com.skylin.uav.drawforterrain.service.BluetoothLeService;

import java.util.Set;

public class DeviceListActivity extends BaseActivity {
    // Debugging  
    private static final String TAG = "DeviceListActivity";
    private static final boolean D = true;

    // Return Intent extra  
    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    // Member fields  
    private BluetoothAdapter mBtAdapter;
//    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
//    private ArrayAdapter<String> mNewDevicesArrayAdapter;
    private TbaseAdapter mPairedDevicesArrayAdapter;
    private TbaseAdapter mNewDevicesArrayAdapter;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_list);

        // Set result CANCELED incase the user backs out  
        setResult(Activity.RESULT_CANCELED);
        Log.d(TAG, "onCreate");

        initViews();

        // Initialize array adapters. One for already paired devices and  
        // one for newly discovered devices  
//        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
//        mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
        mPairedDevicesArrayAdapter = new TbaseAdapter(this, R.layout.item_bluedevice);
        mNewDevicesArrayAdapter = new TbaseAdapter(this, R.layout.item_bluedevice);

        // Find and set up the ListView for paired devices  
        ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);
//        pairedListView.setOnItemClickListener(mDeviceClickListener);
        pairedListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Device item = mPairedDevicesArrayAdapter.getItem(position);
                Intent intent = new Intent();
                intent.putExtra(EXTRA_DEVICE_ADDRESS, item.getAdress());

                // Set result and finish this Activity
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });

        // Find and set up the ListView for newly discovered devices  
        ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
//        newDevicesListView.setOnItemClickListener(mDeviceClickListener);
        newDevicesListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Device item = mNewDevicesArrayAdapter.getItem(position);
                Intent intent = new Intent();
                intent.putExtra(EXTRA_DEVICE_ADDRESS, item.getAdress());

                // Set result and finish this Activity
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);   // Register for broadcasts when a device is discovered
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);        // Register for broadcasts when discovery has finished

        this.registerReceiver(mReceiver, intentFilter);


        // Get the local Bluetooth adapter  
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        // Get a set of currently paired devices  
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

        // If there are paired devices, add each one to the ArrayAdapter  
        if (pairedDevices.size() > 0) {
            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {
//                mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                mPairedDevicesArrayAdapter.add(new Device(device));
            }
        } else {
//            mPairedDevicesArrayAdapter.add("未找到已匹配的设备");
            mPairedDevicesArrayAdapter.add(new Device(getString(R.string.DeviceListActivity_tv1),""));
        }
    }

    private void initViews() {
        // Initialize the button to perform device discovery
        findViewById(R.id.button_scan).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                doDiscovery();
            }
        });

        findViewById(R.id.lift_topbar).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        TextView middle_topbar = findViewById(R.id.middle_topbar);
        middle_topbar.setText(getString(R.string.DeviceListActivity_tv2));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Make sure we're not doing discovery anymore  
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }

        // Unregister broadcast listeners  
        this.unregisterReceiver(mReceiver);
    }

    /**
     * Start device discover with the BluetoothAdapter
     */
    private void doDiscovery() {
        if (D) Log.d(TAG, "doDiscovery()");

        // Turn on sub-title for new devices  
        findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);

        // If we're already discovering, stop it  
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }

        mNewDevicesArrayAdapter.clear();
        // Request discover from BluetoothAdapter  
        mBtAdapter.startDiscovery();
    }

    // The BroadcastReceiver that listens for discovered devices and  
    // changes the title when discovery is finished  
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device  
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent  
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already  
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
//                    mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                    mNewDevicesArrayAdapter.add(new Device(device));
                }
                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if (mNewDevicesArrayAdapter.getCount() == 0) {
//                    mNewDevicesArrayAdapter.add("未发现设备");
                    mNewDevicesArrayAdapter.add(new Device(getString(R.string.DeviceListActivity_tv3),""));
                }
            }
        }
    };

}  