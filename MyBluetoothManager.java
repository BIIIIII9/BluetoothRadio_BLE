

package com.example.a233.bluetooth_radio;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.NotificationCompat;

import java.lang.reflect.Method;

public abstract class MyBluetoothManager extends Service {
    static int nowStateBluetooth;
    static int nowDiscoverableBluetooth;
    protected static BroadcastReceiver mBluetoothReceiver;
    public static boolean isBluetoothSupported(){
        return BluetoothAdapter.getDefaultAdapter() != null;
    }
    public static boolean isBluetoothEnabled() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }
    public static boolean backStageTurnOnBluetooth() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return bluetoothAdapter != null && bluetoothAdapter.enable();
    }

    //Keep discoverable of Bluetooth constantly
    public static void openDiscoverable() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        try {
            Method setDiscoverableTimeout = BluetoothAdapter.class.getMethod("setDiscoverableTimeout", int.class);
            setDiscoverableTimeout.setAccessible(true);
            Method setScanMode = BluetoothAdapter.class.getMethod("setScanMode", int.class, int.class);
            setScanMode.setAccessible(true);

            setDiscoverableTimeout.invoke(adapter, 3000);
            setScanMode.invoke(adapter, BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE, 3000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //Stop to keep discoverable of Bluetooth constantly
    public static void closeDiscoverable() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        try {
            Method setDiscoverableTimeout = BluetoothAdapter.class.getMethod("setDiscoverableTimeout", int.class);
            setDiscoverableTimeout.setAccessible(true);
            Method setScanMode = BluetoothAdapter.class.getMethod("setScanMode", int.class, int.class);
            setScanMode.setAccessible(true);

            setDiscoverableTimeout.invoke(adapter, 1);
            setScanMode.invoke(adapter, BluetoothAdapter.SCAN_MODE_CONNECTABLE, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    protected   void  receiveSystemBroad() {
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        mBluetoothReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {
                    nowStateBluetooth = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
                    if (nowStateBluetooth != BluetoothAdapter.STATE_TURNING_ON || nowStateBluetooth != BluetoothAdapter.STATE_ON) {
                        MyBluetoothManager.backStageTurnOnBluetooth();
                        checkResult();
                    }
                } else if (BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(intent.getAction())) {
                    nowDiscoverableBluetooth = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.SCAN_MODE_NONE);
                    if (nowDiscoverableBluetooth != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                        MyBluetoothManager.openDiscoverable();
                        checkResult();
                    }
                }
            }
        };
        registerReceiver(mBluetoothReceiver, filter);
    }
    protected   void  checkResult() {

        if (nowStateBluetooth != BluetoothAdapter.STATE_TURNING_ON || nowStateBluetooth != BluetoothAdapter.STATE_ON) {
            unableResolveBluetoothNotification("Bluetooth Adapter Error.Bluetooth is turned off.");
            stopSelf();
        }

        if (nowDiscoverableBluetooth != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            unableResolveBluetoothNotification("Bluetooth Adapter Error.Bluetooth is undiscoverable.");
            stopSelf();
        }
    }

    protected   void unableResolveBluetoothNotification(String errorText) {
        final String channelID = "com.example.a233.bluetooth_radio.unableResolveBluetoothProblem";
        NotificationManager notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("Error")
                .setContentText(errorText)
                .setAutoCancel(true);
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        notifyManager.notify(2, builder.build());
    }


}