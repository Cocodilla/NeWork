package ru.netology.nework.app

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.VideoFrameDecoder
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient
import ru.netology.nework.BuildConfig
import ru.netology.nework.di.AppDns
import com.yandex.mapkit.MapKitFactory

@HiltAndroidApp
class NeWorkApp : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()

        BuildConfig.YANDEX_MAPKIT_API_KEY
            .takeIf { it.isNotBlank() }
            ?.let { apiKey ->
                MapKitFactory.setApiKey(apiKey)
                MapKitFactory.initialize(this)
            }
    }

    override fun newImageLoader(): ImageLoader = ImageLoader.Builder(this)
        .okHttpClient {
            OkHttpClient.Builder()
                .dns(AppDns.dns)
                .addInterceptor { chain ->
                    val requestBuilder = chain.request().newBuilder()

                    BuildConfig.API_KEY
                        .takeIf { it.isNotBlank() }
                        ?.let { apiKey ->
                            requestBuilder.addHeader("Api-Key", apiKey)
                        }

                    chain.proceed(requestBuilder.build())
                }
                .build()
        }
        .components {
            add(VideoFrameDecoder.Factory())
        }
        .crossfade(true)
        .build()
}
