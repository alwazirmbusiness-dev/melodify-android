package com.melodifyverse.nancyajram;

import static android.content.ContentValues.TAG;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.ump.ConsentForm;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;

import java.util.concurrent.atomic.AtomicBoolean;


public class Menu extends AppCompatActivity {

    private InterstitialAd mInterstitialAd;

    private ConsentInformation consentInformation;
    private final AtomicBoolean isMobileAdsInitializeCalled = new AtomicBoolean(false);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);

        requestConsentForm();

        loadInterstitial();
    }

    private void loadInterstitial() {
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(this, getResources().getString(R.string.admob_interstitial), adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        super.onAdLoaded(interstitialAd);
                        Log.i("Admob", "interstitial ad loaded:");
                        Menu.this.mInterstitialAd = interstitialAd;

                        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                super.onAdDismissedFullScreenContent();
                                Log.d("Admob", "Ad dismiss: ");
                                startMainActivity();
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                                super.onAdFailedToShowFullScreenContent(adError);
                                Log.d("Admob", "ad failed to show: " + adError);
                                startMainActivity();
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                super.onAdShowedFullScreenContent();
                                Log.d("Admob", "Ad displayed: ");
                                Menu.this.mInterstitialAd = null;
                                loadInterstitial();
                            }
                        });
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        super.onAdFailedToLoad(loadAdError);
                        Log.i("Admob", "onAdFailedToLoad: " + loadAdError);
                        mInterstitialAd = null;
                    }
                });
    }

    private void requestConsentForm() {

        // Set tag for under age of consent. false means users are not under age
        // of consent.
        ConsentRequestParameters params = new ConsentRequestParameters
                .Builder()
                .setTagForUnderAgeOfConsent(false)
                .build();

        consentInformation = UserMessagingPlatform.getConsentInformation(this);
        consentInformation.requestConsentInfoUpdate(
                this,
                params,
                (ConsentInformation.OnConsentInfoUpdateSuccessListener) () -> {
                    UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                            this,
                            (ConsentForm.OnConsentFormDismissedListener) loadAndShowError -> {
                                if (loadAndShowError != null) {
                                    // Consent gathering failed.
                                    Log.w(TAG, String.format("%s: %s",
                                            loadAndShowError.getErrorCode(),
                                            loadAndShowError.getMessage()));
                                }

                                // Consent has been gathered.
                                if (consentInformation.canRequestAds()) {
                                    initializeMobileAdsSdk();
                                }
                            }
                    );
                },
                (ConsentInformation.OnConsentInfoUpdateFailureListener) requestConsentError -> {
                    // Consent gathering failed.
                    Log.w(TAG, String.format("%s: %s",
                            requestConsentError.getErrorCode(),
                            requestConsentError.getMessage()));
                });

        // Check if you can initialize the Google Mobile Ads SDK in parallel
        // while checking for new consent information. Consent obtained in
        // the previous session can be used to request ads.
        if (consentInformation.canRequestAds()) {
            initializeMobileAdsSdk();
        }
    }

    private void initializeMobileAdsSdk() {
        if (isMobileAdsInitializeCalled.getAndSet(true)) {
            return;
        }

        //  the Google Mobile Ads SDK.
        MobileAds.initialize(this);
    }

    public void GotoMainActivity(View view) {
        if (mInterstitialAd != null) {
            final Dialog dialog = new Dialog(Menu.this);
            dialog.setContentView(R.layout.waiting_int);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            dialog.show();

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                dialog.dismiss();
                mInterstitialAd.show(Menu.this);
            }, 2000);
        } else {
            Log.d("TAG", "The interstitial ad wasn't ready yet.");
            startMainActivity();
        }
    }

    private void startMainActivity() {
        Intent intent = new Intent(Menu.this, MainActivity.class);
        intent.putExtra("play_list_type", 0); // 1 for favourites and 0 for normal playlist
        startActivity(intent);
    }

    public void GotoFavourite(View view) {
        Intent intent = new Intent(Menu.this, MainActivity.class);
        intent.putExtra("play_list_type", 1); // 1 for favourites and 0 for normal playlist
        startActivity(intent);
    }

    public void GotoPrivacy(View v) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.provicyPolicy))));
        } catch (ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.provicyPolicy))));
        }
    }

    public void GotoRateApp(View v) {
        try {
            Uri uri1 = Uri.parse(getString(R.string.rate_the_app));
            Intent getMarket = new Intent(Intent.ACTION_VIEW, uri1);
            startActivity(getMarket);
        } catch (ActivityNotFoundException e) {
            Uri uri1 = Uri.parse(getString(R.string.rate_the_app));
            Intent getMarket = new Intent(Intent.ACTION_VIEW, uri1);
            startActivity(getMarket);
        }
    }

    public void GoToRating(View v) {
        try {
            Uri uri1 = Uri.parse(getString(R.string.marketAccount));
            Intent getMarket = new Intent(Intent.ACTION_VIEW, uri1);
            startActivity(getMarket);
        } catch (ActivityNotFoundException e) {
            Uri uri1 = Uri.parse(getString(R.string.marketAccount));
            Intent getMarket = new Intent(Intent.ACTION_VIEW, uri1);
            startActivity(getMarket);
        }
    }

}
