package com.example.truthguardian;

import android.app.Application;
import com.example.truthguardian.api.ApiClient;

/**
 * 自定义Application类，为应用提供全局状态
 */
public class TruthGuardianApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ApiClient.init(this);
    }
} 