package com.sleepysoft.gaminglife.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager
import com.sleepysoft.gaminglife.GamingLifeMainService
import com.sleepysoft.gaminglife.PermissionActivity
import com.sleepysoft.gaminglife.controllers.*
import com.sleepysoft.gaminglife.views.GlView
import glcore.GlLog
import glcore.GlRoot
import glenv.GlEnv
import graphengine.GraphView
import java.lang.ref.WeakReference


class MainActivity : AppCompatActivity() {
    companion object {
        private const val REQUEST_CODE_STORAGE_PERMISSION = 1024
    }

    private lateinit var mView: GlView
    private lateinit var mVibrator: Vibrator
    private val mCtrlContext = GlControllerContext()

    private lateinit var timeViewController: GlTimeViewController
    // private lateinit var timeViewControllerEx: GlTimeViewControllerEx
    private lateinit var audioRecordController: GlAudioRecordLayerController

/*    companion object {
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
    private val runnable = PrivateRunnable(this, mHandler)*/

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mView = GlView(this)
        setContentView(mView)

        createVibrator()
        requireLockScreenShow()

        if (PermissionActivity.verifyPermission()) {
            glStart()
        } else {
            requireExtStoragePermission()
        }

        // RuntimeTest.testEntry(this)
        // mHandler.postDelayed(runnable, 100)
    }

    override fun onStart() {
        super.onStart()

        val onLockScreen: Boolean = intent.getBooleanExtra("OnLockedScreen", false)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_CODE_STORAGE_PERMISSION -> when (resultCode) {
                PermissionActivity.PERMITTED -> glStart()
                PermissionActivity.DENIED -> finish()
                else -> finish()
            }
            else -> mCtrlContext.dispatchAsyncResult(requestCode, resultCode, data)
        }
    }

    // ---------------------------------------------------------------------------------------------

    private fun glStart() {
        initControllerContext()
        GlRoot.init(GlEnv().apply { init() })
        buildGraphControllers()
        mView.graphView = mCtrlContext.graphView
        startGlService()
    }

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

    private fun requireExtStoragePermission() {
        val intent = Intent(this, PermissionActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE_STORAGE_PERMISSION)
    }

    private fun initControllerContext() {
        mCtrlContext.view = WeakReference(mView)
        mCtrlContext.context = WeakReference(this)
        mCtrlContext.vibrator = WeakReference(mVibrator)
    }

    private fun buildGraphControllers() {
        mCtrlContext.graphView = GraphView(mCtrlContext)
        audioRecordController = GlAudioRecordLayerController(mCtrlContext).apply { init() }
        timeViewController = GlTimeViewController(
            mCtrlContext, audioRecordController).apply { init() }
        // timeViewControllerEx = GlTimeViewControllerEx(mCtrlContext, GlRoot.glTaskModule).apply { init() }
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