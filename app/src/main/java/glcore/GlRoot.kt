package glcore

import android.content.Context
import java.io.File


object GlRoot {
    val glContext = GlContext()
    val glDatabase = GlDatabase()
    val glTaskModule = GlTaskModule(glDatabase)

    fun init() {
        GlAudioRecorder.init()
        glDatabase.init()
        glTaskModule.init()
    }

    fun getFileNameTs() : String {
        // TODO:
        return ""
    }

    fun saveFile(fileName: String, fileContent: String) : Boolean {
        return try {
            val ctx: Context = GlApplication.applicationContext()
            File(ctx.filesDir.absolutePath, fileName).writeText(fileContent)
            return true
        } catch (ex: Exception) {
            false
        } finally {

        }
    }

    fun loadFile(fileName: String) : String {
        return  try {
            val ctx: Context = GlApplication.applicationContext()
            val file = File(ctx.filesDir.absolutePath, fileName)
            file.readText()
        } catch (ex: Exception) {
            ""
        } finally {

        }
    }
}