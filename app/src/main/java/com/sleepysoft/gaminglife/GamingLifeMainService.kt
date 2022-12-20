package com.sleepysoft.gaminglife

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import com.sleepysoft.gaminglife.activities.MainActivity
import glcore.GlLog


class GamingLifeMainService : Service() {
    private val mRunner: Runnable = Runnable { serviceLooper() }
    private val mHandler : Handler = Handler(Looper.getMainLooper())
    private val mReceiver: BroadcastReceiver = ScreenBroadcastReceiver()

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")

    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate() {
        GlLog.i("GL service onCreate")

        val intentFilter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        registerReceiver(mReceiver, intentFilter)

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
        unregisterReceiver(mReceiver)
    }

    // ---------------------------------------------------------------------------------------------

    private fun serviceLooper() {
        servicePolling()
        mHandler.postDelayed(mRunner, 1000)
    }

    private fun servicePolling() {
        // GlLog.i("Service running: ${GlDateTime.datetime()}")
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
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        val channelId = createNotificationChannel(
            "GamingLife.Notification.Default",
            "GamingLife Notification Channel",
            NotificationManager.IMPORTANCE_HIGH
        )
        val notification = NotificationCompat.Builder(this, channelId).apply {
            setShowWhen(false)
            setContentTitle("Task")
            setContentText("00:00")
            setContentIntent(pendingIntent)
            setSmallIcon(R.mipmap.ic_launcher)
            setPriority(NotificationCompat.PRIORITY_HIGH)
            setAutoCancel(true)
        }
        startForeground(100, notification.build())
    }
}