package com.example.tablelight;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.HashMap;
import java.util.LinkedHashMap;

import com.iflytek.cloud.ErrorCode;

import com.iflytek.cloud.InitListener;

import com.iflytek.cloud.RecognizerListener;

import com.iflytek.cloud.RecognizerResult;

import com.iflytek.cloud.SpeechConstant;

import com.iflytek.cloud.SpeechError;

import com.iflytek.cloud.SpeechRecognizer;

import com.iflytek.cloud.SpeechUtility;

import com.iflytek.cloud.ui.RecognizerDialog;

import com.iflytek.cloud.ui.RecognizerDialogListener;

import com.iflytek.sunflower.FlowerCollector;



import org.json.JSONException;

import org.json.JSONObject;



import java.util.HashMap;

import java.util.LinkedHashMap;
import static android.widget.Toast.makeText;

/**
 * <p>
 * Title: Calculator.java<／p>
 * <p>
 * Description: <／p>
 * <p>
 * Copyright: Copyright (c) 2014<／p>
 *
 * @author Kevin Xu
 * @date Jan 7, 2014
 * @version 1.6
 */
//@SuppressLint("ShowToast")
public class MainActivity extends Activity {
    //	private TextView tv_show;
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_INPUT_MSG = 3;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device
    private int btState = STATE_NONE;
    private final static int REQUEST_CODE=1000;
    String cmd_name[] = new String[16];
    String cmd_content[] = new String[16];
    Button button[] = new Button[16];
    ToggleButton tog[] = new ToggleButton[16];
    int tog_flag[] = new int[16];
    TextView pos_text = null;
    TextView sudu_text = null;
    int tog_num = 0;
    int long_press_button_num = -1;
    SharedPreferences preference;
    SharedPreferences.Editor editor;
    private ToggleButton TbnBt;
    private Button btnBtSearch;
    //	private ImageButton yuying_button;
    private SeekBar mSeekBar1 = null;
    private SeekBar mSeekBar2 = null;
    //蓝牙相关
    private static BluetoothAdapter btAdapter = null;
    private static BluetoothDevice btDevice = null;
    private static BluetoothService btService = null;
    String message, inputMsgString="";
    byte[] outMsgBuffer, inputMsgBuffer;
    int msgLength, inputMsgLen = 0;
    private static String remoteDeviceAddress, remoteDeviceName;
    //定时器相关
    private Handler myHandler=null;
    private Runnable myRunnable=null;
    private static final int HANDLER_TIME = 1000;
    public static final String PREFER_NAME = "com.iflytek.setting";
    private static String TAG = "motor";
    private static String TAG1 = "action";

    private Context context;

    // 语音听写对象

    private SpeechRecognizer mIat;

    // 语音听写UI

    private RecognizerDialog mIatDialog;

    // 用HashMap存储听写结果

    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();



    private Toast mToast;



    private Button btStart,btStop,btCancel;

    private EditText etContent;

    private SharedPreferences mSharedPreferences;

    private int ret = 0; // 函数调用返回值


    /**

     * 初始化监听器。

     */

    private InitListener mInitListener = new InitListener() {



        @Override

        public void onInit(int code) {

            Log.d("SpeechRecognizer init() code = " + code,"1");

            if (code != ErrorCode.SUCCESS) {

                showTip("初始化失败，错误码：" + code);

            }

        }

    };


    // 引擎类型

    private String mEngineType = SpeechConstant.TYPE_CLOUD;

    Toast toast;

    // 引擎类型
    //private String mEngineType = SpeechConstant.TYPE_CLOUD;

    //读写权限 具体权限加在字符串里面
    //循环申请字符串数组里面的权限，在小米中是直接弹出一个权限框等待用户确认，确认一次既将上面数组里面的权限全部申请
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    //请求状态码
    private static int REQUEST_PERMISSION_CODE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //设置状态栏背景颜色
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().setStatusBarColor(Color.WHITE);
        // 设置状态栏字体黑色
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        //得到当前界面的装饰视图

            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
            }


        initData();

        findViewById();

        setOnclickListener();


        Button ledopen=findViewById(R.id.mainled1dakai);
        Button ledclose=findViewById(R.id.mainled1guanbi);
        Button automodel=findViewById(R.id.zidongbtn1);
        Button handmodel=findViewById(R.id.shoudongbtn1);
        ledopen.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                    SendDnCmd("a" );

            }
        });
        ledclose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                    SendDnCmd("b" );
            }
        });

        automodel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                SendDnCmd("d" );
            }
        });

        handmodel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                    SendDnCmd("c" );
            }
        });





        preference = getSharedPreferences("record", MODE_PRIVATE);
        editor = preference.edit();

        mToast = (Toast) makeText(this, "", Toast.LENGTH_SHORT);
        mSharedPreferences = getSharedPreferences(PREFER_NAME, Activity.MODE_PRIVATE);

        //蓝牙初始化
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        TbnBt = findViewById(R.id.TbnBt);
        if(btAdapter.getState() == BluetoothAdapter.STATE_ON){
            TbnBt.setChecked(true);
        }else {
            TbnBt.setChecked(false);
        }
        TbnBt.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO Auto-generated method stub
                if(TbnBt.isChecked()) {
                    if(btAdapter.getState() == BluetoothAdapter.STATE_OFF) {
                        btAdapter.enable();
                        //setUpBtServer();

                    }
                } else {
                    btAdapter.disable();
                    //destroyBtServer();
                }

            }
        });
        btnBtSearch = (Button)findViewById(R.id.button_scan);
        btnBtSearch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if(btAdapter.getState() == BluetoothAdapter.STATE_OFF) {
                    DisplayToast("蓝牙未打开！");//TODO
                } else {
                    Intent serverIntent = new Intent(MainActivity.this, DeviceListActivity.class);
                    startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
                }
            }
        });

    }








    @Override
    public void onStart() {
        super.onStart();

        if (btAdapter.isEnabled()) {
            if (btService == null) {
                setUpBtServer();
            }
        }
        //DisplayToast("onStart");
    }
    private void setUpBtServer() {
        if(btService == null){
            btService = new BluetoothService(this, btHandler);
        }
    }
    @Override
    protected synchronized void onResume() {
        super.onResume();

        //开启蓝牙服务
        if (btService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (btService.getState() == BluetoothService.STATE_NONE) {
                // Start the Bluetooth chat services
                btService.start();
            }
        } else {
            if (btAdapter.isEnabled()) {
                if (btService == null) {
                    setUpBtServer();
                    if (btService.getState() == BluetoothService.STATE_NONE) {
                        // Start the Bluetooth chat services
                        btService.start();
                    }
                }
            }
        }
        //DisplayToast("onResume");
    }
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        if(btService != null){
       //     destroyBtServer();
        }
        if( null != mIat ){

            // 退出时释放连接

            mIat.cancel();

            mIat.destroy();

        }
       // btAdapter.disable();
       // DisplayToast("onDestroy");
        super.onDestroy();
    }
    private void destroyBtServer() {
        if(btService != null){
            btService.stop();
            btService = null;
        }
    }
    private final Handler btHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    //if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            //DisplayToast("已连接");
                            btState = STATE_CONNECTED;
                            btnBtSearch.setText("已连接");
                            btnBtSearch.setTextColor(Color.GREEN);
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            //DisplayToast("正在连接。。。");
                            btState = STATE_CONNECTING;
                            btnBtSearch.setText("正在连接...");
                            btnBtSearch.setTextColor(Color.YELLOW);
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            //DisplayToast("无连接");
                            btState = STATE_NONE;
                            btnBtSearch.setText("无连接");
                            btnBtSearch.setTextColor(Color.RED);
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    //byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    //String writeMessage = new String(writeBuf);
                    //mConversationArrayAdapter.add("Me:  " + writeMessage);
                    //EdtDownCmd.setHint("send success!");

                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    inputMsgString += readMessage;
                    String temp = "";
                    if('>' == inputMsgString.charAt(inputMsgString.length()-1)) {
                        // if(inputMsgString.contains(">")) {
                        //DisplayToast("OK");
                        //DisplayToast(inputMsgString);
//                	if('<' == inputMsgString.charAt(0) && '.' == inputMsgString.charAt(3) ) {
//                		if(null != EdtUpCmd && null != ChkUpCmd && !ChkUpCmd.isChecked()){
//                			//EdtUpCmd.setText(inputMsgString);
//                			temp = inputMsgString.substring(1, 6) + " Mpa";
//                			DisplayUpCmd(temp);
//                		}
//                	}
//                	inputMsgString = "";
                    }
                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    //Toast.makeText(getApplicationContext(), "Connected to "
                    //              + remoteDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    //Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                    //               Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address

                    btAdapter.cancelDiscovery();
                    remoteDeviceName = intent.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

                    // Get the BLuetoothDevice object
                    remoteDeviceAddress = remoteDeviceName.substring(remoteDeviceName.length()-17);
                    remoteDeviceName = remoteDeviceName.substring(0, remoteDeviceName.length()-17);
                    btDevice = btAdapter.getRemoteDevice(remoteDeviceAddress);

                    // Attempt to connect to the device
                    setUpBtServer();
                    btService.connect(btDevice);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    //Bluetooth is now enabled, so set up a chat session
                    //setUpBtServer();

                } else {
                }
                break;

            case REQUEST_INPUT_MSG:
                break;
            case REQUEST_CODE:
                if (resultCode == 1000) {
                    //Bluetooth is now enabled, so set up a chat session
                    //setUpBtServer();
                    String str;
                    Bundle bundle=intent.getExtras();
                    if (bundle == null)
                        return;
                    String name =bundle.getString("cmd_name");
                    String content =bundle.getString("cmd_content");
                    if (name == null || content == null)
                        return;
                    cmd_name[long_press_button_num] = name;
                    str = "cmd_name[" + long_press_button_num + "]";
                    editor.putString(str, name);

                    cmd_content[long_press_button_num] = content;
                    button[long_press_button_num] .setText(name);
                    str = "cmd_content[" + long_press_button_num + "]";
                    editor.putString(str, content);
                    editor.commit();
                }
                break;
        }//end of switch
        //DisplayToast("onActivityResult");

    }


    //公用接口
    public void DisplayToast(String str){
        toast = Toast.makeText(getApplicationContext(),str,Toast.LENGTH_LONG);
        //toast.setGravity(Gravity.CENTER, 0, 0);


        toast.show();

    }
    public void SendDnCmd(String str) {
        if(btState == STATE_CONNECTED) {
            outMsgBuffer = (str + "\r\n").getBytes();
            btService.write(outMsgBuffer);

        } else {
            DisplayToast("蓝牙未连接！");
        }
        System.out.println(str+ "\r\n");
    }

    private void showTip(final String str) {
        toast = Toast.makeText(MainActivity.this,str,Toast.LENGTH_LONG);
        //toast.setGravity(Gravity.CENTER, 0, 0);


        toast.show();
    }



    private void setOnclickListener(){

        btStart.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View v) {

                checkSoIsInstallSucceed();

                etContent.setText(null);// 清空显示内容

                mIatResults.clear();

                // 设置参数

                setParam();

                boolean isShowDialog = mSharedPreferences.getBoolean(

                        getString(R.string.pref_key_iat_show), true);

                if (isShowDialog) {

                    // 显示听写对话框

                    mIatDialog.setListener(mRecognizerDialogListener);

                    mIatDialog.show();

                    showTip(getString(R.string.text_begin));

                } else {

                    // 不显示听写对话框

                    ret = mIat.startListening(mRecognizerListener);

                    if (ret != ErrorCode.SUCCESS) {

                        showTip("听写失败,错误码：" + ret);

                    } else {

                        showTip(getString(R.string.text_begin));

                    }

                }

            }

        });








    }



    private void findViewById(){

        btStart = (Button) findViewById(R.id.btn_start);



        etContent = (EditText) findViewById(R.id.et_content);

    }



    private void initData(){

        context = MainActivity.this;

        // 初始化识别无UI识别对象

        // 使用SpeechRecognizer对象，可根据回调消息自定义界面；

        mIat = SpeechRecognizer.createRecognizer(context, mInitListener);

        // 初始化听写Dialog，如果只使用有UI听写功能，无需创建SpeechRecognizer

        // 使用UI听写功能，请根据sdk文件目录下的notice.txt,放置布局文件和图片资源

        mIatDialog = new RecognizerDialog(context, mInitListener);

        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);

        mSharedPreferences = getSharedPreferences(MainActivity.PREFER_NAME,

                Activity.MODE_PRIVATE);

    }



    private void checkSoIsInstallSucceed(){

        if( null == mIat ){

            // 创建单例失败，与 21001 错误为同样原因，参考 http://bbs.xfyun.cn/forum.php?mod=viewthread&tid=9688

            this.showTip( "创建对象失败，请确认 libmsc.so 放置正确，且有调用 createUtility 进行初始化" );

            return;

        }

    }







    /**

     * 参数设置

     *

     * @param

     * @return

     */

    public void setParam() {

        // 清空参数

        mIat.setParameter(SpeechConstant.PARAMS, null);



        // 设置听写引擎

        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);

        // 设置返回结果格式

        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");



        String lag = mSharedPreferences.getString("iat_language_preference",

                "mandarin");

        if (lag.equals("en_us")) {

            // 设置语言

            mIat.setParameter(SpeechConstant.LANGUAGE, "en_us");

        } else {

            // 设置语言

            mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");

            // 设置语言区域

            mIat.setParameter(SpeechConstant.ACCENT, lag);

        }



        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理

        mIat.setParameter(SpeechConstant.VAD_BOS, mSharedPreferences.getString("iat_vadbos_preference", "4000"));



        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音

        mIat.setParameter(SpeechConstant.VAD_EOS, mSharedPreferences.getString("iat_vadeos_preference", "1000"));



        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点

        mIat.setParameter(SpeechConstant.ASR_PTT, mSharedPreferences.getString("iat_punc_preference", "0"));



        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限

        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效

        mIat.setParameter(SpeechConstant.AUDIO_FORMAT,"wav");

        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/iat.wav");

    }



    /**

     * 听写UI监听器

     */

    private RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener() {

        public void onResult(RecognizerResult results, boolean isLast) {

            printResult(results);

        }



        /**

         * 识别回调错误.

         */

        public void onError(SpeechError error) {

            showTip(error.getPlainDescription(true));

        }



    };







    /**

     * 听写监听器。

     */

    private RecognizerListener mRecognizerListener = new RecognizerListener() {



        @Override

        public void onBeginOfSpeech() {

            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入

            showTip("开始说话");

        }



        @Override

        public void onError(SpeechError error) {

            // Tips：

            // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。

            // 如果使用本地功能（语记）需要提示用户开启语记的录音权限。

            showTip(error.getPlainDescription(true));

        }



        @Override

        public void onEndOfSpeech() {

            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入

            showTip("结束说话");

        }



        @Override

        public void onResult(RecognizerResult results, boolean isLast) {

            Log.d(results.getResultString(),"2");

            printResult(results);



            if (isLast) {

                // TODO 最后的结果

            }

        }



        @Override

        public void onVolumeChanged(int volume, byte[] data) {

            showTip("当前正在说话，音量大小：" + volume);

            Log.d("返回音频数据："+data.length,"3");

        }



        @Override

        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {

            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因

            // 若使用本地能力，会话id为null

            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {

            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);

            //		Log.d(TAG, "session id =" + sid);

            //	}

        }

    };



    private void printResult(RecognizerResult results) {
        Log.i(TAG1, "执行printresult");

        String text = JsonParser.parseIatResult(results.getResultString());



        String sn = null;

        // 读取json结果中的sn字段

        try {

            JSONObject resultJson = new JSONObject(results.getResultString());

            sn = resultJson.optString("sn");

        } catch (JSONException e) {

            e.printStackTrace();

        }



        mIatResults.put(sn, text);



        StringBuffer resultBuffer = new StringBuffer();

        for (String key : mIatResults.keySet()) {

            resultBuffer.append(mIatResults.get(key));

        }
        String temp;
        temp = resultBuffer.toString();
        //showTip(temp);

        Log.d(TAG1, "接收到：" + temp);
        System.out.println("开始发送数据"+ "\r\n");


        if (temp.contains("开灯")) {
            SendDnCmd("a" );
            delay(1000);
            showTip("已开灯");


        }else if (temp.contains("关灯")) {

            showTip("已关灯");
            SendDnCmd("b" );


        } else if (temp.contains("自动模式")) {

            showTip("已选择自动模式");
            SendDnCmd("d" );


        } else if (temp.contains("手动模式")) {

            showTip("已选择手动模式");
            SendDnCmd("c" );


        }


       // etContent.setText(resultBuffer.toString());


       // etContent.setSelection(etContent.length());

    }
    private void delay(int ms){

        try {

            Thread.currentThread();

            Thread.sleep(ms);

        } catch (InterruptedException e) {

            e.printStackTrace();

        }

    }







}
