package com.sleepysoft.gaminglife.views

import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.sleepysoft.gaminglife.R


open class GlFloatView: FrameLayout {
    constructor(context: Context) : super(context)

    constructor(context: Context, layout: Int) : super(context) {
        inflate(layout)
    }

    fun inflate(layout: Int) {
        LayoutInflater.from(context).inflate(R.layout.layout_view_top_menu, this)
        initLayout()
    }

    open fun initLayout() {

    }
}


object GlFloatViewFactory{
    const val SINGLETON_MARK = "SINGLETON"

    private val floatViews = mutableMapOf<Class<*>, GlFloatView>()

    fun <T : GlFloatView> getFloatView(clazz: Class<T>) : T? {
        @Suppress("UNCHECKED_CAST")
        return floatViews[clazz] as? T
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
                floatViews[clazz] = instance
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

    fun <T : GlFloatView> shiftFloatView(context: Context, clazz: Class<T>,
                                         left: Int?=null, top: Int?=null,
                                         right: Int?=null, bottom: Int?=null) {
        getFloatView(clazz)?.run {
            val windowManager = context.getSystemService(AppCompatActivity.WINDOW_SERVICE) as WindowManager
            val layoutParams = this.layoutParams as WindowManager.LayoutParams

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

            windowManager.updateViewLayout(this, layoutParams)
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
