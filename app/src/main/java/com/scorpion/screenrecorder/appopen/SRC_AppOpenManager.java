package com.scorpion.screenrecorder.appopen;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.scorpion.screenrecorder.SRC_MyApp;
import com.scorpion.screenrecorder.activity.SRC_GrantOverlayPermissionActivity;
import com.scorpion.screenrecorder.activity.SRC_PrivacyPolicy;
import com.scorpion.screenrecorder.activity.SRC_ScreenshotPreviewActivity;
import com.scorpion.screenrecorder.activity.SRC_StartActivity;
import com.scorpion.screenrecorder.activity.SRC_VideoViewActivity;
import com.scorpion.screenrecorder.utils.SRC_Utils;

import java.util.Date;

import static androidx.lifecycle.Lifecycle.Event.ON_START;


/**
 * Prefetches App Open Ads.
 */
public class SRC_AppOpenManager implements LifecycleObserver, Application.ActivityLifecycleCallbacks {
    private static final String LOG_TAG = "AppOpenManager";
    private final SRC_MyApp Application;
    private AppOpenAd appOpenAd = null;
    private static boolean isShowingAd = false;
    private long loadTime = 0;

    public static boolean needToShow = false;

    private AppOpenAd.AppOpenAdLoadCallback loadCallback;
    private Activity currentActivity;


    /**
     * Constructor
     */
    public SRC_AppOpenManager(SRC_MyApp Application) {
        this.Application = Application;
        this.Application.registerActivityLifecycleCallbacks(this);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);

    }

    /**
     * Creates and returns ad request.
     */
    private AdRequest getAdRequest() {
        return new AdRequest.Builder().build();
    }

    /**
     * Utility method that checks if ad exists and can be shown.
     */
    public boolean isAdAvailable() {
        return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4);
    }

    /**
     * Request an ad
     */
    public void fetchAd() {
        // Have unused ad, no need to fetch another.
        if (isAdAvailable()) {
            return;
        }

        loadCallback =
                new AppOpenAd.AppOpenAdLoadCallback() {
                    /**
                     * Called when an app open ad has loaded.
                     *
                     * @param ad the loaded app open ad.
                     */
                    @Override
                    public void onAppOpenAdLoaded(AppOpenAd ad) {
                       appOpenAd = ad;
                       loadTime = (new Date()).getTime();
                    }

                    /**
                     * Called when an app open ad has failed to load.
                     *
                     * @param loadAdError the error.
                     */
                    @Override
                    public void onAppOpenAdFailedToLoad(LoadAdError loadAdError) {
                        // Handle the error.
                    }

                };
        AdRequest request = getAdRequest();


        try {
            AppOpenAd.load(
                    Application, SRC_Utils.Companion.getAPP_OPEN_ID(), request,
                    AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback);
        } catch (Exception e) {

        }


    }

    /**
     * Utility method to check if ad was loaded more than n hours ago.
     */
    private boolean wasLoadTimeLessThanNHoursAgo(long numHours) {
        long dateDifference = (new Date()).getTime() - this.loadTime;
        long numMilliSecondsPerHour = 3600000;
        return (dateDifference < (numMilliSecondsPerHour * numHours));
    }


    /**
     * ActivityLifecycleCallback methods
     */
    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
        currentActivity = activity;
    }

    @Override
    public void onActivityResumed(Activity activity) {
        currentActivity = activity;
    }

    @Override
    public void onActivityStopped(Activity activity) {
    }

    @Override
    public void onActivityPaused(Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        currentActivity = null;
    }

    /**
     * Shows the ad if one isn't already showing.
     */
    public void showAdIfAvailable() {
        // Only show ad if there is not already an app open ad currently showing
        // and an ad is available.
        if (!isShowingAd && isAdAvailable()) {
            Log.d(LOG_TAG, "Will show ad.");

            FullScreenContentCallback fullScreenContentCallback =
                    new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            // Set the reference to null so isAdAvailable() returns false.
                           appOpenAd = null;
                            isShowingAd = false;
                            try {
                                fetchAd();
                            } catch (Exception e) {

                            }
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(AdError adError) {
                        }

                        @Override
                        public void onAdShowedFullScreenContent() {
                            isShowingAd = true;
                        }
                    };

            appOpenAd.show(currentActivity, fullScreenContentCallback);

        } else {
            Log.d(LOG_TAG, "Can not show ad.");
            fetchAd();
        }
    }

    /**
     * LifecycleObserver methods
     */
    @OnLifecycleEvent(ON_START)
    public void onStart() {

        if (!(currentActivity instanceof SRC_PrivacyPolicy || currentActivity instanceof SRC_GrantOverlayPermissionActivity || currentActivity instanceof SRC_VideoViewActivity || currentActivity instanceof SRC_ScreenshotPreviewActivity || currentActivity instanceof SRC_StartActivity) && !needToShow)
            showAdIfAvailable();

        Log.d(LOG_TAG, "onStart");
    }
}