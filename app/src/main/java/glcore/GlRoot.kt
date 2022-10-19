package glcore

import android.content.Context
import java.io.File


object GlRoot {
    val glContext = GlContext()
    val glDatabase = GlDatabase()
    val glData = GlData(glDatabase)
    val glTimeModule = GlTimeModule()

    fun init() {
        glDatabase.init()
        glData.init()
        
        glTimeModule.init(glContext)
    }

    fun getFileNameTs() : String {
        // TODO:
        return ""
    }

    fun saveFile(fileName: String, fileContent: String) : Boolean {
        val ctx: Context = GlApplication.applicationContext()
        File(ctx.filesDir.absolutePath, fileName).writeText(fileContent)
        return true
    }

    fun loadFile(fileName: String) : String {
        val ctx: Context = GlApplication.applicationContext()
        val file = File(ctx.filesDir.absolutePath, fileName)
        return file.readText()
    }
}