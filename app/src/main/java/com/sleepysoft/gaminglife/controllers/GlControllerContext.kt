package com.sleepysoft.gaminglife.controllers

import android.app.Activity
import android.view.View
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.annotation.RequiresApi
import graphengine.GraphContext
import graphengine.GraphView
import java.lang.ref.WeakReference

//
// Why we use this class for:
//    The activity can be created and destroyed multiple times.
//    And the Context related feature may be affected by the lifecycle of activity.
//    So we expect that, the graph item will only be built one time.
//    All graph items will paint on the CURRENT context and environment.
//    To use this singleton for current context and environment reference/
//


typealias AsyncResultHandler =  (requestCode: Int, resultCode: Int, data: Intent?) -> Unit


class GlControllerContext : GraphContext {
    companion object {
        const val REQUEST_AUDIO_RECORD_CONTROLLER = 0x1001

        const val RESULT_COMMON_INPUT_CANCELLED = 0x2001
        const val RESULT_COMMON_INPUT_TEXT_COMPLETE = 0x2002
    }

    var graphView: GraphView? = null

    var view = WeakReference< View >(null)
    var context = WeakReference<Activity>(null)
    var vibrator = WeakReference< Vibrator >(null)
    val asyncResultHandler = mutableListOf< AsyncResultHandler >()

    // ---------------------------------- Implement GraphContext  ----------------------------------

    override fun refresh() {
        view.get()?.invalidate()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun vibrate(milliseconds: Long) {
        vibrator.get()?.vibrate(
            VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    // ---------------------------------------------------------------------------------------------

    fun launchActivity(cls: Class< * >, requestCode: Int? = null) = launchActivity(cls, requestCode) { }

    fun launchActivity(cls: Class< * >, requestCode: Int? = null, intentDecorator: (intent: Intent) -> Unit) {
        val contextRef = context.get()
        contextRef?.run {
            val activityIntent = Intent(contextRef, cls)
            intentDecorator(activityIntent)
            if (requestCode == null) {
                contextRef.startActivity(activityIntent)
            } else {
                contextRef.startActivityForResult(activityIntent, requestCode)
            }
        }
    }

    fun dispatchAsyncResult(requestCode: Int, resultCode: Int, data: Intent?) {
        asyncResultHandler.forEach { it(requestCode, resultCode, data) }
    }
}
