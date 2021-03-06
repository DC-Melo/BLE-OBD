package zk.obd.ble;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import zk.obd.R;

//test main
//second
public class Ble_starActivity extends Activity {
    int index=0;
    int messagecount=1;
    int messagenumber;
    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.e("---------","------msg");
            if(msg.what==1)
            {
                if(index==0)
                    return;
                sendBleMessage(index-1);
            }
        }
    };

    Thread autoSendThread = null;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private static final int UART_PROFILE_CONNECTED = 20;
    private int mState = UART_PROFILE_DISCONNECTED;

    private ArrayAdapter<String> listAdapter;
    private BluetoothDevice mDevice = null;
    private  static UartService mService = null;
    private long sendValueNum = 0;
    private long recValueNum = 0;
    /*private int[] config={
            0x6B4,11,	0,0,0,	0,0,0,//vin
            0x6B7,1,	1,6,10,	7,0,1,//KBI_Inhalt_Tank
            0x6B7,1,	1,1,0,	20,0,1};//KBI_Kilometerstand
    public int functionNum = 0;*/

    List<Signal> messageList=new ArrayList<>();
    Signal message1=new Signal();
    Signal message2=new Signal();
    Signal message3=new Signal();
    Signal message4=new Signal();
    Signal message5=new Signal();

    private Spinner spinnerInterval;
    // ??????
    TextView tv_car_start_check;
    RelativeLayout rl_score_info;
    FrameLayout frameLayout;
    ImageView rightCheckLight;
    ImageView leftCheckLight;
    TranslateAnimation right_translateAnimation;
    TranslateAnimation translateAnimationLeft;
    ImageView carCheckLightBody;
    AnimationDrawable animationDrawableBody;
    ImageView dialog_wharp;
    ImageView dialog_semi_circle;
    ImageView dialog_semi_circle_smill;
    Animation animation_rotate_dialog;
    Animation animation_rotate2_dialog;
    Animation animation_rotate3_dialog;
    FrameLayout dialog_fragment;

    TextView mileage_number;
    TextView oil_number;
    TextView car_number;
    TextView voltage_number;
    TextView vin_number;
    TextView ble_State;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.car_check);
        initView();
        initDate();
        initAnimation();
        initClick();
        listAdapter = new ArrayAdapter<String>(this, R.layout.ble_message_detail);
        Init_service();// ?????????????????????

        if(ContextCompat.checkSelfPermission(Ble_starActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){//?????????????????????
            //??????????????????,200????????????
            Toast.makeText(Ble_starActivity.this,"??????app??????????????????",Toast.LENGTH_LONG).show();

            ActivityCompat.requestPermissions(Ble_starActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},200);
        }else{
            Toast.makeText(Ble_starActivity.this,"?????????????????????",Toast.LENGTH_LONG).show();
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //tv_car_start_check.setText("??????");
        dialog_fragment.setVisibility(View.GONE);
        switch (requestCode) {
            case REQUEST_SELECT_DEVICE:
                // ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                if (resultCode == Activity.RESULT_OK && data != null) {
                    String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
                    mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);
                    boolean isconnected = mService.connect(deviceAddress);
                }
                break;
            case REQUEST_ENABLE_BT:
                // ??????????????????????????????????????????????????????????????????
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "????????????????????????", Toast.LENGTH_SHORT).show();
                } else {
                    // ??????????????????????????????????????????????????????????????????????????????
                    // Log.d(TAG, "???????????????");
                    System.out.println("???????????????");
                    Toast.makeText(this, "???????????????????????????", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                System.out.println("wrong request code");
                break;
        }
    }

    private void Init_service() {
        System.out.println("Init_service");
        Intent bindIntent = new Intent(this, UartService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver,
                makeGattUpdateIntentFilter());
    }

    // UART service connected/disconnected
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        // ???UART?????????????????????
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((UartService.LocalBinder) rawBinder).getService();
            System.out.println("uart???????????????" + mService);
            if (!mService.initialize()) {
                System.out.println("???????????????????????????");
                // ????????????????????????????????????????????????????????????????????????????????????????????????uart??????
                finish();
            }
        }

        // ???UART?????????????????????
        public void onServiceDisconnected(ComponentName classname) {
            // mService.disconnect(mDevice);
            mService = null;
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UartService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(UartService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(UartService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(UartService.DEVICE_DOES_NOT_SUPPORT_UART);
        return intentFilter;
    }

    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            final Intent mIntent = intent;
            // ????????????
            if (action.equals(UartService.ACTION_GATT_CONNECTED)) {
                System.out.println("BroadcastReceiver:ACTION_GATT_CONNECTED");
                ble_State.setText("???????????????");
                tv_car_start_check.setText("??????");
                toastMessage("??????"+ mDevice.getName()+"????????????");
                String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                //btnStart.setEnabled(true);
                listAdapter.add("[" + currentDateTimeString + "] ????????????: " + mDevice.getName());
                mState = UART_PROFILE_CONNECTED;
            }
            // ????????????
            if (action.equals(UartService.ACTION_GATT_DISCONNECTED)) {
                System.out.println("BroadcastReceiver:ACTION_GATT_DISCONNECTED");
                ble_State.setText("???????????????");
                String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                ble_State.setText("??????");
                tv_car_start_check.setText("??????");
                toastMessage("????????????"+ mDevice.getName()+"??????????????????");
                listAdapter.add("[" + currentDateTimeString + "] ????????????: " + mDevice.getName());
                mState = UART_PROFILE_DISCONNECTED;
                mService.close();
            }
            // ?????????????????????
            if ((action.equals(UartService.ACTION_DATA_AVAILABLE))) {
                byte[] rxValue = intent.getByteArrayExtra(UartService.EXTRA_DATA);
                messagecount=(int) rxValue[5]& 0xff;
                if(messagecount<1)messagecount=1;
                String Rx_str =Utils.bytesToHexString(rxValue) ;
                String s_value;
                if (messagecount <= messagenumber ){
                    messageList.get(index).setCanRX(messagecount,rxValue);
                }

                if (messageList.get(index).getSignaltype()=="ASC"){
                     s_value=messageList.get(index).getVIN();
                }else{
                    double value=messageList.get(index).getSignalvalue();
                    s_value=String.valueOf(value)+messageList.get(index).getUnit();
                }


                // ????????????
                switch (index)
                {

//                    TextView mileage_number;
//                    TextView oil_number;
//                    TextView car_number;
//                    TextView voltage_number;
//                    TextView vin_number;
//                    TextView ble_State;
                    case 0:

                        settingDrawableTop(getApplicationContext(),vin_number,R.mipmap.car_check_scan_right,s_value,null);
                    break;
                    case 1:
                        settingDrawableTop(getApplicationContext(),mileage_number,R.mipmap.car_check_scan_right,s_value,null);
                        break;
                    case 2:
                        settingDrawableTop(getApplicationContext(),oil_number,R.mipmap.car_check_scan_right,s_value,null);
                        break;
                    case 3:
                        settingDrawableTop(getApplicationContext(),voltage_number,R.mipmap.car_check_scan_right,s_value,null);
                        break;
                    case 4:
                        settingDrawableTop(getApplicationContext(),car_number,R.mipmap.car_check_scan_right,s_value,null);
                        break;

                }
                if(messageList!=null&&index<5)
                {

                    handler.sendEmptyMessageAtTime(1,5000);
                }

                if(messagenumber==messagecount)
                    index++;
                if(index>4 )  index=0;

                Log.e("----------",""+Rx_str);
//                listAdapter.add("[" + DateFormat.getTimeInstance().format(new Date()) + "] ??????0x: " + );
            }
            // ????????????1Rx_str
            if (action.equals(UartService.ACTION_GATT_SERVICES_DISCOVERED)) {
                mService.enableTXNotification();
            }
            // ????????????2
            if (action.equals(UartService.DEVICE_DOES_NOT_SUPPORT_UART)) {
                toastMessage("????????????????????????????????????");
                mService.disconnect();
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("onDestroy");
        try {
            // ????????????????????????
            LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiver);
        } catch (Exception ignore) {
            System.out.println(ignore.toString());
        }
        // ???????????????
        unbindService(mServiceConnection);
        // ??????????????????
        mService.stopSelf();
        mService = null;
    }

    private void toastMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        System.out.println("???MainActivity????????????back???");
    }

    //?????????????????????????????????
    public void checkBleIsConnectPermission()
    {
                if(null==mService)
                    return;
        byte[] bytes=message1.getCanTX();
        messagenumber=(int) bytes[5];
//        String s1=Utils.bytesToHexString(bytes);
        mService.writeRXCharacteristic(bytes);

//          if (mService.mConnectionState==2){
//
//                byte[] bytes=message1.getCanTX();
//                String s1=Utils.bytesToHexString(bytes);
//                mService.writeRXCharacteristic(bytes);
//                try {
//                    listAdapter.add("[" + DateFormat.getTimeInstance().format(new Date()) + "] ??????0x: " +s1);
//                } catch (Exception e) {
//                    System.out.println(e.toString());
//                }
//        }

    }
    //
    //?????????????????????????????????
    public void sendBleMessage(int i)
    {
        Log.e("-----","----zou??????");
        if(null==mService)
            return;
        Log.e("-----","----fasong");
        byte[] bytes=messageList.get(i).getCanTX();
        Log.e("-----","----fasong2");
        String s1=Utils.bytesToHexString(bytes);
        mService.writeRXCharacteristic(bytes);
        Log.e("-----","----fasong3");

//          if (mService.mConnectionState==2){
//
//                byte[] bytes=message1.getCanTX();
//                String s1=Utils.bytesToHexString(bytes);
//                mService.writeRXCharacteristic(bytes);
//                try {
//                    listAdapter.add("[" + DateFormat.getTimeInstance().format(new Date()) + "] ??????0x: " +s1);
//                } catch (Exception e) {
//                    System.out.println(e.toString());
//                }
//        }

    }

    //?????????????????????
    private void initView() {
        tv_car_start_check=findViewById(R.id.tv_car_start_check);
        rl_score_info = findViewById(R.id.rl_score_info);
        frameLayout = findViewById(R.id.dialog);
        carCheckLightBody = findViewById(R.id.iv_control_normal_bg);
        leftCheckLight = (ImageView) findViewById(R.id.iv_scan_left);
        rightCheckLight = (ImageView) findViewById(R.id.iv_scan_right);
        //dialog
        dialog_fragment = findViewById(R.id.dialog);
        dialog_wharp = (ImageView) findViewById(R.id.loding_wharp);
        dialog_semi_circle = (ImageView) findViewById(R.id.loding_b);
        dialog_semi_circle_smill = (ImageView) findViewById(R.id.loding_1);
        //??????
        mileage_number= findViewById(R.id.mileage_txt_number);
        oil_number= findViewById(R.id.car_oil_txt_number);
        car_number=findViewById(R.id.car_txt_modle);
        voltage_number=findViewById(R.id.voltage_txt_number);
        vin_number=findViewById(R.id.vin_txt_number);
        ble_State=findViewById(R.id.lanya);
    }

    public void initAnimation() {

        //?????????
        translateAnimationLeft = (TranslateAnimation) AnimationUtils.loadAnimation(this, R.anim.anim_guang_shua_left);
        //?????????
        right_translateAnimation = (TranslateAnimation) AnimationUtils.loadAnimation(this, R.anim.anim_guang_shua_right);
        //dialog
        animation_rotate_dialog = AnimationUtils.loadAnimation(this, R.anim.zheng_rotate_anim);
        animation_rotate2_dialog = AnimationUtils.loadAnimation(this, R.anim.fan_rotate_anim);
        animation_rotate3_dialog = AnimationUtils.loadAnimation(this, R.anim.da_fan_rotate_anim);
    }

    public void startAnimation() {
        leftCheckLight.setVisibility(View.VISIBLE);
        //????????????
        carCheckLightBody.setImageResource(R.drawable.car_check_light_body);
        animationDrawableBody = (AnimationDrawable) carCheckLightBody.getDrawable();
        animationDrawableBody.start();
        //?????????
        leftCheckLight.startAnimation(translateAnimationLeft);


    }

    public void initClick() {
        right_translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {


                rightCheckLight.clearAnimation();
                rightCheckLight.setVisibility(View.GONE);

                leftCheckLight.setVisibility(View.VISIBLE);
                leftCheckLight.setAnimation(translateAnimationLeft);
            }
        });

        translateAnimationLeft.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {


            }

            @Override
            public void onAnimationEnd(Animation animation) {


            }

            @Override
            public void onAnimationRepeat(Animation animation) {

                leftCheckLight.clearAnimation();
                leftCheckLight.setVisibility(View.GONE);

                rightCheckLight.setVisibility(View.VISIBLE);
                rightCheckLight.setAnimation(right_translateAnimation);
            }
        });

        //??????
        rl_score_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //?????????????????????
                dialog_fragment.setVisibility(View.VISIBLE);
                //??????


                showDialog();
                cheCkAndStart();
            }
        });
    }

    public void showDialog() {
        dialog_fragment.setVisibility(View.VISIBLE);
        //????????????
        dialog_wharp.setAnimation(animation_rotate3_dialog);
        dialog_semi_circle.setAnimation(animation_rotate2_dialog);
        dialog_semi_circle_smill.setAnimation(animation_rotate_dialog);


    }

    public void initDate()
    {

        message1.setCanTX(new byte[] {0x41,0x50,0x50,0x06,(byte) 0xb4,0x4,(byte) 0xcc,(byte) 0xcc,0xa,0xd});
        message1.setSignaltype("ASC");
        message1.setStartbyte(2);
        message1.setLength(7);
        message2.setCanTX(new byte[] {0x41,0x50,0x50,0x06,(byte) 0xb7,0x1,(byte) 0xcc,(byte) 0xcc,0xa,0xd});
        message2.setStartbyte(2);
        message2.setStartbit(0);
        message2.setLength(20);
        message2.setUnit("km");
        message2.setOffset(0);
        message2.setFactor(1);
        message3.setCanTX(new byte[] {0x41,0x50,0x50,0x03,(byte) 0xb7,0x1,(byte) 0xcc,(byte) 0xcc,0xa,0xd});
        message3.setStartbyte(6);
        message3.setStartbit(0);
        message3.setLength(8);
        message3.setUnit("L");
        message3.setOffset(5);
        message3.setFactor(0.05);
        message4.setCanTX(new byte[] {0x41,0x50,0x50,0x04,(byte) 0xb7,0x1,(byte) 0xcc,(byte) 0xcc,0xa,0xd});
        message4.setStartbyte(6);
        message4.setStartbit(0);
        message4.setLength(8);
        message4.setUnit("V");
        message4.setOffset(5);
        message4.setFactor(0.05);
        message5.setCanTX(new byte[] {0x41,0x50,0x50,0x05,(byte) 0xb7,0x1,(byte) 0xcc,(byte) 0xcc,0xa,0xd});
        message5.setStartbyte(6);
        message5.setStartbit(0);
        message5.setLength(8);
        message5.setUnit("km");
        message5.setOffset(5);
        message5.setFactor(0.05);
        messageList.add(message1);
        messageList.add(message2);
        messageList.add(message3);
        messageList.add(message4);
        messageList.add(message5);
        mileage_number.setText("");
        oil_number.setText("");
        car_number.setText("");
        voltage_number.setText("");
        vin_number.setText("");
        //??????????????????????????????
        //settingDrawableTop(getApplicationContext(),vin_number,R.mipmap.car_check_scan_right,"",new Object());


    }

    public  void settingDrawableTop(Context context, TextView view , int resourcesDrawable, String  resourcesString,Object o) {
        view.setVisibility(View.VISIBLE);
        Drawable drawable = null;
        drawable = context.getResources().getDrawable(resourcesDrawable);
        if (o != null) {
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight()); //????????????
            view.setCompoundDrawables(null, null, drawable, null);//????????????
            view.setText(resourcesString);
        }else
        {

            view.setCompoundDrawables(null, null, null, null);//????????????
            view.setText(resourcesString);

        }
    }

    //????????????
    public void cheCkAndStart()
    {
        // ?????????????????????????????????
        BluetoothAdapter  mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        // ?????????????????????????????????????????????????????????????????????
        if (!mBtAdapter.isEnabled()) {
            toastMessage("?????????????????????????????????");
            System.out.println("?????????????????????");
            // ?????????????????????????????????
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            // ????????????????????????????????????????????????????????????
            if(tv_car_start_check.getText().equals("??????")) {

                Intent newIntent = new Intent(Ble_starActivity.this, DeviceListActivity.class);
                startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);
            }else if(tv_car_start_check.getText().equals("??????"))
            {
                dialog_fragment.setVisibility(View.GONE);
                checkBleIsConnectPermission();
                startAnimation();
            }

        }
    }
}
