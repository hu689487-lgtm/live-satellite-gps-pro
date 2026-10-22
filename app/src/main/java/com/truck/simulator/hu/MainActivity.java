package com.truck.simulator.hu;

import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;

public class MainActivity extends AppCompatActivity {
    private WebView webView;
    private AdView adView;
    private InterstitialAd interstitialAd;
    private RewardedAd rewardedAd;
    private AppOpenAd appOpenAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);
        adView = findViewById(R.id.adView);

        MobileAds.initialize(this, initializationStatus -> {
            // AdMob initialized.
        });

        configureWebView();
        configureBannerAd();
        loadAppOpenAd();
        loadInterstitialAd();
        loadRewardedAd();
    }

    private void configureWebView() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setSupportZoom(false);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);

        webView.setWebViewClient(new WebViewClient());
        webView.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void showInterstitial() {
                runOnUiThread(() -> showInterstitialIfReady());
            }

            @JavascriptInterface
            public void showRewardedForFuel() {
                runOnUiThread(() -> showRewardedFuelAdIfReady());
            }
        }, "AndroidBridge");

        webView.loadUrl("file:///android_asset/truck_final_map_traffic.html");
    }

    private void configureBannerAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    private void loadAppOpenAd() {
        AppOpenAd.load(this,
                "ca-app-pub-3091902439302727/3261482290",
                new AdRequest.Builder().build(),
                new AppOpenAd.AppOpenAdLoadCallback() {
                    @Override
                    public void onAdLoaded(AppOpenAd ad) {
                        appOpenAd = ad;
                        showAppOpenAdIfAvailable();
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError loadAdError) {
                        appOpenAd = null;
                    }
                });
    }

    private void showAppOpenAdIfAvailable() {
        if (appOpenAd != null) {
            appOpenAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    appOpenAd = null;
                    loadAppOpenAd();
                }
            });
            appOpenAd.show(this);
            appOpenAd = null;
        }
    }

    private void loadInterstitialAd() {
        InterstitialAd.load(this,
                "ca-app-pub-3091902439302727/2959983980",
                new AdRequest.Builder().build(),
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(InterstitialAd ad) {
                        interstitialAd = ad;
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError loadAdError) {
                        interstitialAd = null;
                    }
                });
    }

    private void showInterstitialIfReady() {
        if (interstitialAd != null) {
            interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    interstitialAd = null;
                    loadInterstitialAd();
                }
            });
            interstitialAd.show(this);
        } else {
            loadInterstitialAd();
        }
    }

    private void loadRewardedAd() {
        RewardedAd.load(this,
                "ca-app-pub-3091902439302727/2193697228",
                new AdRequest.Builder().build(),
                new RewardedAdLoadCallback() {
                    @Override
                    public void onAdLoaded(RewardedAd ad) {
                        rewardedAd = ad;
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError error) {
                        rewardedAd = null;
                    }
                });
    }

    private void showRewardedFuelAdIfReady() {
        if (rewardedAd != null) {
            rewardedAd.show(this, rewardItem -> {
                webView.loadUrl("javascript:if (window.__fuelRewardGranted) window.__fuelRewardGranted();");
                rewardedAd = null;
                loadRewardedAd();
            });
        } else {
            loadRewardedAd();
            webView.loadUrl("javascript:if (window.__fuelRewardUnavailable) window.__fuelRewardUnavailable();");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adView != null) {
            adView.resume();
        }
        showAppOpenAdIfAvailable();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (adView != null) {
            adView.pause();
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
    }
}
