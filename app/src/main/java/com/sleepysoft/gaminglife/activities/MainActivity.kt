package com.sleepysoft.gaminglife.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.*
import android.view.*
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.sleepysoft.gaminglife.*
import com.sleepysoft.gaminglife.controllers.*
import com.sleepysoft.gaminglife.views.FloatMenuView
import com.sleepysoft.gaminglife.views.GlFloatViewFactory
import com.sleepysoft.gaminglife.views.GlView
import glcore.*
import glenv.GlEnv
import graphengine.GraphView
import pub.devrel.easypermissions.EasyPermissions
import java.lang.ref.WeakReference


class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {
    companion object {
        private const val REQUEST_CODE_PERMISSION = 1024
    }

    // private var mInitRound: Int = 0

    private lateinit var mView: GlView
    private lateinit var mVibrator: Vibrator
    private val mCtrlContext = GlControllerContext()

    private lateinit var timeViewController: GlTimeViewController
    private lateinit var timeViewEditorController: GlTimeViewEditorController
    private lateinit var audioRecordController: GlAudioRecordLayerController

    private val requestDataLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->

        val resultCode = result.resultCode()
        if (result.resultCode() == GlControllerContext.RESULT_ACCEPTED) {
            val requestCode = result.requestCode()
            mCtrlContext.dispatchAsyncResult(requestCode, resultCode, result.data)
        }
    }

/*    private val filePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ ->
        glInit()
    }*/

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (EasyPermissions.hasPermissions(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO)) {
            doInitialization(useInternalStorage=false)
        } else {
            EasyPermissions.requestPermissions(this,
                getString(R.string.HINT_PERMISSION_BATCH),
                REQUEST_CODE_PERMISSION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO)
        }

        createFloatWindow()

        val contentView = findViewById<View>(android.R.id.content)
        contentView?.post {
            GlFloatViewFactory.moveFloatViewUnderActionBar(this, FloatMenuView::class.java)
        }
    }

/*    override fun onStart() {
        super.onStart()
        GlFloatViewFactory.moveFloatViewUnderActionBar(this, FloatMenuView::class.java)
    }*/

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (perms.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                doInitialization(useInternalStorage=false)
            } else {
                doInitialization(useInternalStorage=true)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (perms.contains(Manifest.permission.RECORD_AUDIO)) {
                toast(getString(R.string.HINT_RISK_WHEN_RECORD_DENIED))
            }
            if (perms.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                doInitialization(useInternalStorage=true)
            }
        }
    }

    // ---------------------------------------------------------------------------------------------

    @RequiresApi(Build.VERSION_CODES.O)
    private fun doInitialization(useInternalStorage: Boolean) {
        mView = GlView(this)
        setContentView(mView)

        createVibrator()
        initControllerContext()

        if (useInternalStorage) {
            GlFile.defaultStorage = GlFile.STORAGE_PLACE.STORAGE_INTERNAL
            AlertDialog.Builder(this).apply {
                this.setTitle(getString(R.string.HINT_TEXT_TIPS))
                this.setCancelable(true)
                this.setView(TextView(this@MainActivity).apply {
                    text = getString(R.string.HINT_RISK_WHEN_USING_INTERNAL_STORAGE)
                })
            }.create().show()
        }

        when (GlRoot.init(GlEnv().apply { init() })) {
            GlRoot.ERROR_NONE -> glStart()
            GlRoot.ERROR_INITED -> glStart()
/*            GlRoot.ERROR_FILE_PERMISSION -> when (mInitRound) {
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
            }*/
        }
    }

    private fun glStart() {
        buildGraphControllers()
        mView.graphView = mCtrlContext.graphView
        startGlService()
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

    private fun createVibrator() {
        mVibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    @SuppressLint("InflateParams")
    fun createFloatWindow() {
        GlFloatViewFactory.createFloatView(this, FloatMenuView::class.java)
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

/*    @RequiresApi(Build.VERSION_CODES.M)
    private fun requireLockScreenShow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1)
        {
            setShowWhenLocked(true)
        }
        else
        {
            window.addFlags(WindowManager.FLAG_DISMISS_KEYGUARD or
                    WindowManager.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.FLAG_TURN_SCREEN_ON)
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