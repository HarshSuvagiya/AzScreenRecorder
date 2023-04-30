package com.scorpion.screenrecorder;

import android.content.Context;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.scorpion.screenrecorder.appopen.SRC_AppOpenManager;


public final class SRC_MyApp extends android.app.Application {
    Context mContext;
    private static SRC_AppOpenManager appOpenManager;

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = this;

       SRC_Helper.getHeightAndWidth(mContext);

        MobileAds.initialize(
                this,
                new OnInitializationCompleteListener() {
                    @Override
                    public void onInitializationComplete(InitializationStatus initializationStatus) {
                    }
                });

        appOpenManager = new SRC_AppOpenManager(this);


    }
}