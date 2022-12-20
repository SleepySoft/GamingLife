package glenv

import android.app.Application
import android.content.Context
import glcore.GlFile
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.io.Writer
import java.util.*


class GlExceptionHandler : Thread.UncaughtExceptionHandler {
    private var context: Context? = null
    private var defaultHandler: Thread.UncaughtExceptionHandler? = null

    fun mount(context: Context? = null) {
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    fun unmount() {
        defaultHandler?.run {
            Thread.setDefaultUncaughtExceptionHandler(defaultHandler)
            defaultHandler = null
        }
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        println("#################################################################################")
        println(e.message)
        println("#################################################################################")

        val writer: Writer = StringWriter()
        val printWriter = PrintWriter(writer).apply {
            e.printStackTrace(this)
        }
        val stacktrace: String = writer.toString()
        printWriter.close()

        println(stacktrace)
        println("#################################################################################")

        val timestamp = Date().time
        val logFileName = "$timestamp.log"

        try {
            context?.openFileOutput(logFileName, Context.MODE_PRIVATE).use { output ->
                output?.write(stacktrace.toByteArray())
            }
        } catch (e: Exception) {
            println("Save log file fail: $logFileName - $e")
        } finally {

        }

        defaultHandler?.run {
            uncaughtException(t, e)
        }
    }
}


// Do not reference this class in GlRoot Module

class GlApp : Application() {
    val mExceptionHandler: GlExceptionHandler = GlExceptionHandler()

    init {
        instance = this
    }

    companion object {
        private var instance: GlApp? = null

        fun applicationContext() : Context {
            return instance!!.applicationContext
        }
    }

    override fun onCreate() {
        super.onCreate()
        mExceptionHandler.mount()
    }
}