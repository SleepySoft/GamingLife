package com.sleepysoft.gaminglife

import android.app.AlertDialog
import android.content.Context
import android.graphics.*
import android.text.InputType
import android.view.Gravity
import android.widget.EditText
import glcore.GlFile
import glcore.GlRoot
import glenv.GlApp
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

    private val mTextInput = EditText(GlApp.applicationContext()).apply {
        this.setText("")
        this.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
        this.isSingleLine = false
        this.setLines(6)
        // this.maxLines = 5
        this.gravity = Gravity.START or Gravity.TOP
    }

    private var mTextDialogBuilder = AlertDialog.Builder(mContext).apply {
        this.setTitle("文字输入")
        this.setView(mTextInput)
        this.setPositiveButton("OK") { _, _ -> onTextInputOk() }
        this.setNegativeButton("Cancel") { _, _ -> onUserInputCancel() }
    }

    private val mInputDialog = mTextDialogBuilder.create()

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

        GlRoot.env.glAudio.startRecord(GlFile.glRoot())
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
        mAudioCircle = GraphImage(mIconAudio).apply {
            this.id = "TimeView.RecordLayer.Audio"
        }

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
        mCancelCircle = GraphImage(mIconTrash).apply {
            this.id = "TimeView.RecordLayer.Cancel"
        }

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
        mTextRectangle = GraphImage(mIconInput).apply {
            this.id = "TimeView.RecordLayer.Text"
        }

        layer.addGraphItem(mTextRectangle)
        layer.addGraphItem(mCancelCircle)
        layer.addGraphItem(mAudioCircle)

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
        GlRoot.env.glAudio.stopRecord()

        val intersectingItems: List< GraphItem > =
            mVoiceRecordEffectLayer.itemIntersectRect(droppedItem.boundRect()) {
                it != droppedItem
            }

        if (intersectingItems.isEmpty()) {
            // Just release the record button

            GlRoot.env.glAudio.join(1500)

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

        mAudioCircle.paintArea.fromCenterSides(
            PointF(area.width() / 2, 3 * area.height() / 4),
            20 * mGraphView.unitScale, 20 * mGraphView.unitScale
        )

/*        mCancelCircle.origin = PointF(area.width() / 2, area.height() / 4)
        mCancelCircle.radius = 15 * mGraphView.unitScale*/
        mCancelCircle.paintArea.fromCenterSides(
            PointF(area.width() / 2, area.height() / 8),
            20 * mGraphView.unitScale, 20 * mGraphView.unitScale
        )

/*        mTextRectangle.rect = RectF(mGraphView.paintArea).apply {
            this.left += mGraphView.unitScale * 15.0f
            this.right -= mGraphView.unitScale * 15.0f
            this.bottom -= mGraphView.unitScale * 15.0f
            this.top = this.bottom - mGraphView.unitScale * 20.0f
        }*/

        mTextRectangle.paintArea = RectF(mGraphView.paintArea).apply {
            left += mGraphView.unitScale * 5.0f
            right -= mGraphView.unitScale * 5.0f
            bottom -= mGraphView.unitScale * 5.0f
            top = bottom - mGraphView.unitScale * 15.0f
        }
    }

    private fun layoutLandscape() {

    }

    // -----------------------------------------------------------------

    private fun popupTextEditor() {
        mInputDialog.show()
    }

    private fun onTextInputOk() {
        println(mTextInput.text)
        mReturnFunction?.run { this("Text", mTextInput.text.toString()) }
    }

    private fun onAudioRecordOk() {
        mReturnFunction?.run { this("Audio", GlRoot.env.glAudio.WAVPath) }
    }

    private fun onUserInputCancel() {
        mReturnFunction?.run { this("Nothing", null) }
    }
}