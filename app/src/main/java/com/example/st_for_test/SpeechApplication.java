package com.example.st_for_test;

import android.app.Application;

import com.iflytek.cloud.SpeechUtility;

public class SpeechApplication extends Application {

    @Override
    public void onCreate() {

        SpeechUtility.createUtility(SpeechApplication.this,"appid=6c310eee");

        super.onCreate();
    }
}
