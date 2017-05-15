package ble;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ble.antilost.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import main.BaseActivity;
/**
 * Created by orange on 4/28/17.
 */
@SuppressLint("NewApi")
public class BlePreventLostActivity extends BaseActivity implements OnClickListener {
    private String TAG = "DeviceListActivity";
    private TextView instruction_tips;
    private ListView bleListView;

    public static List<BleDevice> foundBleDevicesList = new ArrayList<>();
    public static List<BleDevice> scannedBleDevicesList = new ArrayList<>();
    // Uses MAC address as key to store data
    protected Map<String, Integer> scannedBleDevicesData = new HashMap<>();

    private BleDeviceListAdapter mDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;

    // These two flag values indicate the process just enter the scanning period
    private boolean mFindDevice = true;
    private boolean mLiveDevice = false;
    private Handler mHandler;
    private DeviceLiveThread deviceLiveThread;
    private static final int REQUEST_ENABLE_BT = 1;
    // Sets the scan period as 10 seconds
    private static final long SCAN_PERIOD = 1000 * 10;
    // Sets the live period as 3 seconds (this is for "keep tracking" button)
    private static final long LIVE_PERIOD = 1000 * 3;
    private static final String target_macAddr = "E3:0A:17:D9:A1:AF";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ble_prevent_lost);
        instruction_tips = (TextView) findViewById(R.id.instruction_tips);
        instruction_tips.setOnClickListener(this);
        bleListView = (ListView) findViewById(R.id.ble_device_list);

        // Initializes list view adapter
        mDeviceListAdapter = new BleDeviceListAdapter(this, foundBleDevicesList);
        bleListView.setAdapter(mDeviceListAdapter);

        mHandler = new Handler();
        // Checks whether BLE is supported on the device
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
        // Initializes a Bluetooth adapter
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        findDevice(true);
    }

    // Scan BLE devices
    // enable --> true: scan and stop after period; false: stop while onPause
    private void findDevice(final boolean enable) {
        if (enable) {

            setInstructStytle(true);
            mFindDevice = true;
            // Starts scanning
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            mDeviceListAdapter.notifyDataSetChanged();

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mFindDevice) {
                        if (foundBleDevicesList != null && foundBleDevicesList.size() > 0) {
                            instruction_tips.setVisibility(View.VISIBLE);
                            for (int i = 0; i < foundBleDevicesList.size(); i++) {
                                List<Integer> tempList = new ArrayList<>();
                                scannedBleDevicesData.put(foundBleDevicesList.get(i).getMacAddr(), foundBleDevicesList.get(i).getRssi());
                            }
                        }
                    }
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    mFindDevice = false;
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);
        } else {
            setInstructStytle(false);
            mFindDevice = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        invalidateOptionsMenu();
    }

    // Device can callback
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.e(TAG, "devices" + device.getName() + "    Rssi:" + rssi + " " + device.getAddress());
//                            if (device.getAddress().equals(target_macAddr)) {
                                BleDevice bleDevice = new BleDevice(device.getName(), device.getAddress(), rssi);
                                //Device is found for the first time
                                if (mFindDevice) {
                                    if (foundBleDevicesList != null && !foundBleDevicesList.contains(bleDevice)) {
                                        foundBleDevicesList.add(bleDevice);
                                        mDeviceListAdapter.notifyDataSetChanged();
                                    }
                                } else {
                                    if (scannedBleDevicesList != null && !scannedBleDevicesList.contains(bleDevice)) {
                                        scannedBleDevicesList.add(bleDevice);
	                        mDeviceListAdapter.notifyDataSetChanged();
                                    }

                                    // Updates the RSSI values
                                    scannedBleDevicesData.put(bleDevice.getMacAddr(), rssi);
                                    mDeviceListAdapter.notifyDataSetChanged();
                                }
                            }
//                        }
                    });
                }
            };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Users choose not to enable Bluetooth
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // Sets styling
    private void setInstructStytle(boolean needReScan) {
        if (needReScan) {
            instruction_tips.setVisibility(View.INVISIBLE);
            mDeviceListAdapter.clear();
        } else {
            instruction_tips.setVisibility(View.VISIBLE);
        }
    }

    public class DeviceLiveThread extends Thread {
        private boolean isRunning = true;

        private void stopThread() {
            isRunning = false;
        }

        @Override
        public void run() {
            // This is auto-generated method
            while (isRunning) {
                if (mLiveDevice) {
                    if (scannedBleDevicesData != null && scannedBleDevicesData.size() > 0) {
                        displayRssiResult(scannedBleDevicesData, target_macAddr);
                        scannedBleDevicesData.clear();
                    } else {
                        displayRssiResult(null, "");
                    }

                    try {
                        Thread.sleep(LIVE_PERIOD);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        Thread.sleep(LIVE_PERIOD);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void startDeviceLiving() {
        if (deviceLiveThread == null) {
            deviceLiveThread = new DeviceLiveThread();
            deviceLiveThread.start();
        }
    }

    // Displays the updated results
    private void displayRssiResult(Map<String, Integer> scannedBleDevices, String macAddr) {

        // This is for dealing with exceptions
        if (scannedBleDevices == null || !scannedBleDevices.containsKey(macAddr)) {
            int size = foundBleDevicesList.size();
            for (int i = 0; i < size; i++) {
                foundBleDevicesList.get(i).setRssi(0);
            }
        } else {
            // If the target device exists
            int size = foundBleDevicesList.size();
            for (int i = 0; i < size; i++) {
                String foundDeviceKey = foundBleDevicesList.get(i).getMacAddr();
                // If the target device is found, then update its RSSI value
                if (macAddr.equals(foundDeviceKey)) {
                    foundBleDevicesList.get(i).setRssi(scannedBleDevices.get(macAddr));
                } else {
                    // If the target device is not found, then set it to be RSSI = 0, i.e. lost
                    foundBleDevicesList.get(i).setRssi(0);
                }
            }
        }

        // Updates the notification data
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDeviceListAdapter.notifyDataSetChanged();
            }
        });
    }

    private void stopDeviceLiving() {

    }

    @Override
    public void onClick(View v) {
        // Sets auto-generated method for clicks
        switch (v.getId()) {
            case R.id.instruction_tips:
                mLiveDevice = !mLiveDevice;
                invalidateOptionsMenu();

                if (mLiveDevice) {
                    instruction_tips.setText(R.string.device_live_stop);
                    mBluetoothAdapter.startLeScan(mLeScanCallback);
                    startDeviceLiving();
                } else {
                    instruction_tips.setText(R.string.device_live_start);
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    stopDeviceLiving();
                }
                break;
            default:
                break;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.blemenu, menu);
        if (mLiveDevice) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else if (!mFindDevice) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(
                    R.layout.actionbar_indeterminate_progress);
        }
        return true;
    }

    // Scan & Stop
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                findDevice(true);
                break;
            case R.id.menu_stop:
                findDevice(false);
                break;
        }
        return true;
    }


    private long exitTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(getApplicationContext(), R.string.double_return_exit, Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                BlePreventLostActivity.this.finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.e(TAG, "*************onStart");
        // Ensures Bluetooth is enabled on the device
        // If Bluetooth is not currently enabled, then display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e(TAG, "*************onRestart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "onPause");

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG, "*************onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "*************onDestroy");
        if (deviceLiveThread != null) {
            deviceLiveThread.stopThread();
        }
        mLiveDevice = false;
        findDevice(false);
        scannedBleDevicesList.clear();
        foundBleDevicesList.clear();
        scannedBleDevicesData.clear();
        mDeviceListAdapter.notifyDataSetChanged();

    }


}
