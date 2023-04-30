package com.scorpion.screenrecorder.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.ayoubfletcher.consentsdk.ConsentSDK;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;
import com.scorpion.screenrecorder.SRC_Helper;
import com.scorpion.screenrecorder.R;
import com.scorpion.screenrecorder.utils.SRC_Utils;


public class SRC_ExitActivity extends AppCompatActivity {

    Context mContext;
    ImageView ic_exit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.src_activity_exit);
        SRC_Helper.FS(this);

        mContext = this;

        ic_exit = findViewById(R.id.ic_exit);

        SRC_Helper.setSize(ic_exit, 480, 104, true);

        ic_exit.setOnClickListener(v -> finish());

        initNativeAdvanceAds();
    }

    private AdLoader adLoader;

    private UnifiedNativeAdView nativeAdView;

    private void initNativeAdvanceAds() {
        flNativeAds = findViewById(R.id.flNativeAds);
        flNativeAds.setVisibility(View.GONE);
        nativeAdView = findViewById(R.id.ad_view);
        nativeAdView.setMediaView(nativeAdView.findViewById(R.id.ad_media));
        nativeAdView.setHeadlineView(nativeAdView.findViewById(R.id.ad_headline));
        nativeAdView.setBodyView(nativeAdView.findViewById(R.id.ad_body));
        nativeAdView.setCallToActionView(nativeAdView.findViewById(R.id.ad_call_to_action));
        nativeAdView.setIconView(nativeAdView.findViewById(R.id.ad_icon));
        nativeAdView.setStarRatingView(nativeAdView.findViewById(R.id.ad_stars));
        nativeAdView.setAdvertiserView(nativeAdView.findViewById(R.id.ad_advertiser));
        if (SRC_StartActivity.unifiedNativeAd1 == null)
            loadNativeAds();
        else {
            flNativeAds.setVisibility(View.VISIBLE);
            populateNativeAdView(SRC_StartActivity.unifiedNativeAd1, nativeAdView);
        }
    }

    private void populateNativeAdView(UnifiedNativeAd nativeAd, UnifiedNativeAdView adView) {
        VideoController vc = nativeAd.getVideoController();
        vc.setVideoLifecycleCallbacks(new VideoController.VideoLifecycleCallbacks() {
            public void onVideoEnd() {
                super.onVideoEnd();
            }
        });

        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        com.google.android.gms.ads.formats.NativeAd.Image icon = nativeAd.getIcon();
        if (icon == null) {
            adView.getIconView().setVisibility(View.INVISIBLE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(icon.getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }
        if (nativeAd.getStarRating() == null) {
            adView.getStarRatingView().setVisibility(View.INVISIBLE);
        } else {
            ((RatingBar) adView.getStarRatingView())
                    .setRating(nativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }
        if (nativeAd.getAdvertiser() == null) {
            adView.getAdvertiserView().setVisibility(View.INVISIBLE);
        } else {
            ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }
        adView.setNativeAd(nativeAd);
    }

    private FrameLayout flNativeAds;

    private void loadNativeAds() {
        VideoOptions videoOptions = new VideoOptions.Builder()
                .setStartMuted(false)
                .build();
        NativeAdOptions adOptions = new NativeAdOptions.Builder()
                .setVideoOptions(videoOptions)
                .build();
        AdLoader.Builder builder = new AdLoader.Builder(this, SRC_Utils.Companion.getNATIVE_ID());
        adLoader = builder.forUnifiedNativeAd(
                new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
                    @Override
                    public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
                        if (!adLoader.isLoading()) {
                            flNativeAds.setVisibility(View.VISIBLE);
                            populateNativeAdView(unifiedNativeAd, nativeAdView);
                        }
                    }
                }).withAdListener(
                new AdListener() {
                    @Override
                    public void onAdFailedToLoad(int errorCode) {
                        Log.e("Exit", "The previous native ad failed to load. Attempting to"
                                + " load another.");
                        if (!adLoader.isLoading()) {
                        }
                    }
                }).withNativeAdOptions(adOptions).build();

        // Load the Native ads.
        adLoader.loadAd(ConsentSDK.getAdRequest(this));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(getApplicationContext(), SRC_StartActivity.class));
        finish();
    }
}