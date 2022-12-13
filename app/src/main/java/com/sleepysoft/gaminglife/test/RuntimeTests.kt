package com.sleepysoft.gaminglife.test

import android.content.Context
import android.os.Build
import android.os.Environment
import android.support.annotation.RequiresApi

import glcore.GlDailyRecord
import glcore.GlDateTime
import java.util.*


@RequiresApi(Build.VERSION_CODES.N)
fun testInternalStorage(ctx: Context) {
    println(ctx.dataDir.absolutePath)
    println(ctx.filesDir.absolutePath)
    println(ctx.cacheDir.absolutePath)

    println(ctx.getExternalFilesDir( Environment.DIRECTORY_DCIM)?.absolutePath)
    println(ctx.getExternalFilesDirs( Environment.DIRECTORY_DCIM))

    println(ctx.externalCacheDir?.absolutePath)
    println(ctx.externalCacheDirs)
}


fun testExternalStorage() {
    println(Environment.getExternalStorageState())

    println(Environment.getRootDirectory().absolutePath)
    println(Environment.getDataDirectory().absolutePath)
    println(Environment.getExternalStorageDirectory().absolutePath)
    println(Environment.getDownloadCacheDirectory().absolutePath)
    println(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath)
}


@RequiresApi(Build.VERSION_CODES.O)
fun testDailyStatistics() {
    val dailyStat = GlDailyRecord()
    dailyStat.loadDailyData(GlDateTime.stringToDate("20221115", Date()))
}


object RuntimeTest {
    @RequiresApi(Build.VERSION_CODES.O)
    fun testEntry(ctx: Context) {
        // testInternalStorage(ctx)
        // testExternalStorage()
        testDailyStatistics()
    }
}
