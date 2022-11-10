package glenv

import android.app.Application
import android.content.Context


// Do not reference this class in GlRoot Module

class GlApp : Application() {

    init {
        instance = this
    }

    companion object {
        private var instance: GlApp? = null

        fun applicationContext() : Context {
            return instance!!.applicationContext
        }
    }
}