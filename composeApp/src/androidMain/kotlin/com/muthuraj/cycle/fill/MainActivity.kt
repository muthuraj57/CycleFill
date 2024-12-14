package com.muthuraj.cycle.fill

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.muthuraj.cycle.fill.ui.App
import okhttp3.OkHttpClient

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SingletonImageLoader.setSafe {
            val okHttpClient = OkHttpClient().newBuilder()
                .hostnameVerifier { _, _ -> true }
                .build()
            ImageLoader.Builder(applicationContext)
                .components {
                    add(OkHttpNetworkFetcherFactory(callFactory = {
                        okHttpClient
                    }))
                }
                .build()
        }

        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}