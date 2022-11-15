package com.sleepysoft.gaminglife

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager
import glcore.GlLog
import glcore.GlRoot
import glenv.GlEnv
import graphengine.GraphView


@RequiresApi(Build.VERSION_CODES.N)
fun testInternalStorage(ctx: Context) {
    println(ctx.dataDir.absolutePath)
    println(ctx.filesDir.absolutePath)
    println(ctx.cacheDir.absolutePath)

    println(ctx.getExternalFilesDir( Environment.DIRECTORY_DCIM)?.absolutePath)
    println(ctx.getExternalFilesDirs( Environment.DIRECTORY_DCIM))

    println(ctx.externalCacheDir?.absolutePath)
    println(ctx.externalCacheDirs)
}


fun testExternalStorage() {
    println(Environment.getExternalStorageState())

    println(Environment.getRootDirectory().absolutePath)
    println(Environment.getDataDirectory().absolutePath)
    println(Environment.getExternalStorageDirectory().absolutePath)
    println(Environment.getDownloadCacheDirectory().absolutePath)
    println(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath)
}


class MainActivity : AppCompatActivity() {
    private lateinit var mHandler : Handler
    private lateinit var mRunnable : Runnable
    private lateinit var mView: GraphView
    private lateinit var mController: GlTimeViewController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireLockScreenShow()
        checkRequireExtStoragePermission()

        GlRoot.init(GlEnv().apply { init() })

        mHandler = Handler(Looper.getMainLooper())
        mRunnable = Runnable {
            doPeriod()
        }

        mView = GraphView(this)
        mController = GlTimeViewController(this, mView, GlRoot.glTaskModule).apply {
            this.init()
            mView.pushObserver(this)
        }

        setContentView(mView)
        mHandler.postDelayed(mRunnable, 1000)
    }

    private fun doPeriod() {
        mController.polling()
        mHandler.postDelayed(mRunnable, 100)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun requireLockScreenShow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1)
        {
            setShowWhenLocked(true)
        }
        else
        {
            window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        }

        val canOverlay: Boolean = Settings.canDrawOverlays(this)
        if (canOverlay) {
            GlLog.i("Overlay permission OK.")
        }
        else {
            startActivity(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
            )
        }
    }

    private fun checkRequireExtStoragePermission() {
        val intent = Intent(this, PermissionActivity::class.java)
        startActivity(intent)
    }
}