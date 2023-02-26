package com.sleepysoft.gaminglife.activities

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.king.zxing.CameraScan
import com.king.zxing.CaptureActivity
import com.king.zxing.util.CodeUtils
import com.sleepysoft.gaminglife.*
import com.sleepysoft.gaminglife.controllers.GlControllerContext
import glcore.GlRoot
import glenv.GlKeyPair
import glenv.KeyPairUtility
import pub.devrel.easypermissions.EasyPermissions

class GLIDManagementActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_CODE_FROM_QR = 1
        const val REQUEST_CODE_FROM_IMG = 2
        const val REQUEST_CODE_FROM_TEXT = 3
        const val REQUEST_CODE_FROM_CREATE = 4

        const val REQUEST_PERMISSION_CAMERA = 11
        const val REQUEST_PERMISSION_IMAGE = 12

        const val KEY_TITLE = "key_title"
        const val KEY_IS_QR_CODE = "key_code"
        const val KEY_IS_CONTINUOUS = "key_continuous_scan"
    }

    lateinit var mButtonViewGlid: Button
    lateinit var mButtonViewPubKey: Button
    lateinit var mButtonViewPrvKey: Button
    lateinit var mButtonRegOrCreate: Button
    lateinit var mButtonSignOut: Button
    lateinit var mLayoutGroupWithKey: LinearLayout

    lateinit var mButtonImportByQR: Button
    lateinit var mButtonImportByImg: Button
    lateinit var mButtonImportByText: Button
    lateinit var mButtonCreateNew: Button
    lateinit var mLayoutGroupWithoutKey: LinearLayout

    @RequiresApi(Build.VERSION_CODES.O)
    private val requestDataLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->

        if (result.resultCode() == GlControllerContext.RESULT_ACCEPTED) {
            when (result.requestCode()) {
                REQUEST_CODE_FROM_QR -> loadGlId()
                REQUEST_CODE_FROM_TEXT -> loadGlId()
                REQUEST_CODE_FROM_CREATE -> loadGlId()
                else -> Unit
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
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
            val intent = Intent(this, QRCodeViewerActivity::class.java)
            intent.putExtra(QRCodeViewerActivity.KEY_QR_CODE, GlRoot.systemConfig.GLID)
            ActivityCompat.startActivity(this, intent, null)
        }

        mButtonViewPubKey.setOnClickListener {
            val intent = Intent(this, QRCodeViewerActivity::class.java)
            val publicKeySerialized = GlRoot.systemConfig.mainKeyPair.publicKeyString
            if (publicKeySerialized.isNotEmpty()) {
                intent.putExtra(QRCodeViewerActivity.KEY_QR_CODE, publicKeySerialized)
                ActivityCompat.startActivity(this, intent, null)
            }
        }

        mButtonViewPrvKey.setOnClickListener {
            val intent = Intent(this, QRCodeViewerActivity::class.java)
            val privateKeySerialized = GlRoot.systemConfig.mainKeyPair.privateKeyString
            if (privateKeySerialized.isNotEmpty()) {
                intent.putExtra(QRCodeViewerActivity.KEY_QR_CODE, privateKeySerialized)
                ActivityCompat.startActivity(this, intent, null)
            }
        }

        mButtonRegOrCreate.setOnClickListener {

        }

        mButtonSignOut.setOnClickListener {
            GlRoot.systemConfig.GLID = ""
            GlRoot.systemConfig.mainKeyPair = GlKeyPair()
            loadGlId()
        }

        mButtonImportByQR = findViewById(R.id.id_button_import_scan)
        mButtonImportByImg = findViewById(R.id.id_button_import_image)
        mButtonImportByText = findViewById(R.id.id_button_import_text)
        mButtonCreateNew = findViewById(R.id.id_button_create)
        mLayoutGroupWithoutKey = findViewById(R.id.id_layout_without_key)

        mButtonImportByQR.setOnClickListener {
            requestScan()
        }

        mButtonImportByImg.setOnClickListener {
            requestPhoto()
        }

        mButtonImportByText.setOnClickListener {
            val activityIntent = Intent(this, CommonTextInputActivity::class.java)
            activityIntent.setRequestCode(REQUEST_CODE_FROM_TEXT)
            requestDataLauncher.launch(activityIntent)
        }

        mButtonCreateNew.setOnClickListener {
            val activityIntent = Intent(this, GeneratePairActivity::class.java)
            activityIntent.setRequestCode(REQUEST_CODE_FROM_CREATE)
            requestDataLauncher.launch(activityIntent)
        }

        loadGlId()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null) {
            when (requestCode) {
                REQUEST_CODE_FROM_QR -> {
                    val result = CameraScan.parseScanResult(data)
                    result?.run { parseQRResult(this) }
                }
                REQUEST_PERMISSION_CAMERA -> startScan()

                REQUEST_CODE_FROM_IMG -> parseImage(data)
                REQUEST_PERMISSION_IMAGE -> selectImage()
            }
        }
    }

    // ---------------------------------------------------------------------------------------------

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadGlId() {
        val mainKeyPair = GlRoot.systemConfig.mainKeyPair
        if (mainKeyPair.keyPairValid()) {
            mLayoutGroupWithKey.visibility = View.VISIBLE
            mLayoutGroupWithoutKey.visibility = View.GONE
        } else {
            mLayoutGroupWithKey.visibility = View.GONE
            mLayoutGroupWithoutKey.visibility = View.VISIBLE
        }
    }

    private fun requestScan() {
        val permissions = arrayOf(Manifest.permission.CAMERA)
        if (EasyPermissions.hasPermissions(this, *permissions)) {
            startScan()
        } else {
            EasyPermissions.requestPermissions(
                this, getString(R.string.HINT_PERMISSION_CAMERA_QR),
                REQUEST_PERMISSION_CAMERA, *permissions)
        }
    }

    private fun startScan() {
        val intent = Intent(this, CaptureActivity::class.java)
        intent.putExtra(KEY_TITLE, "")
        intent.putExtra(KEY_IS_CONTINUOUS, false)
        ActivityCompat.startActivityForResult(this, intent, REQUEST_CODE_FROM_QR, null)
    }

    private fun requestPhoto() {
        val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (EasyPermissions.hasPermissions(this, *permissions)) {
            selectImage()
        } else {
            EasyPermissions.requestPermissions(
                this, getString(R.string.HINT_PERMISSION_EXT_STORAGE_QR),
                REQUEST_PERMISSION_IMAGE, *permissions
            )
        }
    }

    private fun selectImage() {
        val pickIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        startActivityForResult(pickIntent, REQUEST_CODE_FROM_IMG)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun parseImage(data: Intent) {
        try {
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, data.data)
            Thread {
                val result = CodeUtils.parseCode(bitmap)
                result?.run {
                    runOnUiThread {
                        parseQRResult(result)
                    }
                }
            }.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun parseQRResult(result: String) {
        val keyPair = KeyPairUtility.deserializeKeyPair(result)
        val glKeyPair = GlKeyPair().apply { fromJavaKeyPair(keyPair) }
        if (glKeyPair.keyPairValid()) {
            GlRoot.systemConfig.mainKeyPair= glKeyPair
            loadGlId()
        } else {
            toast(getString(R.string.HINT_LOAD_PRIVATE_KEY_ERROR))
        }
    }
}