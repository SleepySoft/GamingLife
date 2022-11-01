package com.sleepysoft.gaminglife

import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.text.InputType
import android.view.Gravity
import android.widget.EditText
import glcore.GlApplication
import glcore.GlAudioRecorder
import graphengine.*


class GlAudioRecordLayerController(
    private val mGraphView: GraphView) : GraphViewObserver {

    private lateinit var mVoiceRecordEffectLayer: GraphLayer
    private lateinit var mAudioCircle: GraphCircle
    private lateinit var mCancelCircle: GraphCircle
    private lateinit var mTextRectangle: GraphRectangle

    private val mTextDialogBuilder = AlertDialog.Builder(
        GlApplication.applicationContext()).apply { this.setTitle("文字输入") }

    fun init() {
        checkBuildVoiceRecordEffectLayer()
    }

    fun takeControl(operatingPos: PointF) {
        layoutItems()
        mAudioCircle.origin = operatingPos
        mGraphView.specifySelItem(mAudioCircle)
        mGraphView.pushObserver(this)
        mVoiceRecordEffectLayer.visible = true
    }

    private fun releaseControl() {
        mVoiceRecordEffectLayer.visible = false
        val poppedLayer = mGraphView.popObserver()
        assert(poppedLayer == this)
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

        mAudioCircle = GraphCircle().apply {
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
        }
        layer.addGraphItem(mAudioCircle)

        mCancelCircle = GraphCircle().apply {
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
        }
        layer.addGraphItem(mCancelCircle)

        mTextRectangle = GraphRectangle().apply {
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
        }
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
            mVoiceRecordEffectLayer.itemIntersectRect(droppedItem.boundRect())

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

        mAudioCircle.origin = PointF(area.width() / 2, 3 * area.height() / 4)
        mAudioCircle.radius = 20 * mGraphView.unitScale

        mCancelCircle.origin = PointF(area.width() / 2, area.height() / 4)
        mCancelCircle.radius = 15 * mGraphView.unitScale

        mTextRectangle.rect = RectF(mGraphView.paintArea).apply {
            this.left += mGraphView.unitScale * 15.0f
            this.right -= mGraphView.unitScale * 15.0f
            this.bottom -= mGraphView.unitScale * 15.0f
            this.top = this.bottom - mGraphView.unitScale * 20.0f
        }
    }

    private fun layoutLandscape() {

    }

    // -----------------------------------------------------------------

    private fun popupTextEditor() {

        val input = EditText(GlApplication.applicationContext()).apply {
            this.setText("")
            this.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            this.isSingleLine = false
            this.setLines(5)
            this.maxLines = 5
            this.gravity = Gravity.LEFT or Gravity.TOP
        }

        mTextDialogBuilder.setView(input)
        mTextDialogBuilder.setPositiveButton("OK") { _, _ -> onTextInputOk() }
        mTextDialogBuilder.setNegativeButton("Cancel") { _, _ -> onUserInputCancel() }

        val alert: AlertDialog = builder.create()
        alert.show()
    }

    private fun onTextInputOk() {

    }

    private fun onAudioRecordOk() {

    }

    private fun onUserInputCancel() {

    }
}