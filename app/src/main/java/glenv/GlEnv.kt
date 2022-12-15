package glenv

import java.io.File
import android.os.Environment


class GlEnv {
    var valid: Boolean = false
    val glAudio: GlAudio = GlAudio()

    fun init() {
        glAudio.init()

        println("------------------------------- Init Environment -------------------------------")

        println("Internal Storage Root: ${internalStorageRoot()}")
        println("External Storage Root: ${externalStorageRoot()}")
        // println("Audio pcm temporary file: ${glAudio.PCMPath}")
        // println("Audio wav temporary file: ${glAudio.WAVPath}")

        println("----------------------------- Init Environment Done-----------------------------")

        valid = true
    }

    fun internalStorageRoot() : String =
        GlApp.applicationContext().filesDir.absolutePath

    fun externalStorageRoot() : String =
        File(Environment.getExternalStorageDirectory(), "gaminglife").absolutePath
}