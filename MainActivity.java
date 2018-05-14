package com.example.a233.bluetooth_radio;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;


import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
    public static final int serviceMaxRuntime=300000;
    static final int BLUETOOTH_DISCOVERABLE_DURATION = 300;
    //Color of button
    static final int colorUnable= 0xFF666666;
    static final int colorNormal=0xFF3366FF;
    static  final int colorWork=0xFF66FFCC;

    public static final String EXTRA_MESSAGE = "com.example.a233.bluetooth_radio.MESSAGE";
    static final int REQUEST_CODE_SEND_MY_MASSAGE = 1001;
    static final int REQUEST_CODE_SEARCH_MY_MASSAGE=1002;
    static final int REQUEST_CODE_ASK_Bluetooth_PERMISSION_TO_DISCOVER=1003;
    private static ButtonState myButtonState;
    private ListView myMainListView;
    private MyListViewManager myListViewManager;
    static public final String LocalAction_RefreshUI= "Local_Broadcast_RefreshActivityUI";
    static public final String ServiceOnDestroy="Local_Broadcast_Service_OnDestroy";
    private Runnable timeRunable;
    Handler mainHandler;
    LocalBroadcastManager myLocalBroadcastManager;
    BroadcastReceiver myReceiver;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Change Text to Multi-line
        EditText editText = findViewById(R.id.editText);
        editText.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        editText.setGravity(Gravity.TOP);
        editText.setSingleLine(false);
        editText.setHorizontallyScrolling(false);

        myMainListView=findViewById(R.id.MyContentListView);
        myListViewManager=new MyListViewManager();
        myListViewManager.star();
        myLocalBroadcastManager=LocalBroadcastManager.getInstance(this);
        myLocalBroadcastManager.registerReceiver(new LocalBroadcastReceiver(),new IntentFilter(LocalAction_RefreshUI));
        myButtonState = ButtonState.CANCEL;
        setButtonState(myButtonState);
        mainHandler=new Handler();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        runCancelService();
    }
    //After serviceMaxRuntime seconds stop service
    private void setServiceRuntime(){
        timeRunable=new Runnable() {
            @Override
            public void run() {
                runCancelService();
            }
        };
        mainHandler.postDelayed(timeRunable,serviceMaxRuntime);
    }
    private void hideKeyboard(View view){
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm!=null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    //Onclick button Send
    public void sendMessage(View view) {
        hideKeyboard(view);
        if (myButtonState == ButtonState.CANCEL && MyBluetoothMethodManager.isBluetoothSupported()) {
            if (!MyBluetoothMethodManager.isBluetoothEnabled()) {
                requestBluetoothDiscoverable();
            } else {
                runSendMessage();
                setServiceRuntime();
            }
        }
    }
    public void runSendMessage() {
        if (MyBluetoothMethodManager.isBluetoothEnabled()) {
            Intent intent = new Intent(this, ChangeNameOfBluetooth.class);
            EditText editText = findViewById(R.id.editText);
            String message = editText.getText().toString();
            intent.putExtra(EXTRA_MESSAGE, message);
            startService(intent);
            myButtonState = ButtonState.SEND;
            setButtonState(myButtonState);
            receiveSystemBroad();
        }
    }
    public void requestBluetoothDiscoverable() {
        Intent requestBluetoothOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        requestBluetoothOn.setAction(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        requestBluetoothOn.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, BLUETOOTH_DISCOVERABLE_DURATION);//BLUETOOTH_DISCOVERABLE_DURATION=300s
        startActivityForResult(requestBluetoothOn, REQUEST_CODE_SEND_MY_MASSAGE);
    }
    //Onclick button Search
    public  void searchMessage(View view) {
        hideKeyboard(view);
        if (myButtonState == ButtonState.CANCEL && MyBluetoothMethodManager.isBluetoothSupported()) {
            if (!MyBluetoothMethodManager.isBluetoothEnabled()) {
                requestTurnOnBluetooth();
            } else {
                runSearchMessage();
                setServiceRuntime();
            }
        }
    }
    //TODO:Try BLE
    public void runSearchMessage() {
        if (MyBluetoothMethodManager.isBluetoothEnabled()&&checkPermission()) {
            Intent intent = new Intent(this, ReceiveRadio_BLE.class);
            startService(intent);
            myButtonState = ButtonState.SEARCH;
            setButtonState(myButtonState);
            receiveSystemBroad();
        }
    }
    public void requestTurnOnBluetooth(){
        Intent requestBluetoothOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(requestBluetoothOn, REQUEST_CODE_SEARCH_MY_MASSAGE);
    }
    //If version Android of target phone more than or equal 6.0 ,request permission
    public boolean checkPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String permission = Manifest.permission.ACCESS_COARSE_LOCATION;
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{permission}, REQUEST_CODE_ASK_Bluetooth_PERMISSION_TO_DISCOVER);
                return false;
            }
        }
        return true;
    }
    //Receive result of request that turn on bluetooth,change to discoverable
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_SEND_MY_MASSAGE || requestCode == REQUEST_CODE_SEARCH_MY_MASSAGE) {
            switch (resultCode) {
                case Activity.RESULT_OK: {
                    if (requestCode == REQUEST_CODE_SEARCH_MY_MASSAGE) {
                        runSearchMessage();
                    }
                }
                break;
                case BLUETOOTH_DISCOVERABLE_DURATION: {
                    if (requestCode == REQUEST_CODE_SEND_MY_MASSAGE) {
                        runSendMessage();
                    }
                }
                break;
                case Activity.RESULT_CANCELED: {
                    MainActivityDialog(MainActivity.this, "Can not set bluetooth state!");
                }
                break;
                default:
                    break;
            }
        }
    }
    //TODO:BLE
    //Receive result of request that permission for Android 6.0
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_ASK_Bluetooth_PERMISSION_TO_DISCOVER) {
            switch (grantResults[0]) {
                case PackageManager.PERMISSION_GRANTED: {
                    runSearchMessage();
                }
                break;

                case PackageManager.PERMISSION_DENIED: {
                    MainActivityDialog(MainActivity.this,"Can not get permission to search bluetooth device!");
                }
                break;
                default:
                    break;
            }
        }
    }
    public void cancelService(View view) {
        runCancelService();
        mainHandler.removeCallbacks(timeRunable);
    }
    // Onclick Button Cancel
    public void runCancelService(){
        if(myReceiver !=null) {
            unregisterReceiver(myReceiver);
        }
        if (myButtonState==ButtonState.SEND) {
            Intent stopIntent = new Intent(this, ChangeNameOfBluetooth.class);
            stopService(stopIntent);
            myButtonState=ButtonState.CANCEL;
            setButtonState(myButtonState);
        }
        //TODO:Try BLE
        else if(myButtonState==ButtonState.SEARCH){
            Intent stopIntent= new Intent(this,ReceiveRadio_BLE.class);
            stopService(stopIntent);
            myButtonState=ButtonState.CANCEL;
            setButtonState(myButtonState);
        }
    }
    //Change color and state of button
    void setButtonState(ButtonState state) {
        Button btn =  findViewById(R.id.button);
        Button btn2 =  findViewById(R.id.button2);
        Button btn3= findViewById(R.id.button3);
        Boolean boolBTN1=false;
        Boolean boolBTN2=false;
        Boolean boolBTN3=false;
        switch (state){
            case SEND:{
                boolBTN2=true;
                btn.setBackgroundColor(colorWork);
                btn3.setBackgroundColor(colorUnable);
                btn2.setBackgroundColor(colorNormal);
            }break;
            case SEARCH:{
                boolBTN2=true;
                btn.setBackgroundColor(colorUnable);
                btn3.setBackgroundColor(colorWork);
                btn2.setBackgroundColor(colorNormal);
            }break;
            case CANCEL:{
                boolBTN1=true;
                boolBTN3=true;
                btn.setBackgroundColor(colorNormal);
                btn3.setBackgroundColor(colorNormal);
                btn2.setBackgroundColor(colorUnable);
            }
        }
        btn.setEnabled(boolBTN1);
        btn2.setEnabled(boolBTN2);
        btn3.setEnabled(boolBTN3);
    }
    private void MainActivityDialog(Context context,String msg) {
        new AlertDialog.Builder(context)
                .setTitle("Message")
                .setMessage(msg)
                .show();
    }
    //Listen to System Broadcast, if bluetooth turned off occurred other problem , stop Service
    void  receiveSystemBroad() {
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        myReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {
                    int nowStateBluetooth = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
                    if (nowStateBluetooth == BluetoothAdapter. STATE_OFF ) {
                        bluetoothProblemNotification("Bluetooth Turned Off!");
                        runCancelService();
                    }
                } else if (BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(intent.getAction())) {
                    int nowDiscoverableBluetooth = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.SCAN_MODE_NONE);
                    if (nowDiscoverableBluetooth == BluetoothAdapter.SCAN_MODE_CONNECTABLE) {
                        bluetoothProblemNotification("Bluetooth Is Not Discoverable!");
                        runCancelService();
                    }
                }
            }
        };
        registerReceiver(myReceiver, filter);
    }
    //If occur problem of Bluetooth state,notify user
    void  bluetoothProblemNotification(String errorText) {
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
        if(notifyManager!=null) {
            notifyManager.notify(2222, builder.build());
        }
    }
    //Refresh UI(ListView),  received complete message ,which found in method "ReceiveRadio.MsgBlueRadio.setMessageBody()"
    public class LocalBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if( LocalAction_RefreshUI.equals(intent.getAction())){
                Bundle bundle=intent.getExtras();
                if(bundle!=null) {
                    //TODO:Try BLE
                    String address = bundle.getString(ReceiveRadio_BLE.EXTRA_CONTENT_MESSAGE_ADDRESS);
                    String text = bundle.getString(ReceiveRadio_BLE.EXTRA_CONTENT_MESSAGE_TEXT);
                    Log.i("onReceive" , "onReceive: "+text);
                    myListViewManager.put(address,text);
                }
            }
            if(ServiceOnDestroy.equals(intent.getAction())){
                myButtonState=ButtonState.CANCEL;
                setButtonState(myButtonState);
            }
        }
    }
    private class MyListViewManager{
        private final static String str_address="Bluetooth_name";
        private final static String str_time="Bluetooth_time";
        private final static String str_text="Bluetooth_content";
        List<Map<String , String>> list;
        SimpleAdapter adapter;
        MyListViewManager(){
            this.list=new ArrayList<>();
            Map<String , String> map=new HashMap<>();
            map.put(str_address,"");
            map.put(str_text,"Empty");
            map.put(str_text,"");
            list.add(map);
            adapter=new SimpleAdapter(MainActivity.this,list , R.layout.my_listview,new String[]{str_address,str_time,str_text},
                    new int[]{R.id.Bluetooth_name,R.id.Bluetooth_time,R.id.Bluetooth_content});
        }
        void star() {
            myMainListView.setAdapter(adapter);
        }
        void put (String address,String text){
            Map<String , String> map=new HashMap<>();
            map.put(str_address,address);
            long systemTime = System.currentTimeMillis();
            String time =  new SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault()).format(new Date(systemTime));
            map.put(str_time,time);
            map.put(str_text,text);
            list.add(0,map);
            adapter.notifyDataSetChanged();
        }
    }
}
enum ButtonState{
    SEND,SEARCH,CANCEL
}






