package com.example.st_for_test;

import android.Manifest;
import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.ui.RecognizerDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Objects;

public class FloatViewService extends AccessibilityService {

    public static boolean isStarted = false;

    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;

    private View displayView;

    private static final String TAG = "FloatService";

    private SpeechRecognizer mIat;// ??????????????????

    // ???HashMap??????????????????
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();

    private SharedPreferences mSharedPreferences;//??????

    private String mEngineType = SpeechConstant.TYPE_CLOUD;// ????????????
    private String language = "zh_cn";//????????????
    private String resultType = "json";//????????????????????????

    // basic page
    private Button basic;

    // load info page
    private TextView des1;
    private TextView des2;
    private RadioGroup setting_operation;
    private RadioButton radio_set_true;
    private RadioButton radio_set_false;
    private Button close;
    private Button user_start;

    // recognize page
    private TextView title_1;
    private Button float_btn_start;
    private TextView float_tv_result;
    private TextView not_right_info;
    private Button next_step;
    private Button search_answer;

    // recognize answer page
    private TextView title_2;
    private TextView des3;
    private TextView des4;
    private TextView xun_fei_1;
    private Button xun_fei_choose_1;
    private TextView xun_fei_2;
    private Button xun_fei_choose_2;
    private TextView xun_fei_plus_1;
    private Button xun_fei_plus_choose_1;
    private TextView xun_fei_plus_2;
    private Button xun_fei_plus_choose_2;
    private TextView ST_1;
    private Button ST_choose_1;
    private TextView ST_2;
    private Button ST_choose_2;
    private Button not_correct;

    //save page
    private TextView user;
    private EditText user_input;
    private TextView job;
    private EditText job_input;
    private Button save_info;
    private Button close_after;
    private Button next_group;

    // related info
    private String presentApp;
    private String presentComponent;
    private int num, nNum;
    private int index;
    private boolean operation_state;
    private boolean send_1_first;
    private boolean send_2_first;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        AccessibilityServiceInfo config = new AccessibilityServiceInfo();
        //??????????????????????????????????????????|????????????
        config.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED | AccessibilityEvent.TYPE_VIEW_CLICKED;
        config.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        if (Build.VERSION.SDK_INT >= 16) {
            config.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;
        }
        setServiceInfo(config);

    }

    @Override
    public void onCreate() {
        super.onCreate();
        isStarted = true;
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        layoutParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
//        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL ;
//        layoutParams.width = 800;
        layoutParams.width = 300;
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        layoutParams.x = 100;
        layoutParams.y = 300;
        num = 0;
        nNum = 0;
        index = 0;
        send_1_first = true;
        send_2_first = true;
        presentApp = "";
        presentComponent = "";
        showFloatingWindow();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        AccessibilityNodeInfo nodeInfo = event.getSource();//????????????????????????????????????
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {//??????????????????
            Log.i(TAG,"changed!");
            String temp = event.getPackageName().toString();
            Log.i(TAG,"??????Activity??????"+temp);
            if(temp.equals("com.tencent.mm")){
                num = 0;
                presentApp = "??????";
                Log.i(TAG,"??????App??????"+presentApp);
            }
            else if(temp.equals("com.baidu.BaiduMap")){
                num = 0;
                presentApp = "????????????";
                Log.i(TAG,"??????APP??????"+presentApp);
            }
            else {
                if(num<3){
                    num = num + 1;
                }
                else {
                    presentApp = "";
                    num = 0;
                    Log.i(TAG,"??????????????????App???");
                }
            }
        }
        if(event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            String text = "";
            String contentDescription = "";
            String viewIdResourceName = "";
            String className = "";
            try {
                if(nodeInfo.getText() == null){
                    text = "";
                } else {
                    text = nodeInfo.getText().toString();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (nodeInfo.getContentDescription() == null){
                    contentDescription = "";
                } else {
                    contentDescription = nodeInfo.getContentDescription().toString();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    if (nodeInfo.getViewIdResourceName() == null) {
                        viewIdResourceName = "";
                    } else {
                        viewIdResourceName = nodeInfo.getViewIdResourceName();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if(nodeInfo.getClassName() == null){
                    className = "";
                } else {
                    className = nodeInfo.getClassName().toString();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.i(TAG, "onAccessibilityEvent: " + text);
            Log.i(TAG,"contentDescription: " + contentDescription);
            Log.i(TAG,"viewIdResourceName: " + viewIdResourceName);
            Log.i(TAG, "className: " + className);
            Log.i(TAG,"new");
            if (presentApp.equals("??????") && className.equals("android.widget.EditText")){
                presentComponent = "?????????";
                nNum = 0;
            }
            else if (presentApp.equals("??????") && className.equals("android.widget.TextView")){
                presentComponent = "?????????";
                nNum = 0;
            }
            else if (presentApp.equals("??????") && className.equals("android.widget.FrameLayout")){
                presentComponent = "????????????";
                nNum = 0;
            }
            else {
                if(nNum<8){
                    nNum = nNum + 1;
                }
                else {
                    presentComponent = "";
                    nNum = 0;
                    Log.i(TAG,"???????????????????????????");
                }
            }
        }
    }

    private ActivityInfo tryGetActivity(ComponentName componentName) {
        try {
            return getPackageManager().getActivityInfo(componentName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    @Override
    public void onInterrupt() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        showFloatingWindow();
        return super.onStartCommand(intent, flags, startId);
    }

    private void showFloatingWindow() {
        if (Settings.canDrawOverlays(this)) {
            LayoutInflater layoutInflater = LayoutInflater.from(this);
            displayView = layoutInflater.inflate(R.layout.float_window, null);
            displayView.setOnTouchListener(new FloatingOnTouchListener());

            basic = displayView.findViewById(R.id.basic);
            basic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                    basic.setVisibility(View.GONE);
                    des1.setVisibility(View.VISIBLE);
                    des2.setVisibility(View.VISIBLE);
                    setting_operation.setVisibility(View.VISIBLE);
                    radio_set_true.setVisibility(View.VISIBLE);
                    radio_set_false.setVisibility(View.VISIBLE);
                    close.setVisibility(View.VISIBLE);
                    user_start.setVisibility(View.VISIBLE);
                    layoutParams.width = 1000;
                }
            });

            des1 = displayView.findViewById(R.id.des1);
            des1.setVisibility(View.GONE);
            des2 = displayView.findViewById(R.id.des2);
            des2.setVisibility(View.GONE);
            setting_operation = displayView.findViewById(R.id.setting_operation);
            setting_operation.setVisibility(View.GONE);
            radio_set_true = displayView.findViewById(R.id.radio_set_true);
            radio_set_true.setVisibility(View.GONE);
            radio_set_false = displayView.findViewById(R.id.radio_set_false);
            radio_set_false.setVisibility(View.GONE);
            setting_operation.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int i) {
                    switch (i){
                        case R.id.radio_set_true:
                            operation_state = true;
                            break;
                        case R.id.radio_set_false:
                            operation_state = false;
                            break;
                    }
                }
            });
            setting_operation.check(R.id.radio_set_true);
            close = displayView.findViewById(R.id.close);
            close.setVisibility(View.GONE);
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    reset_page();
                }
            });
            user_start =displayView.findViewById(R.id.user_start);
            user_start.setVisibility(View.GONE);
            user_start.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String[] result = {""};
                    HttpURLConnection connection = null;
                    try {
                        String str_id = String.valueOf(index);
                        String str_operation = "True";
                        if(!operation_state){
                            str_operation = "False";
                        }
                        URL url = new URL("http://47.92.159.130:8080/set_basic_info?id="+str_id+"&operation="+str_operation);
                        connection = (HttpURLConnection) url.openConnection();
                        connection.setConnectTimeout(3000);
                        connection.setReadTimeout(3000);
                        //?????????????????? GET / POST ???????????????
                        connection.setRequestMethod("GET");
                        connection.setDoInput(true);
                        connection.setDoOutput(false);
                        connection.connect();
                        int responseCode = connection.getResponseCode();
                        if (responseCode != HttpURLConnection.HTTP_OK) {
                            throw new IOException("HTTP error code" + responseCode);
                        }
                        result[0] = getStringByStream(connection.getInputStream());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if(result[0].equals("Done")){
                        // load info page
                        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                        des1.setVisibility(View.GONE);
                        des2.setVisibility(View.GONE);
                        user.setVisibility(View.GONE);
                        user_input.setVisibility(View.GONE);
                        job.setVisibility(View.GONE);
                        job_input.setVisibility(View.GONE);
                        setting_operation.setVisibility(View.GONE);
                        radio_set_true.setVisibility(View.GONE);
                        radio_set_false.setVisibility(View.GONE);
                        close.setVisibility(View.GONE);
                        user_start.setVisibility(View.GONE);

                        // recognize page
                        title_1.setVisibility(View.VISIBLE);
                        float_btn_start.setVisibility(View.VISIBLE);
                        float_tv_result.setVisibility(View.VISIBLE);
                        float_tv_result.setText("??????????????????");
                        title_1.setText("??????");

                        send_1_first = true;
                        send_2_first = true;

                        Toast.makeText(getApplicationContext(), "??????????????????", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "??????????????????", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            title_1 = displayView.findViewById(R.id.title_1);
            title_1.setVisibility(View.GONE);
            float_btn_start = displayView.findViewById(R.id.float_btn_start);
            float_btn_start.setVisibility(View.GONE);
            float_btn_start.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if( null == mIat ){
                        // ???????????????????????? 21001 ?????????????????????????????? http://bbs.xfyun.cn/forum.php?mod=viewthread&tid=9688
                        showMsg( "?????????????????????????????? libmsc.so ??????????????????????????? createUtility ???????????????" );
                        return;
                    }

                    mIatResults.clear();//????????????
                    setParam(); // ????????????
                    mIat.startListening(mRecoListener);
                    send_1_first = true;
                    send_2_first = true;
                    not_right_info.setVisibility(View.GONE);
                    float_tv_result.setText("??????????????????");
                }
            });
            float_tv_result = displayView.findViewById(R.id.float_tv_result);
            float_tv_result.setVisibility(View.GONE);
            not_right_info = displayView.findViewById(R.id.not_right_info);
            not_right_info.setVisibility(View.GONE);
            next_step = displayView.findViewById(R.id.next_step);
            next_step.setVisibility(View.GONE);
            next_step.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String[] result = {""};
                    HttpURLConnection connection = null;
                    try {
                        String str_id = String.valueOf(index);
                        String str_first_des = float_tv_result.getText().toString();
                        URL url = new URL("http://47.92.159.130:8080/set_first_des?id="+str_id+"&first_des="+str_first_des);
                        connection = (HttpURLConnection) url.openConnection();
                        connection.setConnectTimeout(3000);
                        connection.setReadTimeout(3000);
                        //?????????????????? GET / POST ???????????????
                        connection.setRequestMethod("GET");
                        connection.setDoInput(true);
                        connection.setDoOutput(false);
                        connection.connect();
                        int responseCode = connection.getResponseCode();
                        if (responseCode != HttpURLConnection.HTTP_OK) {
                            throw new IOException("HTTP error code" + responseCode);
                        }
                        result[0] = getStringByStream(connection.getInputStream());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if(result[0].equals("Done")){
                        title_1.setText("ST");
                        float_tv_result.setText("??????????????????");
                        next_step.setVisibility(View.GONE);
                        not_right_info.setVisibility(View.GONE);
                        send_1_first = true;
                        send_2_first = true;
                        Toast.makeText(getApplicationContext(), "??????????????????", Toast.LENGTH_SHORT).show();
                    } else if(result[0].equals("Again")){
                        if (send_1_first){
                            not_right_info.setVisibility(View.VISIBLE);
                            send_1_first = false;
                        } else {
                            title_1.setText("ST");
                            float_tv_result.setText("??????????????????");
                            next_step.setVisibility(View.GONE);
                            not_right_info.setVisibility(View.GONE);
                            send_1_first = true;
                            send_2_first = true;
                        }
                        Toast.makeText(getApplicationContext(), "??????????????????", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "??????????????????", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            search_answer = displayView.findViewById(R.id.search_answer);
            search_answer.setVisibility(View.GONE);
            search_answer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String[] result = {"",""};
                    String str_id = String.valueOf(index);
                    HttpURLConnection connection = null;
                    try {
                        String str_second_des = float_tv_result.getText().toString();
                        URL url = new URL("http://47.92.159.130:8080/set_second_des?id="+str_id+"&second_des="+str_second_des+"&app="+presentApp+"&component="+presentComponent);
                        connection = (HttpURLConnection) url.openConnection();
                        connection.setConnectTimeout(3000);
                        connection.setReadTimeout(3000);
                        //?????????????????? GET / POST ???????????????
                        connection.setRequestMethod("GET");
                        connection.setDoInput(true);
                        connection.setDoOutput(false);
                        connection.connect();
                        int responseCode = connection.getResponseCode();
                        if (responseCode != HttpURLConnection.HTTP_OK) {
                            throw new IOException("HTTP error code" + responseCode);
                        }
                        result[0] = getStringByStream(connection.getInputStream());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if(result[0].equals("Done")){
                        // recognize page
                        title_1.setVisibility(View.GONE);
                        float_btn_start.setVisibility(View.GONE);
                        float_tv_result.setVisibility(View.GONE);
                        not_right_info.setVisibility(View.GONE);
                        next_step.setVisibility(View.GONE);
                        search_answer.setVisibility(View.GONE);


                        // recognize answer page
                        title_2.setVisibility(View.VISIBLE);
                        des3.setVisibility(View.VISIBLE);
                        des4.setVisibility(View.VISIBLE);
                        xun_fei_1.setVisibility(View.VISIBLE);
                        xun_fei_choose_1.setVisibility(View.VISIBLE);
                        xun_fei_2.setVisibility(View.VISIBLE);
                        xun_fei_choose_2.setVisibility(View.VISIBLE);
                        not_correct.setVisibility(View.VISIBLE);
                        title_2.setText("??????");
                        send_1_first = true;
                        send_2_first = true;
                        try {
                            URL url = new URL("http://47.92.159.130:8080/get_xunfei_answer?id="+str_id);
                            connection = (HttpURLConnection) url.openConnection();
                            connection.setConnectTimeout(3000);
                            connection.setReadTimeout(3000);
                            //?????????????????? GET / POST ???????????????
                            connection.setRequestMethod("GET");
                            connection.setDoInput(true);
                            connection.setDoOutput(false);
                            connection.connect();
                            int responseCode = connection.getResponseCode();
                            if (responseCode != HttpURLConnection.HTTP_OK) {
                                throw new IOException("HTTP error code" + responseCode);
                            }
                            result[1] = getStringByStream(connection.getInputStream());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        String[] separated = result[1].split("&");
                        if(separated[0].equals("Done")){
                            xun_fei_1.setText(separated[1]);
                            xun_fei_2.setText(separated[2]);
                            Toast.makeText(getApplicationContext(), "??????????????????", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "??????????????????", Toast.LENGTH_SHORT).show();
                        }
                        Toast.makeText(getApplicationContext(), "??????????????????", Toast.LENGTH_SHORT).show();
                    } else if (result[0].equals("Again")) {
                        if (send_2_first){
                            not_right_info.setVisibility(View.VISIBLE);
                            send_2_first = false;
                        } else {
                            // recognize page
                            title_1.setVisibility(View.GONE);
                            float_btn_start.setVisibility(View.GONE);
                            float_tv_result.setVisibility(View.GONE);
                            not_right_info.setVisibility(View.GONE);
                            next_step.setVisibility(View.GONE);
                            search_answer.setVisibility(View.GONE);


                            // recognize answer page
                            title_2.setVisibility(View.VISIBLE);
                            des3.setVisibility(View.VISIBLE);
                            des4.setVisibility(View.VISIBLE);
                            xun_fei_1.setVisibility(View.VISIBLE);
                            xun_fei_choose_1.setVisibility(View.VISIBLE);
                            xun_fei_2.setVisibility(View.VISIBLE);
                            xun_fei_choose_2.setVisibility(View.VISIBLE);
                            not_correct.setVisibility(View.VISIBLE);
                            title_2.setText("??????");
                            send_1_first = true;
                            send_2_first = true;
                            try {
                                URL url = new URL("http://47.92.159.130:8080/get_xunfei_answer?id="+str_id);
                                connection = (HttpURLConnection) url.openConnection();
                                connection.setConnectTimeout(3000);
                                connection.setReadTimeout(3000);
                                //?????????????????? GET / POST ???????????????
                                connection.setRequestMethod("GET");
                                connection.setDoInput(true);
                                connection.setDoOutput(false);
                                connection.connect();
                                int responseCode = connection.getResponseCode();
                                if (responseCode != HttpURLConnection.HTTP_OK) {
                                    throw new IOException("HTTP error code" + responseCode);
                                }
                                result[1] = getStringByStream(connection.getInputStream());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            String[] separated = result[1].split("&");
                            if(separated[0].equals("Done")){
                                xun_fei_1.setText(separated[1]);
                                xun_fei_2.setText(separated[2]);
                                Toast.makeText(getApplicationContext(), "??????????????????", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), "??????????????????", Toast.LENGTH_SHORT).show();
                            }
                            Toast.makeText(getApplicationContext(), "??????????????????", Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Toast.makeText(getApplicationContext(), "??????????????????", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            title_2 = displayView.findViewById(R.id.title_2);
            title_2.setVisibility(View.GONE);
            des3 = displayView.findViewById(R.id.des3);
            des3.setVisibility(View.GONE);
            des4 = displayView.findViewById(R.id.des4);
            des4.setVisibility(View.GONE);
            xun_fei_1 = displayView.findViewById(R.id.xun_fei_1);
            xun_fei_1.setVisibility(View.GONE);
            xun_fei_choose_1 = displayView.findViewById(R.id.xun_fei_choose_1);
            xun_fei_choose_1.setVisibility(View.GONE);
            xun_fei_choose_1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String[] result = {"",""};
                    String str_id = String.valueOf(index);
                    HttpURLConnection connection = null;
                    try {
                        String str_answer = xun_fei_1.getText().toString();
                        URL url = new URL("http://47.92.159.130:8080/set_xunfei_answer?id="+str_id+"&correct=True&answer="+str_answer);
                        connection = (HttpURLConnection) url.openConnection();
                        connection.setConnectTimeout(3000);
                        connection.setReadTimeout(3000);
                        //?????????????????? GET / POST ???????????????
                        connection.setRequestMethod("GET");
                        connection.setDoInput(true);
                        connection.setDoOutput(false);
                        connection.connect();
                        int responseCode = connection.getResponseCode();
                        if (responseCode != HttpURLConnection.HTTP_OK) {
                            throw new IOException("HTTP error code" + responseCode);
                        }
                        result[0] = getStringByStream(connection.getInputStream());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if(result[0].equals("Done")){
                        title_2.setText("??????+");
                        xun_fei_1.setVisibility(View.GONE);
                        xun_fei_choose_1.setVisibility(View.GONE);
                        xun_fei_2.setVisibility(View.GONE);
                        xun_fei_choose_2.setVisibility(View.GONE);
                        xun_fei_plus_1.setVisibility(View.VISIBLE);
                        xun_fei_plus_choose_1.setVisibility(View.VISIBLE);
                        xun_fei_plus_2.setVisibility(View.VISIBLE);
                        xun_fei_plus_choose_2.setVisibility(View.VISIBLE);
                        try {
                            URL url = new URL("http://47.92.159.130:8080/get_xunfei_plus_answer?id="+str_id);
                            connection = (HttpURLConnection) url.openConnection();
                            connection.setConnectTimeout(3000);
                            connection.setReadTimeout(3000);
                            //?????????????????? GET / POST ???????????????
                            connection.setRequestMethod("GET");
                            connection.setDoInput(true);
                            connection.setDoOutput(false);
                            connection.connect();
                            int responseCode = connection.getResponseCode();
                            if (responseCode != HttpURLConnection.HTTP_OK) {
                                throw new IOException("HTTP error code" + responseCode);
                            }
                            result[1] = getStringByStream(connection.getInputStream());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        String[] separated = result[1].split("&");
                        if(separated[0].equals("Done")){
                            xun_fei_plus_1.setText(separated[1]);
                            xun_fei_plus_2.setText(separated[2]);
                            Toast.makeText(getApplicationContext(), "??????????????????", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "??????????????????", Toast.LENGTH_SHORT).show();
                        }
                        Toast.makeText(getApplicationContext(), "??????????????????", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "??????????????????", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            xun_fei_2 = displayView.findViewById(R.id.xun_fei_2);
            xun_fei_2.setVisibility(View.GONE);
            xun_fei_choose_2 = displayView.findViewById(R.id.xun_fei_choose_2);
            xun_fei_choose_2.setVisibility(View.GONE);
            xun_fei_choose_2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String[] result = {"",""};
                    String str_id = String.valueOf(index);
                    HttpURLConnection connection = null;
                    try {
                        String str_answer = xun_fei_2.getText().toString();
                        URL url = new URL("http://47.92.159.130:8080/set_xunfei_answer?id="+str_id+"&correct=True&answer="+str_answer);
                        connection = (HttpURLConnection) url.openConnection();
                        connection.setConnectTimeout(3000);
                        connection.setReadTimeout(3000);
                        //?????????????????? GET / POST ???????????????
                        connection.setRequestMethod("GET");
                        connection.setDoInput(true);
                        connection.setDoOutput(false);
                        connection.connect();
                        int responseCode = connection.getResponseCode();
                        if (responseCode != HttpURLConnection.HTTP_OK) {
                            throw new IOException("HTTP error code" + responseCode);
                        }
                        result[0] = getStringByStream(connection.getInputStream());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if(result[0].equals("Done")){
                        title_2.setText("??????+");
                        xun_fei_1.setVisibility(View.GONE);
                        xun_fei_choose_1.setVisibility(View.GONE);
                        xun_fei_2.setVisibility(View.GONE);
                        xun_fei_choose_2.setVisibility(View.GONE);
                        xun_fei_plus_1.setVisibility(View.VISIBLE);
                        xun_fei_plus_choose_1.setVisibility(View.VISIBLE);
                        xun_fei_plus_2.setVisibility(View.VISIBLE);
                        xun_fei_plus_choose_2.setVisibility(View.VISIBLE);
                        try {
                            URL url = new URL("http://47.92.159.130:8080/get_xunfei_plus_answer?id="+str_id);
                            connection = (HttpURLConnection) url.openConnection();
                            connection.setConnectTimeout(3000);
                            connection.setReadTimeout(3000);
                            //?????????????????? GET / POST ???????????????
                            connection.setRequestMethod("GET");
                            connection.setDoInput(true);
                            connection.setDoOutput(false);
                            connection.connect();
                            int responseCode = connection.getResponseCode();
                            if (responseCode != HttpURLConnection.HTTP_OK) {
                                throw new IOException("HTTP error code" + responseCode);
                            }
                            result[1] = getStringByStream(connection.getInputStream());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        String[] separated = result[1].split("&");
                        if(separated[0].equals("Done")){
                            xun_fei_plus_1.setText(separated[1]);
                            xun_fei_plus_2.setText(separated[2]);
                            Toast.makeText(getApplicationContext(), "??????????????????", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "??????????????????", Toast.LENGTH_SHORT).show();
                        }
                        Toast.makeText(getApplicationContext(), "??????????????????", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "??????????????????", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            xun_fei_plus_1 = displayView.findViewById(R.id.xun_fei_plus_1);
            xun_fei_plus_1.setVisibility(View.GONE);
            xun_fei_plus_choose_1 = displayView.findViewById(R.id.xun_fei_plus_choose_1);
            xun_fei_plus_choose_1.setVisibility(View.GONE);
            xun_fei_plus_choose_1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String[] result = {"",""};
                    String str_id = String.valueOf(index);
                    HttpURLConnection connection = null;
                    try {
                        String str_answer = xun_fei_plus_1.getText().toString();
                        URL url = new URL("http://47.92.159.130:8080/set_xunfei_plus_answer?id="+str_id+"&correct=True&answer="+str_answer);
                        connection = (HttpURLConnection) url.openConnection();
                        connection.setConnectTimeout(3000);
                        connection.setReadTimeout(3000);
                        //?????????????????? GET / POST ???????????????
                        connection.setRequestMethod("GET");
                        connection.setDoInput(true);
                        connection.setDoOutput(false);
                        connection.connect();
                        int responseCode = connection.getResponseCode();
                        if (responseCode != HttpURLConnection.HTTP_OK) {
                            throw new IOException("HTTP error code" + responseCode);
                        }
                        result[0] = getStringByStream(connection.getInputStream());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if(result[0].equals("Done")){
                        title_2.setText("ST");
                        xun_fei_plus_1.setVisibility(View.GONE);
                        xun_fei_plus_choose_1.setVisibility(View.GONE);
                        xun_fei_plus_2.setVisibility(View.GONE);
                        xun_fei_plus_choose_2.setVisibility(View.GONE);
                        ST_1.setVisibility(View.VISIBLE);
                        ST_choose_1.setVisibility(View.VISIBLE);
                        ST_2.setVisibility(View.VISIBLE);
                        ST_choose_2.setVisibility(View.VISIBLE);
                        try {
                            URL url = new URL("http://47.92.159.130:8080/get_ST_answer?id="+str_id);
                            connection = (HttpURLConnection) url.openConnection();
                            connection.setConnectTimeout(3000);
                            connection.setReadTimeout(3000);
                            //?????????????????? GET / POST ???????????????
                            connection.setRequestMethod("GET");
                            connection.setDoInput(true);
                            connection.setDoOutput(false);
                            connection.connect();
                            int responseCode = connection.getResponseCode();
                            if (responseCode != HttpURLConnection.HTTP_OK) {
                                throw new IOException("HTTP error code" + responseCode);
                            }
                            result[1] = getStringByStream(connection.getInputStream());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        String[] separated = result[1].split("&");
                        if(separated[0].equals("Done")){
                            ST_1.setText(separated[1]);
                            ST_2.setText(separated[2]);
                            Toast.makeText(getApplicationContext(), "??????????????????", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "??????????????????", Toast.LENGTH_SHORT).show();
                        }
                        Toast.makeText(getApplicationContext(), "??????????????????", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "??????????????????", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            xun_fei_plus_2 = displayView.findViewById(R.id.xun_fei_plus_2);
            xun_fei_plus_2.setVisibility(View.GONE);
            xun_fei_plus_choose_2 = displayView.findViewById(R.id.xun_fei_plus_choose_2);
            xun_fei_plus_choose_2.setVisibility(View.GONE);
            xun_fei_plus_choose_2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String[] result = {"",""};
                    String str_id = String.valueOf(index);
                    HttpURLConnection connection = null;
                    try {
                        String str_answer = xun_fei_plus_2.getText().toString();
                        URL url = new URL("http://47.92.159.130:8080/set_xunfei_plus_answer?id="+str_id+"&correct=True&answer="+str_answer);
                        connection = (HttpURLConnection) url.openConnection();
                        connection.setConnectTimeout(3000);
                        connection.setReadTimeout(3000);
                        //?????????????????? GET / POST ???????????????
                        connection.setRequestMethod("GET");
                        connection.setDoInput(true);
                        connection.setDoOutput(false);
                        connection.connect();
                        int responseCode = connection.getResponseCode();
                        if (responseCode != HttpURLConnection.HTTP_OK) {
                            throw new IOException("HTTP error code" + responseCode);
                        }
                        result[0] = getStringByStream(connection.getInputStream());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if(result[0].equals("Done")){
                        title_2.setText("ST");
                        xun_fei_plus_1.setVisibility(View.GONE);
                        xun_fei_plus_choose_1.setVisibility(View.GONE);
                        xun_fei_plus_2.setVisibility(View.GONE);
                        xun_fei_plus_choose_2.setVisibility(View.GONE);
                        ST_1.setVisibility(View.VISIBLE);
                        ST_choose_1.setVisibility(View.VISIBLE);
                        ST_2.setVisibility(View.VISIBLE);
                        ST_choose_2.setVisibility(View.VISIBLE);
                        try {
                            URL url = new URL("http://47.92.159.130:8080/get_ST_answer?id="+str_id);
                            connection = (HttpURLConnection) url.openConnection();
                            connection.setConnectTimeout(3000);
                            connection.setReadTimeout(3000);
                            //?????????????????? GET / POST ???????????????
                            connection.setRequestMethod("GET");
                            connection.setDoInput(true);
                            connection.setDoOutput(false);
                            connection.connect();
                            int responseCode = connection.getResponseCode();
                            if (responseCode != HttpURLConnection.HTTP_OK) {
                                throw new IOException("HTTP error code" + responseCode);
                            }
                            result[1] = getStringByStream(connection.getInputStream());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        String[] separated = result[1].split("&");
                        if(separated[0].equals("Done")){
                            ST_1.setText(separated[1]);
                            ST_2.setText(separated[2]);
                            Toast.makeText(getApplicationContext(), "??????????????????", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "??????????????????", Toast.LENGTH_SHORT).show();
                        }
                        Toast.makeText(getApplicationContext(), "??????????????????", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "??????????????????", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            ST_1 = displayView.findViewById(R.id.ST_1);
            ST_1.setVisibility(View.GONE);
            ST_choose_1 = displayView.findViewById(R.id.ST_choose_1);
            ST_choose_1.setVisibility(View.GONE);
            ST_choose_1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String[] result = {""};
                    String str_id = String.valueOf(index);
                    HttpURLConnection connection = null;
                    try {
                        String str_answer = ST_1.getText().toString();
                        URL url = new URL("http://47.92.159.130:8080/set_ST_answer?id="+str_id+"&correct=True&answer="+str_answer);
                        connection = (HttpURLConnection) url.openConnection();
                        connection.setConnectTimeout(3000);
                        connection.setReadTimeout(3000);
                        //?????????????????? GET / POST ???????????????
                        connection.setRequestMethod("GET");
                        connection.setDoInput(true);
                        connection.setDoOutput(false);
                        connection.connect();
                        int responseCode = connection.getResponseCode();
                        if (responseCode != HttpURLConnection.HTTP_OK) {
                            throw new IOException("HTTP error code" + responseCode);
                        }
                        result[0] = getStringByStream(connection.getInputStream());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if(result[0].equals("Done")){
                        // recognize answer page
                        title_2.setVisibility(View.GONE);
                        des3.setVisibility(View.GONE);
                        des4.setVisibility(View.GONE);
                        ST_1.setVisibility(View.GONE);
                        ST_choose_1.setVisibility(View.GONE);
                        ST_2.setVisibility(View.GONE);
                        ST_choose_2.setVisibility(View.GONE);
                        not_correct.setVisibility(View.GONE);

                        //save page
                        user.setVisibility(View.VISIBLE);
                        user_input.setVisibility(View.VISIBLE);
                        job.setVisibility(View.VISIBLE);
                        job_input.setVisibility(View.VISIBLE);
                        save_info.setVisibility(View.VISIBLE);
                        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
                        Toast.makeText(getApplicationContext(), "??????????????????", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "??????????????????", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            ST_2 = displayView.findViewById(R.id.ST_2);
            ST_2.setVisibility(View.GONE);
            ST_choose_2 = displayView.findViewById(R.id.ST_choose_2);
            ST_choose_2.setVisibility(View.GONE);
            ST_choose_2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String[] result = {""};
                    String str_id = String.valueOf(index);
                    HttpURLConnection connection = null;
                    try {
                        String str_answer = ST_2.getText().toString();
                        URL url = new URL("http://47.92.159.130:8080/set_ST_answer?id="+str_id+"&correct=True&answer="+str_answer);
                        connection = (HttpURLConnection) url.openConnection();
                        connection.setConnectTimeout(3000);
                        connection.setReadTimeout(3000);
                        //?????????????????? GET / POST ???????????????
                        connection.setRequestMethod("GET");
                        connection.setDoInput(true);
                        connection.setDoOutput(false);
                        connection.connect();
                        int responseCode = connection.getResponseCode();
                        if (responseCode != HttpURLConnection.HTTP_OK) {
                            throw new IOException("HTTP error code" + responseCode);
                        }
                        result[0] = getStringByStream(connection.getInputStream());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if(result[0].equals("Done")){
                        // recognize answer page
                        title_2.setVisibility(View.GONE);
                        des3.setVisibility(View.GONE);
                        des4.setVisibility(View.GONE);
                        ST_1.setVisibility(View.GONE);
                        ST_choose_1.setVisibility(View.GONE);
                        ST_2.setVisibility(View.GONE);
                        ST_choose_2.setVisibility(View.GONE);
                        not_correct.setVisibility(View.GONE);

                        //save page
                        user.setVisibility(View.VISIBLE);
                        user_input.setVisibility(View.VISIBLE);
                        job.setVisibility(View.VISIBLE);
                        job_input.setVisibility(View.VISIBLE);
                        save_info.setVisibility(View.VISIBLE);
                        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
                        Toast.makeText(getApplicationContext(), "??????????????????", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "??????????????????", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            not_correct = displayView.findViewById(R.id.not_correct);
            not_correct.setVisibility(View.GONE);
            not_correct.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String[] result = {"",""};
                    String str_id = String.valueOf(index);
                    if (title_2.getText().toString().equals("??????")){
                        HttpURLConnection connection = null;
                        try {
                            URL url = new URL("http://47.92.159.130:8080/set_xunfei_answer?id="+str_id+"&correct=False&answer=");
                            connection = (HttpURLConnection) url.openConnection();
                            connection.setConnectTimeout(3000);
                            connection.setReadTimeout(3000);
                            //?????????????????? GET / POST ???????????????
                            connection.setRequestMethod("GET");
                            connection.setDoInput(true);
                            connection.setDoOutput(false);
                            connection.connect();
                            int responseCode = connection.getResponseCode();
                            if (responseCode != HttpURLConnection.HTTP_OK) {
                                throw new IOException("HTTP error code" + responseCode);
                            }
                            result[0] = getStringByStream(connection.getInputStream());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if(result[0].equals("Done")){
                            title_2.setText("??????+");
                            xun_fei_1.setVisibility(View.GONE);
                            xun_fei_choose_1.setVisibility(View.GONE);
                            xun_fei_2.setVisibility(View.GONE);
                            xun_fei_choose_2.setVisibility(View.GONE);
                            xun_fei_plus_1.setVisibility(View.VISIBLE);
                            xun_fei_plus_choose_1.setVisibility(View.VISIBLE);
                            xun_fei_plus_2.setVisibility(View.VISIBLE);
                            xun_fei_plus_choose_2.setVisibility(View.VISIBLE);
                            try {
                                URL url = new URL("http://47.92.159.130:8080/get_xunfei_plus_answer?id="+str_id);
                                connection = (HttpURLConnection) url.openConnection();
                                connection.setConnectTimeout(3000);
                                connection.setReadTimeout(3000);
                                //?????????????????? GET / POST ???????????????
                                connection.setRequestMethod("GET");
                                connection.setDoInput(true);
                                connection.setDoOutput(false);
                                connection.connect();
                                int responseCode = connection.getResponseCode();
                                if (responseCode != HttpURLConnection.HTTP_OK) {
                                    throw new IOException("HTTP error code" + responseCode);
                                }
                                result[1] = getStringByStream(connection.getInputStream());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            String[] separated = result[1].split("&");
                            if(separated[0].equals("Done")){
                                xun_fei_plus_1.setText(separated[1]);
                                xun_fei_plus_2.setText(separated[2]);
                                Toast.makeText(getApplicationContext(), "??????????????????", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), "??????????????????", Toast.LENGTH_SHORT).show();
                            }
                            Toast.makeText(getApplicationContext(), "??????????????????", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "??????????????????", Toast.LENGTH_SHORT).show();
                        }
                    } else if (title_2.getText().toString().equals("??????+")){
                        HttpURLConnection connection = null;
                        try {
                            URL url = new URL("http://47.92.159.130:8080/set_xunfei_plus_answer?id="+str_id+"&correct=False&answer=");
                            connection = (HttpURLConnection) url.openConnection();
                            connection.setConnectTimeout(3000);
                            connection.setReadTimeout(3000);
                            //?????????????????? GET / POST ???????????????
                            connection.setRequestMethod("GET");
                            connection.setDoInput(true);
                            connection.setDoOutput(false);
                            connection.connect();
                            int responseCode = connection.getResponseCode();
                            if (responseCode != HttpURLConnection.HTTP_OK) {
                                throw new IOException("HTTP error code" + responseCode);
                            }
                            result[0] = getStringByStream(connection.getInputStream());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if(result[0].equals("Done")){
                            title_2.setText("ST");
                            xun_fei_plus_1.setVisibility(View.GONE);
                            xun_fei_plus_choose_1.setVisibility(View.GONE);
                            xun_fei_plus_2.setVisibility(View.GONE);
                            xun_fei_plus_choose_2.setVisibility(View.GONE);
                            ST_1.setVisibility(View.VISIBLE);
                            ST_choose_1.setVisibility(View.VISIBLE);
                            ST_2.setVisibility(View.VISIBLE);
                            ST_choose_2.setVisibility(View.VISIBLE);
                            try {
                                URL url = new URL("http://47.92.159.130:8080/get_ST_answer?id="+str_id);
                                connection = (HttpURLConnection) url.openConnection();
                                connection.setConnectTimeout(3000);
                                connection.setReadTimeout(3000);
                                //?????????????????? GET / POST ???????????????
                                connection.setRequestMethod("GET");
                                connection.setDoInput(true);
                                connection.setDoOutput(false);
                                connection.connect();
                                int responseCode = connection.getResponseCode();
                                if (responseCode != HttpURLConnection.HTTP_OK) {
                                    throw new IOException("HTTP error code" + responseCode);
                                }
                                result[1] = getStringByStream(connection.getInputStream());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            String[] separated = result[1].split("&");
                            if(separated[0].equals("Done")){
                                ST_1.setText(separated[1]);
                                ST_2.setText(separated[2]);
                                Toast.makeText(getApplicationContext(), "??????????????????", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), "??????????????????", Toast.LENGTH_SHORT).show();
                            }
                            Toast.makeText(getApplicationContext(), "??????????????????", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "??????????????????", Toast.LENGTH_SHORT).show();
                        }
                    } else if (title_2.getText().toString().equals("ST")){
                        HttpURLConnection connection = null;
                        try {
                            URL url = new URL("http://47.92.159.130:8080/set_ST_answer?id="+str_id+"&correct=False&answer=");
                            connection = (HttpURLConnection) url.openConnection();
                            connection.setConnectTimeout(3000);
                            connection.setReadTimeout(3000);
                            //?????????????????? GET / POST ???????????????
                            connection.setRequestMethod("GET");
                            connection.setDoInput(true);
                            connection.setDoOutput(false);
                            connection.connect();
                            int responseCode = connection.getResponseCode();
                            if (responseCode != HttpURLConnection.HTTP_OK) {
                                throw new IOException("HTTP error code" + responseCode);
                            }
                            result[0] = getStringByStream(connection.getInputStream());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if(result[0].equals("Done")){
                            // recognize answer page
                            title_2.setVisibility(View.GONE);
                            des3.setVisibility(View.GONE);
                            des4.setVisibility(View.GONE);
                            ST_1.setVisibility(View.GONE);
                            ST_choose_1.setVisibility(View.GONE);
                            ST_2.setVisibility(View.GONE);
                            ST_choose_2.setVisibility(View.GONE);
                            not_correct.setVisibility(View.GONE);

                            //save page
                            user.setVisibility(View.VISIBLE);
                            user_input.setVisibility(View.VISIBLE);
                            job.setVisibility(View.VISIBLE);
                            job_input.setVisibility(View.VISIBLE);
                            save_info.setVisibility(View.VISIBLE);
                            layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
                            Toast.makeText(getApplicationContext(), "??????????????????", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "??????????????????", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
            user = displayView.findViewById(R.id.user);
            user.setVisibility(View.GONE);
            user_input = displayView.findViewById(R.id.user_input);
            user_input.setVisibility(View.GONE);
            user_input.setEnabled(true);
            job = displayView.findViewById(R.id.job);
            job.setVisibility(View.GONE);
            job_input = displayView.findViewById(R.id.job_input);
            job_input.setVisibility(View.GONE);
            job_input.setEnabled(true);
            save_info = displayView.findViewById(R.id.save_info);
            save_info.setVisibility(View.GONE);
            save_info.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String[] result = {""};
                    HttpURLConnection connection = null;
                    try {
                        String str_id = String.valueOf(index);
                        String str_user = user_input.getText().toString();
                        String str_job = job_input.getText().toString();
                        URL url = new URL("http://47.92.159.130:8080/save_test_info?id="+str_id+"&user="+str_user+"&job="+str_job);
                        connection = (HttpURLConnection) url.openConnection();
                        connection.setConnectTimeout(3000);
                        connection.setReadTimeout(3000);
                        //?????????????????? GET / POST ???????????????
                        connection.setRequestMethod("GET");
                        connection.setDoInput(true);
                        connection.setDoOutput(false);
                        connection.connect();
                        int responseCode = connection.getResponseCode();
                        if (responseCode != HttpURLConnection.HTTP_OK) {
                            throw new IOException("HTTP error code" + responseCode);
                        }
                        result[0] = getStringByStream(connection.getInputStream());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if(result[0].equals("Done")){
                        //save page
                        user.setVisibility(View.GONE);
                        user_input.setVisibility(View.GONE);
                        job.setVisibility(View.GONE);
                        job_input.setVisibility(View.GONE);
                        save_info.setVisibility(View.GONE);

                        close_after.setVisibility(View.VISIBLE);
                        next_group.setVisibility(View.VISIBLE);
                        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                        index = index + 1;
                        Toast.makeText(getApplicationContext(), "??????????????????", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "??????????????????", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            close_after = displayView.findViewById(R.id.close_after);
            close_after.setVisibility(View.GONE);
            close_after.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    reset_page();
                }
            });
            next_group = displayView.findViewById(R.id.next_group);
            next_group.setVisibility(View.GONE);
            next_group.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // load info page
                    des1.setVisibility(View.VISIBLE);
                    des2.setVisibility(View.VISIBLE);
                    setting_operation.setVisibility(View.VISIBLE);
                    radio_set_true.setVisibility(View.VISIBLE);
                    radio_set_false.setVisibility(View.VISIBLE);
                    close.setVisibility(View.VISIBLE);
                    user_start.setVisibility(View.VISIBLE);

                    //save page
                    close_after.setVisibility(View.GONE);
                    next_group.setVisibility(View.GONE);
                }
            });
            // ??????SpeechRecognizer????????????????????????????????????????????????
            mIat = SpeechRecognizer.createRecognizer(this, mInitListener);
            mSharedPreferences = getSharedPreferences("ASR", Activity.MODE_PRIVATE);
            windowManager.addView(displayView, layoutParams);
        }
    }

    private void reset_page(){
        // basic page
        basic.setVisibility(View.VISIBLE);
        layoutParams.width = 300;

        // load info page
        des1.setVisibility(View.GONE);
        des2.setVisibility(View.GONE);
        setting_operation.setVisibility(View.GONE);
        radio_set_true.setVisibility(View.GONE);
        radio_set_false.setVisibility(View.GONE);
        close.setVisibility(View.GONE);
        user_start.setVisibility(View.GONE);

        // recognize page
        title_1.setVisibility(View.GONE);
        float_btn_start.setVisibility(View.GONE);
        float_tv_result.setVisibility(View.GONE);
        float_tv_result.setText("??????????????????");
        not_right_info.setVisibility(View.GONE);
        not_right_info.setVisibility(View.GONE);
        next_step.setVisibility(View.GONE);
        search_answer.setVisibility(View.GONE);

        // recognize answer page
        title_2.setVisibility(View.GONE);
        des3.setVisibility(View.GONE);
        des4.setVisibility(View.GONE);
        xun_fei_1.setText("");
        xun_fei_1.setVisibility(View.GONE);
        xun_fei_choose_1.setVisibility(View.GONE);
        xun_fei_2.setText("");
        xun_fei_2.setVisibility(View.GONE);
        xun_fei_choose_2.setVisibility(View.GONE);
        xun_fei_plus_1.setText("");
        xun_fei_plus_1.setVisibility(View.GONE);
        xun_fei_plus_choose_1.setVisibility(View.GONE);
        xun_fei_plus_2.setText("");
        xun_fei_plus_2.setVisibility(View.GONE);
        xun_fei_plus_choose_2.setVisibility(View.GONE);
        ST_1.setText("");
        ST_1.setVisibility(View.GONE);
        ST_choose_1.setVisibility(View.GONE);
        ST_2.setText("");
        ST_2.setVisibility(View.GONE);
        ST_choose_2.setVisibility(View.GONE);
        not_correct.setVisibility(View.GONE);

        //save page
        user.setVisibility(View.GONE);
        user_input.setVisibility(View.GONE);
        user_input.setText("");
        job.setVisibility(View.GONE);
        job_input.setVisibility(View.GONE);
        job_input.setText("");
        save_info.setVisibility(View.GONE);
        close_after.setVisibility(View.GONE);
        next_group.setVisibility(View.GONE);

    }

    private String getStringByStream(InputStream inputStream) {
        Reader reader;
        try {
            reader = new InputStreamReader(inputStream, "UTF-8");
            char[] rawBuffer = new char[512];
            StringBuffer buffer = new StringBuffer();
            int length;
            while ((length = reader.read(rawBuffer)) != -1) {
                buffer.append(rawBuffer, 0, length);
            }
            return buffer.toString();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * ?????????????????????
     */
    private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            Log.d(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                showMsg("??????????????????????????????" + code + ",???????????????https://www.xfyun.cn/document/error-code??????????????????");
            }
        }
    };

    private RecognizerListener mRecoListener= new RecognizerListener() {

        //??????0-30
        @Override
        public void onVolumeChanged(int i, byte[] bytes) {

        }

        //????????????
        @Override
        public void onBeginOfSpeech() {
            Toast.makeText(getApplicationContext(),"????????????",Toast.LENGTH_LONG).show();
            float_btn_start.setEnabled(false);
        }

        //????????????
        @Override
        public void onEndOfSpeech() {
            Toast.makeText(getApplicationContext(),"????????????",Toast.LENGTH_LONG).show();
            float_btn_start.setEnabled(true);
        }

        //????????????
        @Override
        public void onResult(RecognizerResult recognizerResult, boolean b) {
            System.out.println(recognizerResult.getResultString());
            printResult(recognizerResult);
            if(title_1.getText().toString().equals("??????")) {
                next_step.setVisibility(View.VISIBLE);
            } else if (title_1.getText().toString().equals("ST")) {
                search_answer.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onError(SpeechError speechError) {
            showMsg(speechError.getPlainDescription(true));
        }

        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {

        }
    };

    /**
     * ????????????
     *
     * @param results
     */
    private void printResult(RecognizerResult results) {
        String text = JsonParser.parseIatResult(results.getResultString());

        String sn = null;
        // ??????json????????????sn??????
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

        float_tv_result.setText(resultBuffer.toString());//??????????????????

    }

    /**
     * ????????????
     *
     * @return
     */
    public void setParam() {
        // ????????????
        mIat.setParameter(SpeechConstant.PARAMS, null);
        // ??????????????????
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        // ????????????????????????
        mIat.setParameter(SpeechConstant.RESULT_TYPE, resultType);

        if (language.equals("zh_cn")) {
            String lag = mSharedPreferences.getString("iat_language_preference",
                    "mandarin");
            Log.e(TAG, "language:" + language);// ????????????
            mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
            // ??????????????????
            mIat.setParameter(SpeechConstant.ACCENT, lag);
        } else {

            mIat.setParameter(SpeechConstant.LANGUAGE, language);
        }
        Log.e(TAG, "last language:" + mIat.getParameter(SpeechConstant.LANGUAGE));

        //??????????????????dialog???????????????????????????
        //mIat.setParameter("view_tips_plain","false");

        // ?????????????????????:????????????????????????????????????????????????????????????????????????
        mIat.setParameter(SpeechConstant.VAD_BOS, mSharedPreferences.getString("iat_vadbos_preference", "4000"));

        // ?????????????????????:?????????????????????????????????????????????????????????????????????????????????????????? ??????????????????
        mIat.setParameter(SpeechConstant.VAD_EOS, mSharedPreferences.getString("iat_vadeos_preference", "1000"));

        // ??????????????????,?????????"0"?????????????????????,?????????"1"?????????????????????
        mIat.setParameter(SpeechConstant.ASR_PTT, mSharedPreferences.getString("iat_punc_preference", "1"));

        // ???????????????????????????????????????????????????pcm???wav??????????????????sd????????????WRITE_EXTERNAL_STORAGE??????
        mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/iat.wav");
    }

    /**
     * ????????????
     * @param msg
     */
    private void showMsg(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (null != mIat) {
            // ?????????????????????
            mIat.cancel();
            mIat.destroy();
        }
    }

    private class FloatingOnTouchListener implements View.OnTouchListener {
        private int x;
        private int y;

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    x = (int) event.getRawX();
                    y = (int) event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    int nowX = (int) event.getRawX();
                    int nowY = (int) event.getRawY();
                    int movedX = nowX - x;
                    int movedY = nowY - y;
                    x = nowX;
                    y = nowY;
                    layoutParams.x = layoutParams.x + movedX;
                    layoutParams.y = layoutParams.y + movedY;
                    windowManager.updateViewLayout(view, layoutParams);
                    break;
                default:
                    break;
            }
            return false;
        }
    }
}
