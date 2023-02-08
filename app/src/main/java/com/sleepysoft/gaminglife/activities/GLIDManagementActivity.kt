package com.sleepysoft.gaminglife.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Layout
import android.widget.Button
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import com.sleepysoft.gaminglife.*
import com.sleepysoft.gaminglife.controllers.GlControllerContext

class GLIDManagementActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_CODE_FROM_QR = 1
        const val REQUEST_CODE_FROM_TEXT = 2
        const val REQUEST_CODE_FROM_CREATE = 3
    }

    lateinit var mButtonViewGlid: Button
    lateinit var mButtonViewPubKey: Button
    lateinit var mButtonViewPrvKey: Button
    lateinit var mButtonRegOrCreate: Button
    lateinit var mButtonSignOut: Button
    lateinit var mLayoutGroupWithKey: LinearLayout

    lateinit var mButtonImportByQR: Button
    lateinit var mButtonImportByText: Button
    lateinit var mButtonCreateNew: Button
    lateinit var mLayoutGroupWithoutKey: LinearLayout

    private val requestDataLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->

        if (result.resultCode() == GlControllerContext.RESULT_ACCEPTED) {
            when (val requestCode = result.requestCode()) {
                REQUEST_CODE_FROM_QR -> Unit
                REQUEST_CODE_FROM_TEXT -> Unit
                REQUEST_CODE_FROM_CREATE -> Unit
                else -> Unit
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_glidmanagement)

        mButtonViewGlid = findViewById(R.id.id_button_view_glid)
        mButtonViewPubKey = findViewById(R.id.id_button_view_public_key)
        mButtonViewPrvKey = findViewById(R.id.id_button_view_private_key)
        mButtonRegOrCreate = findViewById(R.id.id_button_register_check_in)
        mButtonSignOut = findViewById(R.id.id_button_sign_out)
        mLayoutGroupWithKey = findViewById(R.id.id_layout_with_key)

        mButtonViewGlid.setOnClickListener {

        }

        mButtonViewPubKey.setOnClickListener {

        }

        mButtonViewPrvKey.setOnClickListener {

        }

        mButtonRegOrCreate.setOnClickListener {

        }

        mButtonSignOut.setOnClickListener {

        }

        mButtonImportByQR = findViewById(R.id.id_button_import_scan)
        mButtonImportByText = findViewById(R.id.id_button_import_text)
        mButtonCreateNew = findViewById(R.id.id_button_create)
        mLayoutGroupWithoutKey = findViewById(R.id.id_layout_without_key)

        mButtonImportByQR.setOnClickListener {

        }

        mButtonImportByText.setOnClickListener {
            val activityIntent = Intent(this, CommonTextInputActivity::class.java)
            activityIntent.setRequestCode(REQUEST_CODE_FROM_TEXT)
            requestDataLauncher.launch(activityIntent)
        }

        mButtonCreateNew.setOnClickListener {

        }
    }
}