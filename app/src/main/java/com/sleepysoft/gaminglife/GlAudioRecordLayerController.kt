package com.sleepysoft.gaminglife

import android.app.AlertDialog
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.text.InputType
import android.view.Gravity
import android.widget.EditText
import glcore.GlApplication
import glcore.GlAudioRecorder
import graphengine.*


class GlAudioRecordLayerController(
    private val mContext: Context,
    private val mGraphView: GraphView) : GraphViewObserver {

    private lateinit var mVoiceRecordEffectLayer: GraphLayer
    private lateinit var mAudioCircle: GraphImage
    private lateinit var mCancelCircle: GraphImage
    private lateinit var mTextRectangle: GraphImage
    private var mReturnFunction: ((inputType: String, result: Any?) -> Unit)? = null

    private lateinit var mIconAudio: Bitmap
    private lateinit var mIconInput: Bitmap
    private lateinit var mIconTrash: Bitmap

    private val mTextInput = EditText(GlApplication.applicationContext()).apply {
        this.setText("")
        this.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
        this.isSingleLine = false
        this.setLines(6)
        // this.maxLines = 5
        this.gravity = Gravity.START or Gravity.TOP
    }

    private val mTextDialogBuilder = AlertDialog.Builder(mContext).apply {
        this.setTitle("文字输入")
        this.setView(mTextInput)
        this.setPositiveButton("OK") { _, _ -> onTextInputOk() }
        this.setNegativeButton("Cancel") { _, _ -> onUserInputCancel() }
    }

    fun init() {
        loadResource()
        checkBuildVoiceRecordEffectLayer()
    }

    fun popupInput(operatingPos: PointF,
                   returnFunction: (inputType: String, result: Any?) -> Unit) {
        layoutItems()
        mReturnFunction = returnFunction
        mAudioCircle.moveCenter(operatingPos)
        mGraphView.specifySelItem(mAudioCircle)
        mGraphView.pushObserver(this)
        mVoiceRecordEffectLayer.visible = true
        mGraphView.invalidate()
    }

    private fun releaseControl() {
        mVoiceRecordEffectLayer.visible = false
        val poppedLayer = mGraphView.popObserver()
        assert(poppedLayer == this)
        mGraphView.invalidate()
    }

    private fun loadResource() {
        mIconAudio = BitmapFactory.decodeResource(mContext.resources, R.drawable.icon_audio_recording)
        mIconInput = BitmapFactory.decodeResource(mContext.resources, R.drawable.icon_text_input)
        mIconTrash = BitmapFactory.decodeResource(mContext.resources, R.drawable.icon_trush)
    }

    private fun checkBuildVoiceRecordEffectLayer() {
        val layers = mGraphView.pickLayer { it.id == "TimeView.RecordLayer" }
        val layer = if (layers.isNotEmpty()) {
            layers[0]
        } else {
            GraphLayer("TimeView.RecordLayer", false).apply {
                this.setBackgroundAlpha(128)
                mGraphView.addLayer(this)
            }
        }

        layer.removeGraphItem() { true }

/*        mAudioCircle = GraphCircle().apply {
            this.id = "TimeView.RecordLayer.Audio"
            this.mainText = "A"

            this.fontPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = Color.parseColor("#FFFFFF")
                this.textAlign = Paint.Align.CENTER
            }
            this.shapePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = Color.parseColor("#90D7EC")
                this.style = Paint.Style.FILL
            }
        }*/
        mAudioCircle = GraphImage(mIconAudio)
        layer.addGraphItem(mAudioCircle)

/*        mCancelCircle = GraphCircle().apply {
            this.id = "TimeView.RecordLayer.Cancel"
            this.mainText = "Cancel"

            this.fontPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = Color.parseColor("#000000")
                this.textAlign = Paint.Align.CENTER
            }
            this.shapePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = Color.parseColor("#90D7EC")
                this.style = Paint.Style.FILL
            }
        }*/
        mCancelCircle = GraphImage(mIconTrash)
        layer.addGraphItem(mCancelCircle)

/*        mTextRectangle = GraphRectangle().apply {
            this.id = "TimeView.RecordLayer.Text"
            this.mainText = "Text"

            this.fontPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = Color.parseColor("#000000")
                this.textAlign = Paint.Align.CENTER
            }
            this.shapePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = Color.parseColor("#90D7EC")
                this.style = Paint.Style.FILL
            }
        }*/
        mTextRectangle = GraphImage(mIconInput)
        layer.addGraphItem(mTextRectangle)

        mVoiceRecordEffectLayer = layer
    }

    // -------------------------- Implements GraphViewObserver interface ---------------------------

    override fun onViewSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        val strokeWidth = mGraphView.unitScale * 1.0f

        mAudioCircle.shapePaint.strokeWidth = strokeWidth
        mCancelCircle.shapePaint.strokeWidth = strokeWidth
        mTextRectangle.shapePaint.strokeWidth = strokeWidth
    }

    override fun onItemDropped(droppedItem: GraphItem) {
        GlAudioRecorder.stopRecord()
        val intersectingItems: List< GraphItem > =
            mVoiceRecordEffectLayer.itemIntersectRect(droppedItem.boundRect()) {
                it != droppedItem
            }

        if (intersectingItems.isEmpty()) {
            // Just release the record button

            GlAudioRecorder.join(1500)

            // Copy wav to daily folder and rename
            onAudioRecordOk()
        }
        else {
            // Drag the record button to give up or text
            when (intersectingItems[0].id) {
                "TimeView.RecordLayer.Text" -> {
                    // Open text editor
                    popupTextEditor()
                }
                "TimeView.RecordLayer.Cancel" -> {
                    // Do nothing
                    onUserInputCancel()
                }
                else -> {
                    // Should not reach here
                    assert(false)
                }
            }
        }
        releaseControl()
    }

    override fun onItemLayout() {
        layoutItems()
    }

    // ---------------------------- Private ----------------------------

    private fun layoutItems() {
        if (mGraphView.isPortrait()) {
            layoutPortrait()
        }
        else {
            layoutLandscape()
        }
    }

    private fun layoutPortrait() {
        val area = mGraphView.paintArea

/*        mAudioCircle.origin = PointF(area.width() / 2, 3 * area.height() / 4)
        mAudioCircle.radius = 20 * mGraphView.unitScale*/
        mAudioCircle.moveCenter(PointF(area.width() / 2, 3 * area.height() / 4))

/*        mCancelCircle.origin = PointF(area.width() / 2, area.height() / 4)
        mCancelCircle.radius = 15 * mGraphView.unitScale*/
        mCancelCircle.moveCenter(PointF(area.width() / 2, area.height() / 4))

/*        mTextRectangle.rect = RectF(mGraphView.paintArea).apply {
            this.left += mGraphView.unitScale * 15.0f
            this.right -= mGraphView.unitScale * 15.0f
            this.bottom -= mGraphView.unitScale * 15.0f
            this.top = this.bottom - mGraphView.unitScale * 20.0f
        }*/
        mTextRectangle.moveCenter(
            PointF(
                mGraphView.paintArea.width() / 2.0f,
                mGraphView.paintArea.bottom - 50.0f
        ))
    }

    private fun layoutLandscape() {

    }

    // -----------------------------------------------------------------

    private fun popupTextEditor() {
        val inputDlg = mTextDialogBuilder.create()
        inputDlg.show()
    }

    private fun onTextInputOk() {
        println(mTextInput.text)
        mReturnFunction?.run { this("Text", mTextInput.text.toString()) }
    }

    private fun onAudioRecordOk() {
        mReturnFunction?.run { this("Audio", GlAudioRecorder.WAVPath) }
    }

    private fun onUserInputCancel() {
        mReturnFunction?.run { this("Nothing", null) }
    }
}