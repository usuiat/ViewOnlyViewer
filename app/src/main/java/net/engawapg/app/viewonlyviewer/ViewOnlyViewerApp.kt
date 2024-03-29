package net.engawapg.app.viewonlyviewer

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.VideoFrameDecoder
import net.engawapg.app.viewonlyviewer.data.GalleryModel
import net.engawapg.app.viewonlyviewer.data.SettingsRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

class ViewOnlyViewerApp: Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@ViewOnlyViewerApp)
            modules(appModule)
        }
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .crossfade(true)
            .components {
                add(VideoFrameDecoder.Factory())
            }
            .build()
    }
}

val appModule = module {
    single { GalleryModel(androidContext()) }
    single { SettingsRepository(androidContext()) }
}
