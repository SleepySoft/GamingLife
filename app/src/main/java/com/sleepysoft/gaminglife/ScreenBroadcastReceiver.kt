package com.sleepysoft.gaminglife

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import glcore.GlLog
import glenv.GlApp


class ScreenBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if ((Intent.ACTION_SCREEN_OFF == action) ||
            (Intent.ACTION_SCREEN_ON == action)) {

            GlLog.i("Screen locked.")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                Settings.canDrawOverlays(context)) {

                val activityIntent = Intent(context, TimeViewActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    putExtra("OnLockedScreen", true)
                }
                context.startActivity(activityIntent)
                // GlApp.applicationContext().startActivity(activityIntent)

                GlLog.i("Popup Time View on Locked Screen.")
            }
        }
    }
}
