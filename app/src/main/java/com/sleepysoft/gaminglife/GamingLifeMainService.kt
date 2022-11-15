package com.sleepysoft.gaminglife

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder


class GamingLifeMainService : Service() {
    private val mReceiver: BroadcastReceiver = ScreenBroadcastReceiver()

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val intentFilter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        registerReceiver(mReceiver, intentFilter)
        return START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mReceiver)
    }
}