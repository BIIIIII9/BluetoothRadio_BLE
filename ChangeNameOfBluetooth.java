package com.example.a233.bluetooth_radio;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class ChangeNameOfBluetooth extends MyBluetoothManager {
    private  boolean workFlagMyThread;
    private static Thread myThread;
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public void onCreate(){
        super.onCreate();
        startForeground(4,new Notification());
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        foregroundNotification();
        receiveSystemBroad();
        openDiscoverable();
        final List<byte[]> listBytesMessage = Split(message, 248);
        workFlagMyThread=true;
        myThread=new Thread(new Runnable() {
            @Override
            public void run() {
                while (workFlagMyThread) {
                   try {
                        startMyRadio(listBytesMessage);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        myThread.start();
        return START_REDELIVER_INTENT;
    }
    @Override
    public void onDestroy() {
        workFlagMyThread=false;
        if(mBluetoothReceiver !=null) {
            unregisterReceiver(mBluetoothReceiver);
        }
        MyBluetoothManager.closeDiscoverable();
        stopForeground(true);
        super.onDestroy();
    }
    public void startMyRadio(List<byte[]> listBytesMessage) throws InterruptedException {
        final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        for (int i = 0; (workFlagMyThread)&&(i < listBytesMessage.size()); i++) {
            String name = new String(listBytesMessage.get(i));
                    adapter.setName(name);
            Log.i("startMyRadio", "startMyRadio"+System.currentTimeMillis());
                    myThread.sleep(1000);
        }
    }

    public List<byte[]> Split(String message, int size) {
        byte[] byteMessage_Be = null;
        try {
            byteMessage_Be = message.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (byteMessage_Be != null) {
            byte[] data = byteMessage_Be;
            List<byte[]> list = new ArrayList<byte[]>();
            for (int i = 0; i < data.length / size; i++) {
                byte[] s = new byte[size];
                System.arraycopy(data, size * i, s, 0, size);
                list.add(s);
            }
            if (data.length % size != 0) {
                byte[] s = new byte[data.length % size];
                System.arraycopy(data, data.length - data.length % size, s, 0, data.length % size);
                list.add(s);
            }
            return list;
        } else return null;
    }


    private void foregroundNotification() {
        final String channelID = "com.example.a233.bluetooth_radio.foregroundNotification";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("BluetoothRadio")
                .setContentText("Being broadcast");

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        startForeground(1, builder.build());
    }
}
