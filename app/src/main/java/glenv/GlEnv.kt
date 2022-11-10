package glenv

import java.io.File
import android.os.Environment


class GlEnv {
    val glAudio: GlAudio = GlAudio()

    fun init() {
        glAudio.init()
    }

    fun internalStorageRoot() : String =
        GlApp.applicationContext().filesDir.absolutePath

    fun externalStorageRoot() : String =
        File(Environment.getExternalStorageDirectory(), "gaminglife").absolutePath
}