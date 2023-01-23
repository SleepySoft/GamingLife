package com.sleepysoft.gaminglife

import android.widget.Toast
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.appcompat.app.AppCompatActivity
import com.sleepysoft.gaminglife.controllers.GlControllerContext
import glcore.GlLog
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
