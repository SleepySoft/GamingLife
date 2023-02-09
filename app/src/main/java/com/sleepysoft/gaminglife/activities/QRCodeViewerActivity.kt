package com.sleepysoft.gaminglife.activities

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.king.zxing.util.CodeUtils
import com.sleepysoft.gaminglife.R


class QRCodeViewerActivity : AppCompatActivity() {
    companion object {
        const val KEY_QR_CODE = "KEY_QR_CODE"
    }

    private lateinit var mImageQR: ImageView
    private lateinit var mTextCode: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrcode_viewer)

        mImageQR = findViewById(R.id.id_image_qr)
        mTextCode = findViewById(R.id.id_text_code)

        val qrCode = intent.getStringExtra(KEY_QR_CODE)
        qrCode?.run {
            createQRCode(this)
            mTextCode.setText(this)
        }
    }

    private fun createQRCode(content: String) {
        Thread {
            val logo = BitmapFactory.decodeResource(resources, R.drawable.ic_idle)
            val bitmap = CodeUtils.createQRCode(content, 800, logo)
            runOnUiThread {
                mImageQR.setImageBitmap(bitmap)
            }
        }.start()
    }
}