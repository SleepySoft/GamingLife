package com.sleepysoft.gaminglife.views

import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.LayoutInflater
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
}