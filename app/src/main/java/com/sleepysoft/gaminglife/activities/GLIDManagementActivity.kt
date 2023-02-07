package com.sleepysoft.gaminglife.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Layout
import android.widget.Button
import android.widget.LinearLayout
import com.sleepysoft.gaminglife.R

class GLIDManagementActivity : AppCompatActivity() {
    lateinit var mButtonViewGlid: Button
    lateinit var mButtonViewPubKey: Button
    lateinit var mButtonViewPrvKey: Button
    lateinit var mLayoutWithKey: LinearLayout

    lateinit var mButtonRegOrCreate: Button
    lateinit var mButtonSignOut: Button
    lateinit var mLayoutWithoutKey: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_glidmanagement)

        mButtonViewGlid = findViewById(R.id.id_button_view_glid)
        mButtonViewPubKey = findViewById(R.id.id_button_view_public_key)
        mButtonViewPrvKey = findViewById(R.id.id_button_view_private_key)
        mLayoutWithKey = findViewById(R.id.id_layout_with_key)

        mButtonRegOrCreate = findViewById(R.id.id_button_register_check_in)
        mButtonSignOut = findViewById(R.id.id_button_sign_out)
        mLayoutWithoutKey = findViewById(R.id.id_layout_without_key)
    }
}