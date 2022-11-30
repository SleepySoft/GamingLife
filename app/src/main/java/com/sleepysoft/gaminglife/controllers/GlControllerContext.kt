package com.sleepysoft.gaminglife.controllers

import android.view.View
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.support.annotation.RequiresApi
import graphengine.InteractiveDecorator
import java.lang.ref.WeakReference

//
// Why we use this class for:
//    The activity can be created and destroyed multiple times.
//    And the Context related feature may be affected by the lifecycle of activity.
//    So we expect that, the graph item will only be built one time.
//    All graph items will paint on the CURRENT context and environment.
//    To use this singleton for current context and environment reference/
//

object GlControllerContext {
    var view = WeakReference< View >(null)
    var context = WeakReference< Context >(null)
    var vibrator = WeakReference< Vibrator >(null)

    fun refresh() = view.get()?.invalidate()

    @RequiresApi(Build.VERSION_CODES.O)
    fun vibrate(milliseconds: Long) = vibrator.get()?.vibrate(
        VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE))

    fun launchActivity(cls: Class< * >) = launchActivity(cls) { }

    fun launchActivity(cls: Class< * >, intentDecorator: (intent: Intent) -> Unit) {
        val contextRef = context.get()
        contextRef?.run {
            val activityIntent = Intent(contextRef, cls)
            intentDecorator(activityIntent)
            contextRef.startActivity(activityIntent)
        }
    }
}
