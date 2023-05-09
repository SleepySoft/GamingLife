package com.sleepysoft.gaminglife

import android.app.Activity
import android.widget.Toast
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.DisplayMetrics
import android.util.Size
import android.view.WindowInsets
import androidx.activity.result.ActivityResult
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.sleepysoft.gaminglife.activities.GLIDManagementActivity
import com.sleepysoft.gaminglife.controllers.GlControllerContext
import glcore.*
import java.util.ArrayList


fun Context.toast(message: CharSequence) =
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()


fun Intent.putAnyExtra(key: String, data: Any) {
    when {
        data is Int -> this.putExtra(key, data)
        data is Long -> this.putExtra(key, data)
        data is Float -> this.putExtra(key, data)
        data is String -> this.putExtra(key, data)
        data is Boolean -> this.putExtra(key, data)
        data is Byte -> this.putExtra(key, data)
        data is Char -> this.putExtra(key, data)
        data is Short -> this.putExtra(key, data)
        data is Double -> this.putExtra(key, data)
        data is CharSequence -> this.putExtra(key, data)
        data is Parcelable -> this.putExtra(key, data)
        else -> GlLog.e("Warning: Unsupported type in putAnyExtra()")
    }
}


fun Intent.setRequestCode(code: Int) {
    putExtra(GlControllerContext.KEY_REQUEST_CODE, code)
}


/*@RequiresApi(Build.VERSION_CODES.R)
fun Activity.getActivitySize() : Size {
    val display = windowManager.currentWindowMetrics
    val windowInsets = display.windowInsets
    val insets = windowInsets.getInsetsIgnoringVisibility(
        WindowInsets.Type.navigationBars() or WindowInsets.Type.displayCutout())
    val insetsWidth = insets.right + insets.left
    val insetsHeight = insets.top + insets.bottom
    val width = display.bounds.width() - insetsWidth
    val height = display.bounds.height() - insetsHeight
    return Size(width, height)
}*/

fun Activity.getActivitySize(): Size {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val display = windowManager.currentWindowMetrics
        val windowInsets = display.windowInsets
        val insets = windowInsets.getInsetsIgnoringVisibility(
            WindowInsets.Type.navigationBars() or WindowInsets.Type.displayCutout())
        val insetsWidth = insets.right + insets.left
        val insetsHeight = insets.top + insets.bottom
        val width = display.bounds.width() - insetsWidth
        val height = display.bounds.height() - insetsHeight
        Size(width, height)
    } else {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        Size(displayMetrics.widthPixels, displayMetrics.heightPixels)
    }
}

fun AppCompatActivity.finishWithResult(data: Map< String , Any >, accepted: Boolean) {
    val resultCode = if (accepted)
        GlControllerContext.RESULT_ACCEPTED else
        GlControllerContext.RESULT_CANCELED
    val resultIntent = Intent().apply {
        for ((k, v) in data) {
            putAnyExtra(k, v)
        }
        val requestCode = intent.getIntExtra(
            GlControllerContext.KEY_REQUEST_CODE,
            GlControllerContext.REQUEST_INVALID)
        putExtra(GlControllerContext.KEY_REQUEST_CODE, requestCode)
        putExtra(GlControllerContext.KEY_RESULT_CODE, resultCode)
    }
    setResult(resultCode, resultIntent)
    finish()
}


fun ActivityResult.resultCode() : Int =
    data?.getIntExtra(
        GlControllerContext.KEY_RESULT_CODE,
        GlControllerContext.RESULT_INVALID) ?:
        GlControllerContext.RESULT_INVALID


fun ActivityResult.requestCode() : Int =
    data?.getIntExtra(
        GlControllerContext.KEY_REQUEST_CODE,
        GlControllerContext.REQUEST_INVALID) ?:
        GlControllerContext.REQUEST_INVALID


fun taskGroupIcon(groupID: String) =
    when (groupID) {
        GROUP_ID_IDLE -> R.drawable.ic_idle
        GROUP_ID_ENJOY -> R.drawable.ic_enjoy
        GROUP_ID_LIFE -> R.drawable.ic_life
        GROUP_ID_WORK -> R.drawable.ic_work
        GROUP_ID_PROMOTE -> R.drawable.ic_promote
        GROUP_ID_CREATE -> R.drawable.ic_create
        else -> R.drawable.ic_idle
    }
