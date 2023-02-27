package io.github.afalabarce.jetpackcompose

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.*
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback

@Composable
fun AdmobInterstitialAdvertView(adUnitId: String, onInterstitial: (Boolean) -> Unit = {}){
    val context = LocalContext.current

    LaunchedEffect(key1 = "Reward Word"){
        InterstitialAd.load(
            context,
            adUnitId,
            AdManagerAdRequest.Builder().build(),
            object: InterstitialAdLoadCallback() {

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    Log.e("Error Admob", p0.message)
                    onInterstitial(false)
                }

                override fun onAdLoaded(p0: InterstitialAd) {
                    super.onAdLoaded(p0)
                    p0.setImmersiveMode(true)

                    p0.fullScreenContentCallback = object: FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent()
                            onInterstitial(false)
                        }

                        override fun onAdShowedFullScreenContent() {
                            super.onAdShowedFullScreenContent()
                            onInterstitial(true)
                        }

                        override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                            super.onAdFailedToShowFullScreenContent(p0)
                            onInterstitial(false)
                        }
                    }

                    p0.show(context as Activity)
                }
            }
        )
    }
}

@Composable
fun AdmobRewardInterstitialAdvertView(adUnitId: String, onRewardShown: () -> Unit, onReward: (Boolean) -> Unit = {}){
    val context = LocalContext.current

    LaunchedEffect(key1 = "Reward Word"){
        RewardedInterstitialAd.load(
            context,
            adUnitId,
            AdManagerAdRequest.Builder().build(),
            object: RewardedInterstitialAdLoadCallback() {

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    Log.e("Error Admob", p0.message)
                    onReward(false)
                    onRewardShown()
                }

                override fun onAdLoaded(rewardAd: RewardedInterstitialAd) {
                    super.onAdLoaded(rewardAd)
                    rewardAd.setImmersiveMode(true)
                    onRewardShown()
                    rewardAd.fullScreenContentCallback = object: FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent()
                            onReward(false)
                        }

                        override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                            super.onAdFailedToShowFullScreenContent(p0)
                            onReward(false)
                        }
                    }

                    rewardAd.show(context as Activity){ rewardItem ->
                        if (rewardItem.type == "HangmanReward" && rewardItem.amount == 1){
                            onReward(true)
                        }else
                            onReward(false)
                    }
                }
            }
        )
    }
}

@Composable
fun AdmobAdvertView(adUnitId: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val isInEditMode = LocalInspectionMode.current
    Column(modifier = modifier.height(54.dp)) {
        if (isInEditMode) {
            Text(
                modifier = modifier
                    .background(Color.Red)
                    .padding(horizontal = 2.dp, vertical = 6.dp),
                textAlign = TextAlign.Center,
                color = Color.White,
                text = "Advert Here",
            )
        } else {
            AndroidView(
                modifier = modifier,
                factory = { context ->
                    AdView(context).apply {
                        setAdUnitId(adUnitId)
                        setAdSize(AdSize.BANNER)
                        this.adListener = object : AdListener() {
                            override fun onAdFailedToLoad(p0: LoadAdError) {
                                super.onAdFailedToLoad(p0)
                                Log.e("AdmobError", p0.message)

                                Log.e("AdmobError", p0.toString())
                            }
                        }
                        loadAd(AdRequest.Builder().build())
                    }
                }
            )
        }
    }
}


