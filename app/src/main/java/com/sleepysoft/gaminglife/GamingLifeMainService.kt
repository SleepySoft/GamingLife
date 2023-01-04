package com.sleepysoft.gaminglife

import android.app.*
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.sleepysoft.gaminglife.activities.MainActivity
import glcore.*
import glcore.GlDateTime
import glcore.GlLog
import glcore.GlRoot


class GamingLifeMainService : Service() {
    private val mRunner: Runnable = Runnable { serviceLooper() }
    private val mHandler : Handler = Handler(Looper.getMainLooper())
    private val mReceiver: BroadcastReceiver = ScreenBroadcastReceiver()
    private var mNotification: NotificationCompat.Builder? = null

    companion object {
        const val NOTIFICATION_ID_DEFAULT_INT = 299792458
        const val NOTIFICATION_ID_DEFAULT_STR = "GamingLife.Notification.Default"
    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")

    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate() {
        GlLog.i("GL service onCreate")

        // val intentFilter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        // registerReceiver(mReceiver, intentFilter)

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
    }

    // ---------------------------------------------------------------------------------------------

    private fun serviceLooper() {
        servicePolling()
        mHandler.postDelayed(mRunner, 1000)
    }

    private fun servicePolling() {
        updateNotification()
    }

    private fun createNotificationChannel(
        channelID: String, channelNAME: String, level: Int) : String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(channelID, channelNAME, level)
            manager.createNotificationChannel(channel)
        }
        return channelID
    }

    @RequiresApi(Build.VERSION_CODES.N)
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
            setShowWhen(false)
            setContentTitle("Task")
            setContentText("00:00")
            setContentIntent(pendingIntent)
            setSmallIcon(R.mipmap.ic_launcher)
            setAutoCancel(true)
            setOnlyAlertOnce(true)
            priority = NotificationCompat.PRIORITY_HIGH
            notification.flags = Notification.FLAG_ONGOING_EVENT
        }
        mNotification?.run {
            this@GamingLifeMainService.startForeground(NOTIFICATION_ID_DEFAULT_INT, this.build())
        }
    }

    private fun updateNotification() {
        mNotification?.run {
            this.setContentTitle(GlRoot.glService.getCurrentTaskName())
            this.setContentText(GlRoot.glService.getCurrentTaskLastTimeFormatted())

            setSmallIcon(when (GlRoot.glService.getCurrentTaskInfo().groupID) {
                GROUP_ID_IDLE -> R.drawable.ic_idle
                GROUP_ID_ENJOY -> R.drawable.ic_enjoy
                GROUP_ID_LIFE -> R.drawable.ic_life
                GROUP_ID_WORK -> R.drawable.ic_work
                GROUP_ID_PROMOTE -> R.drawable.ic_promote
                GROUP_ID_CREATE -> R.drawable.ic_create
                else -> R.drawable.ic_idle
            })

            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(NOTIFICATION_ID_DEFAULT_INT, this.build())
        }
    }
}
