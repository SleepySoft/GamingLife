package glcore

import android.content.Context
import android.os.Environment
import java.io.File


object GlFile {

    fun glInternalRoot() : String {
        val ctx: Context = GlApplication.applicationContext()
        return ctx.filesDir.absolutePath
    }

    fun glExternalRoot() : String {
        return File(Environment.getExternalStorageDirectory(), "gaminglife").absolutePath
    }

    fun saveFile(fileName: String, fileContent: ByteArray) : Boolean {
        // Use external for debug
        return saveFileExternal(fileName, fileContent)
    }

    fun loadFile(fileName: String) : ByteArray {
        // Use external for debug
        return loadFileExternal(fileName)
    }

    fun saveFileInternal(fileName: String, fileContent: ByteArray) : Boolean {
        return doSaveFile(File(glInternalRoot(), fileName).absolutePath, fileContent)
    }

    fun loadFileInternal(fileName: String) : ByteArray {
        return doLoadFile(File(glInternalRoot(), fileName).absolutePath)
    }

    fun saveFileExternal(fileName: String, fileContent: ByteArray) : Boolean {
        return doSaveFile(File(glExternalRoot(), fileName).absolutePath, fileContent)
    }

    fun loadFileExternal(fileName: String) : ByteArray {
        return doLoadFile(File(glExternalRoot(), fileName).absolutePath)
    }

    // -----------------------------------------------------------------------

    private fun doSaveFile(filePath: String, fileContent: ByteArray) : Boolean {
        return try {
            File(filePath).apply {
                ensureFileDirExists(this)
            }.writeBytes(fileContent)
            true
        } catch (ex: Exception) {
            println("File write fail: $filePath")
            false
        } finally {

        }
    }

    private fun doLoadFile(filePath: String) : ByteArray {
        return try {
            File(filePath).readBytes()
        } catch (ex: Exception) {
            println("Load write fail: $filePath")
            ByteArray(0)
        } finally {

        }
    }

    private fun ensureFileDirExists(file: File) {
        file.parentFile?.run {
            if (!this.exists()) {
                this.mkdirs()
            }
        }
    }
}