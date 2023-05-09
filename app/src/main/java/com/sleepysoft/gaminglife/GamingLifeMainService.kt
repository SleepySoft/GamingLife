package com.sleepysoft.gaminglife

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.media.MediaRecorder
import android.os.*
import android.view.*
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.sleepysoft.gaminglife.activities.MainActivity
import glcore.*
import java.lang.ref.WeakReference


class GamingLifeMainService : Service() {
    private var fistRun: Boolean = true
    private val mRunner: Runnable = Runnable { serviceLooper() }
    private val mHandler : Handler = Handler(Looper.getMainLooper())
    // private val mReceiver: BroadcastReceiver = ScreenBroadcastReceiver()
    private var mNotification: NotificationCompat.Builder? = null
    private var mRecorder: MediaRecorder? = null

    companion object {
        var serviceInstance = WeakReference< GamingLifeMainService >(null)
        const val NOTIFICATION_ID_DEFAULT_INT = 299792458
        const val NOTIFICATION_ID_DEFAULT_STR = "GamingLife.Notification.Default"
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        GlLog.i("GL service onCreate")

        // val intentFilter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        // registerReceiver(mReceiver, intentFilter)

        serviceInstance = WeakReference(this)

        createNotification()

        mHandler.postDelayed(mRunner, 1000)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        GlLog.i("GL service onStartCommand")
        return START_STICKY
    }

    override fun onDestroy() {
        GlLog.i("GL service onDestroy")
        super.onDestroy()
        // unregisterReceiver(mReceiver)

        stopRecord()
    }

    // ---------------------------------------------------------------------------------------------

    @RequiresApi(Build.VERSION_CODES.S)
    fun startRecord(filePath: String) {
        mRecorder?.run { stopRecord() }
        mRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            MediaRecorder(this) else MediaRecorder()
        mRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mRecorder?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        mRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        mRecorder?.setOutputFile(filePath)
        mRecorder?.prepare()
        mRecorder?.start()
    }

    fun stopRecord() {
        try {
            mRecorder?.stop()
            mRecorder?.release()
        } catch (e: Exception) {
            println("Stop record fail: $e")
        } finally {
            mRecorder = null
        }
    }

    // ---------------------------------------------------------------------------------------------

    private fun serviceLooper() {
        servicePolling()
        mHandler.postDelayed(mRunner, 1000)
    }

    private fun servicePolling() {
        if (fistRun) {
            fistRun = false
            GlService.checkSettleDailyData()
        }
        updateNotification()
    }

    private fun createNotificationChannel(
        channelID: String, channelNAME: String, level: Int) : String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(channelID, channelNAME, level).apply {  }
            manager.createNotificationChannel(channel)
        }
        return channelID
    }

    private fun createNotification() {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra("OnNotification", true)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val channelId = createNotificationChannel(
            NOTIFICATION_ID_DEFAULT_STR,
            "GamingLife Notification Channel",
            NotificationManager.IMPORTANCE_HIGH
        )
        mNotification = NotificationCompat.Builder(this, channelId).apply {
            setSilent(true)
            setShowWhen(false)
            setContentTitle("Task")
            setContentText("00:00")
            setContentIntent(pendingIntent)
            setSmallIcon(R.mipmap.ic_launcher)
            setAutoCancel(true)
            setOnlyAlertOnce(true)
            priority = NotificationCompat.PRIORITY_LOW
            notification.flags = Notification.FLAG_ONGOING_EVENT
        }
        mNotification?.run {
            this@GamingLifeMainService.startForeground(NOTIFICATION_ID_DEFAULT_INT, this.build())
        }
    }

    private fun updateNotification() {
        mNotification?.run {
            this.setContentTitle(GlService.getCurrentTaskName())
            this.setContentText(GlService.getCurrentTaskLastTimeFormatted())

            setSmallIcon(taskGroupIcon(GlService.getCurrentTaskInfo().groupID))

            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(NOTIFICATION_ID_DEFAULT_INT, this.build())
        }
    }
}
