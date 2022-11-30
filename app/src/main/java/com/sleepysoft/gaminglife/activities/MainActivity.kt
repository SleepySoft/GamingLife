package com.sleepysoft.gaminglife.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager
import com.sleepysoft.gaminglife.controllers.GlControllerBuilder
import com.sleepysoft.gaminglife.GamingLifeMainService
import com.sleepysoft.gaminglife.PermissionActivity
import com.sleepysoft.gaminglife.RuntimeTest
import com.sleepysoft.gaminglife.controllers.GlControllerContext
import glcore.GlLog
import glcore.GlRoot
import glenv.GlEnv
import graphengine.GraphShadowView
import java.lang.ref.WeakReference


class MainActivity : AppCompatActivity() {
    private lateinit var mView: GraphShadowView
    private lateinit var mVibrator: Vibrator

    companion object {
        private var mHandler = Handler(Looper.getMainLooper())
    }

    private class PrivateRunnable(acitvity: MainActivity,
                                  private val handlerRef: Handler) : Runnable {
        private val activityRef = WeakReference(acitvity)

        override fun run() {
            activityRef.get()?.run {
                doPeriod()
                handlerRef.postDelayed(this@PrivateRunnable, 100)
            }
        }
    }
    private val runnable = PrivateRunnable(this, mHandler)

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mView = GraphShadowView(this)
        setContentView(mView)

        createVibrator()
        requireLockScreenShow()
        checkRequireExtStoragePermission()
        initControllerContext()

        GlRoot.init(GlEnv().apply { init() })
        GlControllerBuilder.checkBuildController()

        startGlService()

        RuntimeTest.testEntry(this)

        mHandler.postDelayed(runnable, 100)
    }

    override fun onStart() {
        super.onStart()

        val onLockScreen: Boolean = intent.getBooleanExtra("OnLockedScreen", false)
    }

    // ---------------------------------------------------------------------------------------------

    private fun createVibrator() {
        mVibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
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

    private fun initControllerContext() {
        GlControllerContext.view = WeakReference(mView)
        GlControllerContext.context = WeakReference(this)
        GlControllerContext.vibrator = WeakReference(mVibrator)
    }

    private fun startGlService() {
        val intent = Intent(this, GamingLifeMainService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

/*    private fun registerScreenStatusListener() {
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        registerReceiver(screenReceiver, filter)
    }*/

/*    private fun launchTimeViewActivity() {
        val intent = Intent(this, TimeViewActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
        GlLog.i("Popup Time View on Locked Screen.")
    }

    private fun doPeriod() {
        glControllerBuilder.pollingEntry()
    }*/
}