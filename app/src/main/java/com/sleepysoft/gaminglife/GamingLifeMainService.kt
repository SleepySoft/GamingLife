package com.sleepysoft.gaminglife

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import glcore.GlDateTime
import glcore.GlLog
import glcore.GlRoot
import glenv.GlEnv


class GamingLifeMainService : Service() {
    private val mRunner: Runnable = Runnable { serviceLooper() }
    private val mHandler : Handler = Handler(Looper.getMainLooper())
    private val mReceiver: BroadcastReceiver = ScreenBroadcastReceiver()

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")

    }

    override fun onCreate() {
        GlLog.i("GL service onCreate")
        GlRoot.init(GlEnv().apply { init() })

        val intentFilter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        registerReceiver(mReceiver, intentFilter)

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

    private fun serviceLooper() {
        servicePolling()
        mHandler.postDelayed(mRunner, 1000)
    }

    private fun servicePolling() {
        // GlLog.i("Service running: ${GlDateTime.datetime()}")
    }
}