package com.muthuraj.cycle.fill

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.muthuraj.cycle.fill.network.CloudflareAccess
import com.muthuraj.cycle.fill.ui.App
import okhttp3.OkHttpClient

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SingletonImageLoader.setSafe {
            val okHttpClient = OkHttpClient().newBuilder()
                .hostnameVerifier { _, _ -> true }
                .addInterceptor { chain ->
                    val request = chain.request()
                    if (!CloudflareAccess.isTunnelHost(request.url.host)) {
                        return@addInterceptor chain.proceed(request)
                    }
                    val builder = request.newBuilder()
                    CloudflareAccess.headers.forEach { (name, value) -> builder.header(name, value) }
                    chain.proceed(builder.build())
                }
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