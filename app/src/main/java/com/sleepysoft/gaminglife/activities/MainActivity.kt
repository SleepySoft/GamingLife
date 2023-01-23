package com.sleepysoft.gaminglife.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.sleepysoft.gaminglife.DailyExtFileAdapter
import com.sleepysoft.gaminglife.GamingLifeMainService
import com.sleepysoft.gaminglife.PermissionActivity
import com.sleepysoft.gaminglife.controllers.*
import com.sleepysoft.gaminglife.toast
import com.sleepysoft.gaminglife.views.GlView
import glcore.*
import glenv.GlEnv
import graphengine.GraphView
import java.lang.ref.WeakReference


class MainActivity : AppCompatActivity() {
    companion object {
        private const val REQUEST_CODE_STORAGE_PERMISSION = 1024
    }

    private var mInitRound: Int = 0

    private lateinit var mView: GlView
    private lateinit var mVibrator: Vibrator
    private val mCtrlContext = GlControllerContext()

    private lateinit var timeViewController: GlTimeViewController
    private lateinit var timeViewEditorController: GlTimeViewEditorController
    private lateinit var audioRecordController: GlAudioRecordLayerController

    private val requestDataLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->

        val resultCode = result.data?.getIntExtra(
            GlControllerContext.KEY_RESULT_CODE,
            GlControllerContext.RESULT_INVALID) ?:
        GlControllerContext.RESULT_INVALID

        if (resultCode == GlControllerContext.RESULT_ACCEPTED) {
            val requestCode = result.data?.getIntExtra(
                GlControllerContext.KEY_REQUEST_CODE,
                GlControllerContext.REQUEST_INVALID) ?:
            GlControllerContext.REQUEST_INVALID

            when (requestCode) {
                REQUEST_CODE_STORAGE_PERMISSION -> glInit()
                else -> mCtrlContext.dispatchAsyncResult(requestCode, resultCode, result.data)
            }
        }
    }

    private val filePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ ->
        glInit()
    }

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
        // requireLockScreenShow()
        initControllerContext()

        glInit()

        // RuntimeTest.testEntry(this)
        // mHandler.postDelayed(runnable, 100)
    }

/*    override fun onStart() {
        super.onStart()

        val onLockScreen: Boolean = intent.getBooleanExtra("OnLockedScreen", false)
    }*/

/*    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_CODE_STORAGE_PERMISSION -> glInit()
            else -> mCtrlContext.dispatchAsyncResult(requestCode, resultCode, data)
        }
    }*/

    // ---------------------------------------------------------------------------------------------

    @SuppressLint("SetTextI18n")
    private fun glInit() {
        GlLog.i("GlInit, round = $mInitRound")

        when (val errorCode = GlRoot.init(GlEnv().apply { init() })) {
            GlRoot.ERROR_NONE -> glStart()
            GlRoot.ERROR_INITED -> glStart()
            GlRoot.ERROR_FILE_PERMISSION -> when (mInitRound) {
                0 -> {
                    requireExtStoragePermission()
                    mInitRound = 1
                }
                1 -> {
                    GlFile.defaultStorage = GlFile.STORAGE_PLACE.STORAGE_INTERNAL

                    AlertDialog.Builder(this).apply {
                        this.setTitle("提示")
                        this.setCancelable(true)
                        this.setView(TextView(this@MainActivity).apply {
                            text = "无法获取外部存储权限，尝试使用内部存储。\n\n软件卸载后数据将会被系统删除。"
                        })
                    }.create().show()
                    mInitRound = 2
                    glInit()
                }
                else -> {
                    GlLog.e("File Permission Error. GamingLife cannot proceed.")
                    toast("File Permission Error. GamingLife cannot proceed.")
                    finish()
                }
            }
            else -> {
                GlLog.e("Gaming life init fail. Error code: $errorCode")
                toast("Gaming life init fail. Error code: $errorCode")
                finish()
            }
        }
    }

    private fun glStart() {
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

/*    @RequiresApi(Build.VERSION_CODES.M)
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
    }*/

    private fun requireExtStoragePermission() {
        val intent = Intent(this, PermissionActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE_STORAGE_PERMISSION)
    }

    private fun initControllerContext() {
        mCtrlContext.view = WeakReference(mView)
        mCtrlContext.context = WeakReference(this)
        mCtrlContext.vibrator = WeakReference(mVibrator)
        mCtrlContext.requestDataLauncher = WeakReference(requestDataLauncher)
    }

    private fun buildGraphControllers() {
        mCtrlContext.graphView = GraphView(mCtrlContext)

        // Optimization: Only one controller will be activated on specific orientation.
        // Once orientation change. The Activity will be rebuilt and re-inited.

        val orientation = this.resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            audioRecordController = GlAudioRecordLayerController(mCtrlContext).apply { init() }
            timeViewController = GlTimeViewController(mCtrlContext, audioRecordController).apply { init() }
        } else {
            timeViewEditorController = GlTimeViewEditorController(
                mCtrlContext,
                GlDailyRecord().apply { loadDailyRecord(GlDateTime.datetime()) },
                GlRoot.systemConfig).apply { init() }
        }

        // timeViewControllerEx = GlTimeViewEditorController(mCtrlContext, GlRoot.glTaskModule).apply { init() }
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