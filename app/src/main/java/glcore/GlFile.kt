package glcore

import android.content.Context
import android.os.Environment
import java.io.File


object GlFile {

    fun glInternalRoot() : String =
        GlApplication.applicationContext().filesDir.absolutePath

    fun glExternalRoot() : String =
        File(Environment.getExternalStorageDirectory(), "gaminglife").absolutePath

    fun saveFile(fileName: String, fileContent: ByteArray) : Boolean =
        // Use external for debug
        saveFileExternal(fileName, fileContent)

    fun loadFile(fileName: String) : ByteArray =
        // Use external for debug
        loadFileExternal(fileName)

    fun saveFileInternal(fileName: String, fileContent: ByteArray) : Boolean =
        doSaveFile(File(glInternalRoot(), fileName).absolutePath, fileContent)

    fun loadFileInternal(fileName: String) : ByteArray =
        doLoadFile(File(glInternalRoot(), fileName).absolutePath)

    fun saveFileExternal(fileName: String, fileContent: ByteArray) : Boolean =
        doSaveFile(File(glExternalRoot(), fileName).absolutePath, fileContent)

    fun loadFileExternal(fileName: String) : ByteArray =
        doLoadFile(File(glExternalRoot(), fileName).absolutePath)

    fun copyFileInternal(srcFileName: String, desFileName: String, overwrite: Boolean = true) : Boolean =
        doCopyFile(File(glInternalRoot(), srcFileName), File(glInternalRoot(), desFileName), overwrite)

    fun copyFileExternal(srcFileName: String, desFileName: String, overwrite: Boolean = true) : Boolean =
        doCopyFile(File(glExternalRoot(), srcFileName), File(glExternalRoot(), desFileName), overwrite)

    fun copyFileAbsolute(srcFilePath: String, desFilePath: String, overwrite: Boolean = true) : Boolean =
        doCopyFile(File(srcFilePath), File(desFilePath), overwrite)

    // -----------------------------------------------------------------------

    private fun doSaveFile(filePath: String, fileContent: ByteArray) : Boolean {
        return try {
            File(filePath).apply {
                ensureFileDirExists(this)
            }.writeBytes(fileContent)
            true
        } catch (e: Exception) {
            println("File write fail: $filePath - $e")
            false
        } finally {

        }
    }

    private fun doLoadFile(filePath: String) : ByteArray {
        return try {
            File(filePath).readBytes()
        } catch (e: Exception) {
            println("Load write fail: $filePath - $e")
            ByteArray(0)
        } finally {

        }
    }

    fun doCopyFile(srcFile: File, desFile: File, overwrite: Boolean = true) : Boolean {
        return try {
            srcFile.copyTo(desFile, overwrite)
            true
        } catch (e: Exception) {
            println("Copy file form ${srcFile.absolutePath} to ${desFile.absolutePath} fail - $e")
            false
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