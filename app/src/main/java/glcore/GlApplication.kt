package glcore

import android.app.Application
import android.content.Context


class GlApplication : Application() {

    init {
        instance = this
    }

    companion object {
        private var instance: GlApplication? = null

        fun applicationContext() : Context {
            return instance!!.applicationContext
        }
    }
}