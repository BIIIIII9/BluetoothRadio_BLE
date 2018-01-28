package com.example.a233.bluetooth_radio;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.example.a233.bluetooth_radio.MESSAGE";
    private int BLUETOOTH_DISCOVERABLE_DURATION = 300;
    private static final int REQUEST_CODE_BLUETOOTH_ON = 6677;
    private static boolean isClickedFlag=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        isClickedFlag=false;
        changeColorOfButton(isClickedFlag);
    }
    @Override
    public void onDestroy(){
        if(isClickedFlag) {
            Intent stopIntent = new Intent(this, ChangeNameOfBluetooth.class);
            stopService(stopIntent);
        }
        super.onDestroy();
    }

    public void sendMessage(View view) {
        if (!isClickedFlag) {
            if (MyBluetoothManager.isBluetoothSupported()) {
                if (!MyBluetoothManager.isBluetoothEnabled() && !MyBluetoothManager.backStageTurnOnBluetooth()) {
                    requestTurnOnBluetooth();
                }
            } else return;
            if (MyBluetoothManager.isBluetoothEnabled()) {
                Intent intent = new Intent(this, ChangeNameOfBluetooth.class);
                EditText editText = (EditText) findViewById(R.id.editText);
                String message = editText.getText().toString();
                intent.putExtra(EXTRA_MESSAGE, message);
                startService(intent);
                isClickedFlag = true;
                changeColorOfButton(isClickedFlag);
            }
        }
    }

    //让用户自己打开蓝牙，未完成
    //设定蓝牙扫描自由时间，0-无限。
    public void cancelService(View view) {
        if(isClickedFlag) {
            Intent stopIntent = new Intent(this, ChangeNameOfBluetooth.class);
            stopService(stopIntent);
            isClickedFlag=false;
            changeColorOfButton(isClickedFlag);
        }
    }
    public  void changeColorOfButton(boolean flag) {
        int color=!flag?0xFF3366FF:0xFF666666;
        int color2=flag?0xFF3366FF:0xFF666666;
        Button btn = (Button) findViewById(R.id.button);
        Button btn2 = (Button) findViewById(R.id.button2);
        btn.setBackgroundColor(color);
        btn.setEnabled(!flag);
        btn2.setBackgroundColor(color2);
        btn2.setEnabled(flag);
    }
    public void requestTurnOnBluetooth() {
        Intent requestBluetoothOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

        requestBluetoothOn.setAction(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        requestBluetoothOn.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, BLUETOOTH_DISCOVERABLE_DURATION);

        startActivityForResult(requestBluetoothOn, REQUEST_CODE_BLUETOOTH_ON);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_BLUETOOTH_ON) {
            switch (resultCode) {
                case Activity.RESULT_OK: {
                }
                break;

                case Activity.RESULT_CANCELED: {
                    unableTurnOnBluetoothDialog(MainActivity.this);
                }
                break;
                default:
                    break;
            }
        }
    }

    private void unableTurnOnBluetoothDialog(Context context) {
        new AlertDialog.Builder(context)
                .setTitle("Message")
                .setMessage("You choose not to open Bluetooth,service can not continue.")
                .show();
    }

}





