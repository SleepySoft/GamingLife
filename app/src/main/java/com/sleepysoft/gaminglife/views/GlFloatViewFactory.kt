package com.sleepysoft.gaminglife.views

import android.app.Activity
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.sleepysoft.gaminglife.R
import com.sleepysoft.gaminglife.getActivitySize


// Or use DialogFragment?

open class GlFloatView: FrameLayout {
    constructor(context: Context) : super(context)

    constructor(context: Context, layout: Int) : super(context) {
        inflate(layout)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        println("=> Attached to window")
    }

    override fun onDetachedFromWindow() {
        println("=> Detached from window")
        GlFloatViewFactory.unregisterFloatView(this)
        super.onDetachedFromWindow()
    }

    fun inflate(layout: Int) {
        LayoutInflater.from(context).inflate(layout, this)
        initLayout()
    }

    open fun initLayout() {

    }
}


object GlFloatViewFactory{
    private const val SINGLETON_MARK = "SINGLETON"

    private val floatViews = mutableMapOf<Class<*>, GlFloatView>()

    fun <T : GlFloatView> getFloatView(clazz: Class<T>) : T? {
        @Suppress("UNCHECKED_CAST")
        return floatViews[clazz] as? T
    }

    fun unregisterFloatView(instance: GlFloatView) {
        floatViews.entries.removeIf { it.value == instance }
    }

    fun <T : GlFloatView> createFloatView(context: Context, clazz: Class<T>, layout: Int? = null) : T? {
        var instance: GlFloatView? = null

        if (checkHasStaticMember(clazz, SINGLETON_MARK)) {
            instance = floatViews[clazz]
        }

        if (instance == null) {

            instance = if (layout == null) {
                val constructor = clazz.getConstructor(Context::class.java)
                constructor.newInstance(context)
            } else {
                val constructor = clazz.getConstructor(Context::class.java, Int::class.java)
                constructor.newInstance(context, layout)
            }

            instance?.run {
                val windowManager = context.getSystemService(AppCompatActivity.WINDOW_SERVICE) as WindowManager
                val layoutParams = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
                ).apply {
                    x = 0
                    y = 0
                    alpha = 0.5f
                    format = PixelFormat.RGBA_8888
                    gravity = Gravity.START or Gravity.TOP
                }
                windowManager.addView(instance, layoutParams)

                if (checkHasStaticMember(clazz, SINGLETON_MARK)) {
                    floatViews[clazz] = instance
                }
            }
        }

        @Suppress("UNCHECKED_CAST")
        return instance as? T
    }

    fun <T : GlFloatView> checkHasStaticMember(clazz: Class<T>, field: String) : Boolean {
        return try {
            clazz.getField(field)
            true
        } catch (e: NoSuchFieldException) {
            false
        } finally {

        }
    }

    fun <T : GlFloatView> showFloatView(clazz: Class<T>) {
        val instance = floatViews[clazz]
        instance?.run { visibility = View.VISIBLE }
    }

    fun <T : GlFloatView> hideFloatView(clazz: Class<T>) {
        val instance = floatViews[clazz]
        instance?.run { visibility = View.GONE }
    }

    fun <T : GlFloatView> changeFloatViewLayoutParam(
        context: Context, clazz: Class<T>, decorator: (lp: WindowManager.LayoutParams) -> Unit) {

        getFloatView(clazz)?.run {
            val windowManager = context.getSystemService(AppCompatActivity.WINDOW_SERVICE) as WindowManager
            val layoutParams = this.layoutParams as WindowManager.LayoutParams

            decorator(layoutParams)

            windowManager.updateViewLayout(this, layoutParams)
        }
    }

    fun <T : GlFloatView> shiftFloatView(context: Context, clazz: Class<T>,
                                         left: Int?=null, top: Int?=null,
                                         right: Int?=null, bottom: Int?=null) {
        getFloatView(clazz)?.run {
            changeFloatViewLayoutParam(context, clazz) { layoutParams->
                if (left != null) {
                    layoutParams.x = left
                }
                if (right != null) {
                    if (left == null) {
                        layoutParams.x = (y - layoutParams.width).toInt()
                    } else {
                        layoutParams.width = right - left
                    }
                }

                if (top != null) {
                    layoutParams.y = top
                }
                if (bottom != null) {
                    if (top == null) {
                        layoutParams.y = bottom - layoutParams.height
                    } else {
                        layoutParams.width = bottom - top
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun <T : GlFloatView> stretchFloatViewAs(
        refActivity: Activity, clazz: Class<T>, horizon: Boolean, vertical: Boolean) {
        changeFloatViewLayoutParam(refActivity, clazz) { layoutParams->

            val size = refActivity.getActivitySize()

            if (horizon) {
                layoutParams.width = size.width
            }

            if (vertical) {
                layoutParams.height = size.height
            }
        }
    }

    fun <T : GlFloatView> moveFloatViewUnderActionBar(refActivity: AppCompatActivity, clazz: Class<T>) {
        getFloatView(clazz)?.run {
            // val actionBarView = refActivity.supportActionBar?.customView
            // val topLimit = actionBarView?.bottom ?: 0

            // val contentView = refActivity.findViewById<View>(android.R.id.content)
            // val actionBarHeight = contentView?.top ?: 0

            val actionBarHeight = refActivity.supportActionBar?.height ?: 0

            shiftFloatView(refActivity, clazz, top=actionBarHeight)
        }
    }
}
