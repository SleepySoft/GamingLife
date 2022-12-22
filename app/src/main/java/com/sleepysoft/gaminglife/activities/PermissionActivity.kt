package com.sleepysoft.gaminglife

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import glcore.GlFile
import java.io.File


class PermissionActivity : AppCompatActivity() {

    companion object {
        const val DENIED = 1024
        const val PERMITTED = 2048
        private const val REQUEST_CODE = 4096

        fun verifyPermission() : Boolean {
            var ret = false
            val file = File(Environment.getExternalStorageDirectory(), "gaminglife")
            if (file.exists()) {
                if (file.canRead() && file.canWrite()) {
                    ret = true
                }
            }
            return ret
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermission()
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                finishEnsurePermission()
            } else {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:$packageName")
                startActivityForResult(intent, REQUEST_CODE)
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkPermission() && verifyPermission()) {
                finishWithPermitted()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ),
                    REQUEST_CODE
                )
            }
        } else {
            finishEnsurePermission()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE) {
            finishEnsurePermission()
        } else {
            finishWithDenied()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                finishEnsurePermission()
            } else {
                finishWithDenied()
            }
        }
    }

    private fun checkPermission() : Boolean = (
            (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) &&
            (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED))

    private fun finishEnsurePermission() {
        if (checkPermission() && verifyPermission()) {
            finishWithPermitted()
        }
        else {
            finishWithDenied()
        }
    }

    private fun finishWithPermitted() {
        toast("External Storage Permission OK")
        setResult(PERMITTED)
        finish()
    }

    private fun finishWithDenied() {
        toast("External Storage Permission Fail")
        setResult(DENIED)
        finish()
    }
}
