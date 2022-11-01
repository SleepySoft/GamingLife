package com.sleepysoft.gaminglife

import android.os.Environment
import org.junit.Test
import org.junit.Assert.*


class GeneralTest {

    @Test
    fun testPath() {
        println(Environment.getExternalStorageState())

        println(Environment.getRootDirectory().path)
        println(Environment.getDataDirectory().path)
        println(Environment.getExternalStorageDirectory())
        println(Environment.getDownloadCacheDirectory().path)
        println(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath)
    }
}