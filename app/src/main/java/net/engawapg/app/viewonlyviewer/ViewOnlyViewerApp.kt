package net.engawapg.app.viewonlyviewer

import android.app.Application
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

class ViewOnlyViewerApp: Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            modules(appModule)
        }
    }
}

val appModule = module {
    single { GalleryModel() }
    viewModel { MainViewModel(get()) }
}
