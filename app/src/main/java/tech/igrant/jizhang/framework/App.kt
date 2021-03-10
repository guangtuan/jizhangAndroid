package tech.igrant.jizhang.framework

import android.app.Application

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        LocalStorage.instance().init(this)
    }

}