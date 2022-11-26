package com.sleepysoft.gaminglife.controllers

import android.graphics.*
import com.sleepysoft.gaminglife.R
import glcore.GlFile
import glcore.GlRoot
import graphengine.*


class GlAudioRecordLayerController(
    private val mContext: GlControllerContext
    ) : GraphViewObserver, GraphInteractiveListener() {

    private lateinit var mVoiceRecordEffectLayer: GraphLayer
    private lateinit var mAudioCircle: GraphImage
    private lateinit var mCancelCircle: GraphImage
    private lateinit var mTextRectangle: GraphImage
    private var mReturnFunction: ((inputType: String, result: Any?) -> Unit)? = null

    private lateinit var mIconAudio: Bitmap
    private lateinit var mIconInput: Bitmap
    private lateinit var mIconTrash: Bitmap

 /*   private val mTextInput = EditText(GlApp.applicationContext()).apply {
        this.setText("")
        this.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
        this.isSingleLine = false
        this.setLines(6)
        // this.maxLines = 5
        this.gravity = Gravity.START or Gravity.TOP
    }

    private var mTextDialogBuilder = AlertDialog.Builder(mContext.context).apply {
        this.setTitle("文字输入")
        this.setView(mTextInput)
        this.setPositiveButton("OK") { _, _ -> onTextInputOk() }
        this.setNegativeButton("Cancel") { _, _ -> onUserInputCancel() }
    }*/

    // private val mInputDialog = mTextDialogBuilder.create()

    fun init() {
        loadResource()
        checkBuildVoiceRecordEffectLayer()
    }

    fun popupInput(operatingPos: PointF,
                   returnFunction: (inputType: String, result: Any?) -> Unit) {
        layoutItems()
        mReturnFunction = returnFunction
        mAudioCircle.moveCenter(operatingPos)
        InteractiveDecorator.changeTrackingItem(mAudioCircle)
/*        mContext.graphView.specifySelItem(mAudioCircle)
        mContext.graphView.pushObserver(this)*/
        mVoiceRecordEffectLayer.visible = true
        mContext.graphView.bringLayerToFront(mVoiceRecordEffectLayer)
        mContext.graphView.invalidate()

        GlRoot.env.glAudio.startRecord(GlFile.glRoot())
    }

    private fun releaseControl() {
        mVoiceRecordEffectLayer.visible = false
/*        val poppedLayer = mContext.graphView.popObserver()
        assert(poppedLayer == this)*/
        mContext.graphView.invalidate()
    }

    private fun loadResource() {
        mIconAudio = BitmapFactory.decodeResource(mContext.context.resources,
            R.drawable.icon_audio_recording
        )
        mIconInput = BitmapFactory.decodeResource(mContext.context.resources,
            R.drawable.icon_text_input
        )
        mIconTrash = BitmapFactory.decodeResource(mContext.context.resources, R.drawable.icon_trush)
    }

    private fun checkBuildVoiceRecordEffectLayer() {
        val layers = mContext.graphView.pickLayer { it.id == "TimeView.RecordLayer" }
        val layer = if (layers.isNotEmpty()) {
            layers[0]
        } else {
            GraphLayer("TimeView.RecordLayer", false, mContext.graphView).apply {
                this.setBackgroundAlpha(128)
                mContext.graphView.addLayer(this)
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
            this.graphActionDecorator.add(InteractiveDecorator(this).apply {
                this.interactiveListener = this@GlAudioRecordLayerController
            })
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

    override fun onItemLayout() {
        layoutItems()
    }

    override fun onViewSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        val strokeWidth = mContext.graphView.unitScale * 1.0f

        mAudioCircle.shapePaint.strokeWidth = strokeWidth
        mCancelCircle.shapePaint.strokeWidth = strokeWidth
        mTextRectangle.shapePaint.strokeWidth = strokeWidth
    }

    // -------------------------------------------------------------------------------

    override fun onItemDropped(item: GraphItem, intersectItems: List< GraphItem >) {

        GlRoot.env.glAudio.stopRecord()

        if (intersectItems.isEmpty()) {
            // Just release the record button

            GlRoot.env.glAudio.join(1500)

            // Copy wav to daily folder and rename
            onAudioRecordOk()
        }
        else {
            // Drag the record button to give up or text
            when (intersectItems[0].id) {
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

    // ---------------------------- Private ----------------------------

    private fun layoutItems() {
        if (mContext.graphView.isPortrait()) {
            layoutPortrait()
        }
        else {
            layoutLandscape()
        }
    }

    private fun layoutPortrait() {
        val area = mContext.graphView.paintArea

/*        mAudioCircle.origin = PointF(area.width() / 2, 3 * area.height() / 4)
        mAudioCircle.radius = 20 * mContext.graphView.unitScale*/

        mAudioCircle.paintArea.fromCenterSides(
            PointF(area.width() / 2, 3 * area.height() / 4),
            20 * mContext.graphView.unitScale, 20 * mContext.graphView.unitScale
        )

/*        mCancelCircle.origin = PointF(area.width() / 2, area.height() / 4)
        mCancelCircle.radius = 15 * mContext.graphView.unitScale*/
        mCancelCircle.paintArea.fromCenterSides(
            PointF(area.width() / 2, area.height() / 8),
            20 * mContext.graphView.unitScale, 20 * mContext.graphView.unitScale
        )

/*        mTextRectangle.rect = RectF(mContext.graphView.paintArea).apply {
            this.left += mContext.graphView.unitScale * 15.0f
            this.right -= mContext.graphView.unitScale * 15.0f
            this.bottom -= mContext.graphView.unitScale * 15.0f
            this.top = this.bottom - mContext.graphView.unitScale * 20.0f
        }*/

        mTextRectangle.paintArea = RectF(mContext.graphView.paintArea).apply {
            left += mContext.graphView.unitScale * 5.0f
            right -= mContext.graphView.unitScale * 5.0f
            bottom -= mContext.graphView.unitScale * 5.0f
            top = bottom - mContext.graphView.unitScale * 15.0f
        }
    }

    private fun layoutLandscape() {

    }

    // -----------------------------------------------------------------

    private fun popupTextEditor() {
    }

    private fun onTextInputOk() {
        // println(mTextInput.text)
        // mReturnFunction?.run { this("Text", mTextInput.text.toString()) }
    }

    private fun onAudioRecordOk() {
        mReturnFunction?.run { this("Audio", GlRoot.env.glAudio.WAVPath) }
    }

    private fun onUserInputCancel() {
        mReturnFunction?.run { this("Nothing", null) }
    }
}