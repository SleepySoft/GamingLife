package glcore

import java.io.File


object GlFile {

    fun joinPaths(vararg paths: String) : String {
        return when (paths.size) {
            0 -> ""
            1 -> paths[0]
            else -> {
                var joinedPath = File(paths[0], paths[1])
                for (i in 2 until paths.size) {
                    joinedPath = File(joinedPath, paths[i])
                }
                joinedPath.absolutePath
            }
        }
    }

    fun glRoot() : String = glInternalRoot()

    fun glInternalRoot() : String = GlRoot.env.internalStorageRoot()

    fun glExternalRoot() : String = GlRoot.env.externalStorageRoot()

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

    fun listFiles(relativePath: String, fullPath: Boolean) : List< String > {
        return listFilesAbsolute(joinPaths(glRoot(), relativePath), fullPath)
    }

    fun listFilesInternal(relativePath: String, fullPath: Boolean) : List< String > {
        return listFilesAbsolute(joinPaths(glInternalRoot(), relativePath), fullPath)
    }

    fun listFilesExternal(relativePath: String, fullPath: Boolean) : List< String > {
        return listFilesAbsolute(joinPaths(glExternalRoot(), relativePath), fullPath)
    }

    fun listFilesAbsolute(absPath: String, fullPath: Boolean) : List< String > {
        val fileNames = mutableListOf< String >()
        File(absPath).walkTopDown().forEach {
            fileNames.add(if (fullPath) it.absolutePath else it.name)
        }
        return fileNames
    }

    // -----------------------------------------------------------------------

    private fun doSaveFile(filePath: String, fileContent: ByteArray) : Boolean {
        return try {
            println("Save file: $filePath")
            File(filePath).apply {
                ensureFileDirExists(this)
            }.writeBytes(fileContent)
            true
        } catch (e: Exception) {
            println("Save file fail: $filePath - $e")
            false
        } finally {

        }
    }

    private fun doLoadFile(filePath: String) : ByteArray {
        return try {
            println("Load file: $filePath")
            File(filePath).readBytes()
        } catch (e: Exception) {
            println("Load file fail: $filePath - $e")
            ByteArray(0)
        } finally {

        }
    }

    fun doCopyFile(srcFile: File, desFile: File, overwrite: Boolean = true) : Boolean {
        return try {
            println("Copy file $srcFile to $desFile")
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