package com.example.a233.bluetooth_radio;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class ReceiveRadio extends Service {
    public ReceiveRadio() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
